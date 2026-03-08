package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.InventoryActionUtils;
import com.wms.entity.Goods;
import com.wms.entity.GoodsBatch;
import com.wms.entity.Record;
import com.wms.entity.Storage;
import com.wms.entity.WarehouseTransfer;
import com.wms.service.GoodsBatchService;
import com.wms.service.GoodsService;
import com.wms.service.InventoryAlertService;
import com.wms.service.InventoryFlowService;
import com.wms.service.RecordService;
import com.wms.service.StorageService;
import com.wms.service.WarehouseTransferService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryFlowServiceImpl implements InventoryFlowService {

    @Resource
    private GoodsService goodsService;

    @Resource
    private GoodsBatchService goodsBatchService;

    @Resource
    private RecordService recordService;

    @Resource
    private WarehouseTransferService warehouseTransferService;

    @Resource
    private StorageService storageService;

    @Resource
    private InventoryAlertService inventoryAlertService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRecord(Record record) {
        if (record == null || record.getGoods() == null) {
            throw new IllegalArgumentException("请选择货品");
        }

        int quantity = Math.abs(record.getCount() == null ? 0 : record.getCount());
        if (quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于 0");
        }

        Goods goods = goodsService.getById(record.getGoods());
        if (goods == null) {
            throw new IllegalArgumentException("货品不存在");
        }

        String actionType = InventoryActionUtils.normalize(record.getActionType(), record.getAction());
        int currentCount = goods.getCount() == null ? 0 : goods.getCount();
        int nextCount = currentCount + InventoryActionUtils.signedDelta(actionType, quantity);
        if (nextCount < 0) {
            throw new IllegalStateException("库存不足，无法完成出库");
        }

        GoodsBatch batch = null;
        if (record.getBatchId() != null) {
            batch = goodsBatchService.getById(record.getBatchId());
            if (batch == null) {
                throw new IllegalArgumentException("批次不存在");
            }
            if (!record.getGoods().equals(batch.getGoodsId())) {
                throw new IllegalStateException("批次与货品不匹配");
            }

            int batchCurrent = batch.getQuantity() == null ? 0 : batch.getQuantity();
            int nextBatchCount = batchCurrent + InventoryActionUtils.signedDelta(actionType, quantity);
            if (nextBatchCount < 0) {
                throw new IllegalStateException("批次库存不足，无法完成出库");
            }

            batch.setQuantity(nextBatchCount);
            goodsBatchService.updateById(batch);
            if (record.getSupplierId() == null) {
                record.setSupplierId(batch.getSupplierId());
            }
        } else if (record.getSupplierId() == null) {
            record.setSupplierId(goods.getSupplierId());
        }

        goods.setCount(nextCount);
        goodsService.updateById(goods);

        record.setCount(quantity);
        record.setActionType(actionType);
        recordService.save(record);

        inventoryAlertService.evaluateGoodsAlert(goods);
        if (batch != null) {
            inventoryAlertService.evaluateBatchAlert(batch);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeTransfer(WarehouseTransfer transfer) {
        WarehouseTransfer current = transfer.getId() == null ? transfer : warehouseTransferService.getById(transfer.getId());
        if (current == null) {
            throw new IllegalArgumentException("调拨单不存在");
        }
        if ("COMPLETED".equalsIgnoreCase(current.getStatus())) {
            throw new IllegalStateException("调拨单已执行");
        }
        if (current.getGoodsId() == null || current.getFromStorageId() == null || current.getToStorageId() == null) {
            throw new IllegalArgumentException("调拨参数不完整");
        }
        if (current.getFromStorageId().equals(current.getToStorageId())) {
            throw new IllegalArgumentException("调入仓库不能与调出仓库相同");
        }
        int quantity = current.getQuantity() == null ? 0 : current.getQuantity();
        if (quantity <= 0) {
            throw new IllegalArgumentException("调拨数量必须大于 0");
        }

        Goods sourceGoods = goodsService.getById(current.getGoodsId());
        if (sourceGoods == null) {
            throw new IllegalArgumentException("源货品不存在");
        }
        if (!current.getFromStorageId().equals(sourceGoods.getStorage())) {
            throw new IllegalStateException("源货品所在仓库与调拨单不一致");
        }
        if ((sourceGoods.getCount() == null ? 0 : sourceGoods.getCount()) < quantity) {
            throw new IllegalStateException("源仓库库存不足");
        }

        Goods targetGoods = findOrCreateTargetGoods(sourceGoods, current.getToStorageId());

        sourceGoods.setCount((sourceGoods.getCount() == null ? 0 : sourceGoods.getCount()) - quantity);
        targetGoods.setCount((targetGoods.getCount() == null ? 0 : targetGoods.getCount()) + quantity);
        goodsService.updateById(sourceGoods);
        goodsService.updateById(targetGoods);

        GoodsBatch sourceBatch = null;
        GoodsBatch targetBatch = null;
        if (current.getBatchId() != null) {
            sourceBatch = goodsBatchService.getById(current.getBatchId());
            if (sourceBatch == null) {
                throw new IllegalArgumentException("源批次不存在");
            }
            if ((sourceBatch.getQuantity() == null ? 0 : sourceBatch.getQuantity()) < quantity) {
                throw new IllegalStateException("源批次库存不足");
            }

            sourceBatch.setQuantity((sourceBatch.getQuantity() == null ? 0 : sourceBatch.getQuantity()) - quantity);
            goodsBatchService.updateById(sourceBatch);

            targetBatch = findOrCreateTargetBatch(sourceBatch, targetGoods.getId(), current.getToStorageId());
            targetBatch.setQuantity((targetBatch.getQuantity() == null ? 0 : targetBatch.getQuantity()) + quantity);
            if (targetBatch.getId() == null) {
                goodsBatchService.save(targetBatch);
            } else {
                goodsBatchService.updateById(targetBatch);
            }

            current.setBatchNo(sourceBatch.getBatchNo());
        }

        Storage fromStorage = storageService.getById(current.getFromStorageId());
        Storage toStorage = storageService.getById(current.getToStorageId());
        current.setGoodsName(sourceGoods.getName());
        current.setFromStorageName(fromStorage == null ? "-" : fromStorage.getName());
        current.setToStorageName(toStorage == null ? "-" : toStorage.getName());
        current.setStatus("COMPLETED");
        current.setExecuteTime(LocalDateTime.now());
        warehouseTransferService.saveOrUpdate(current);

        Record transferOut = new Record();
        transferOut.setGoods(sourceGoods.getId());
        transferOut.setBatchId(sourceBatch == null ? null : sourceBatch.getId());
        transferOut.setSupplierId(sourceBatch == null ? sourceGoods.getSupplierId() : sourceBatch.getSupplierId());
        transferOut.setUserid(current.getOperatorId());
        transferOut.setAdminId(current.getOperatorId());
        transferOut.setCount(quantity);
        transferOut.setRemark("仓库调拨出库: " + current.getToStorageName());
        transferOut.setActionType("TRANSFER_OUT");
        recordService.save(transferOut);

        Record transferIn = new Record();
        transferIn.setGoods(targetGoods.getId());
        transferIn.setBatchId(targetBatch == null ? null : targetBatch.getId());
        transferIn.setSupplierId(targetBatch == null ? targetGoods.getSupplierId() : targetBatch.getSupplierId());
        transferIn.setUserid(current.getOperatorId());
        transferIn.setAdminId(current.getOperatorId());
        transferIn.setCount(quantity);
        transferIn.setRemark("仓库调拨入库: " + current.getFromStorageName());
        transferIn.setActionType("TRANSFER_IN");
        recordService.save(transferIn);

        inventoryAlertService.evaluateGoodsAlert(sourceGoods);
        inventoryAlertService.evaluateGoodsAlert(targetGoods);
        if (sourceBatch != null) {
            inventoryAlertService.evaluateBatchAlert(sourceBatch);
        }
        if (targetBatch != null) {
            inventoryAlertService.evaluateBatchAlert(targetBatch);
        }
    }

    private Goods findOrCreateTargetGoods(Goods sourceGoods, Integer targetStorageId) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStorage, targetStorageId)
                .eq(Goods::getName, sourceGoods.getName())
                .eq(Goods::getGoodstype, sourceGoods.getGoodstype());
        if (sourceGoods.getSupplierId() != null) {
            wrapper.eq(Goods::getSupplierId, sourceGoods.getSupplierId());
        }
        Goods targetGoods = goodsService.getOne(wrapper.last("limit 1"));
        if (targetGoods != null) {
            return targetGoods;
        }

        targetGoods = new Goods();
        targetGoods.setName(sourceGoods.getName());
        targetGoods.setStorage(targetStorageId);
        targetGoods.setGoodstype(sourceGoods.getGoodstype());
        targetGoods.setCount(0);
        targetGoods.setRemark(sourceGoods.getRemark());
        targetGoods.setBarcode(sourceGoods.getBarcode());
        targetGoods.setRfidTag(sourceGoods.getRfidTag());
        targetGoods.setMinStock(sourceGoods.getMinStock());
        targetGoods.setMaxStock(sourceGoods.getMaxStock());
        targetGoods.setSupplierId(sourceGoods.getSupplierId());
        goodsService.save(targetGoods);
        return targetGoods;
    }

    private GoodsBatch findOrCreateTargetBatch(GoodsBatch sourceBatch, Integer targetGoodsId, Integer targetStorageId) {
        LambdaQueryWrapper<GoodsBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsBatch::getGoodsId, targetGoodsId)
                .eq(GoodsBatch::getStorageId, targetStorageId)
                .eq(GoodsBatch::getBatchNo, sourceBatch.getBatchNo());
        GoodsBatch targetBatch = goodsBatchService.getOne(wrapper.last("limit 1"));
        if (targetBatch != null) {
            return targetBatch;
        }

        targetBatch = new GoodsBatch();
        targetBatch.setGoodsId(targetGoodsId);
        targetBatch.setStorageId(targetStorageId);
        targetBatch.setSupplierId(sourceBatch.getSupplierId());
        targetBatch.setBatchNo(sourceBatch.getBatchNo());
        targetBatch.setBarcode(sourceBatch.getBarcode());
        targetBatch.setRfidTag(sourceBatch.getRfidTag());
        targetBatch.setProductionDate(sourceBatch.getProductionDate());
        targetBatch.setExpiryDate(sourceBatch.getExpiryDate());
        targetBatch.setQuantity(0);
        targetBatch.setAlertDays(sourceBatch.getAlertDays());
        targetBatch.setStatus(sourceBatch.getStatus());
        targetBatch.setRemark(sourceBatch.getRemark());
        targetBatch.setCreateTime(LocalDateTime.now());
        return targetBatch;
    }
}
