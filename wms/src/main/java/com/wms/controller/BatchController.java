package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.GoodsBatch;
import com.wms.service.GoodsBatchService;
import com.wms.service.GoodsService;
import com.wms.service.InventoryAlertService;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

@RestController
@RequestMapping("/batch")
public class BatchController {

    @Resource
    private GoodsBatchService goodsBatchService;

    @Resource
    private GoodsService goodsService;

    @Resource
    private InventoryAlertService inventoryAlertService;

    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class)
    public Result save(@RequestBody GoodsBatch batch) {
        try {
            Goods goods = validateGoods(batch.getGoodsId(), batch.getStorageId());
            int quantity = batch.getQuantity() == null ? 0 : batch.getQuantity();
            if (quantity < 0) {
                return Result.fail("批次数量不能小于 0");
            }
            if (batch.getStorageId() == null) {
                batch.setStorageId(goods.getStorage());
            }
            if (batch.getSupplierId() == null) {
                batch.setSupplierId(goods.getSupplierId());
            }
            if (StringUtils.isBlank(batch.getStatus())) {
                batch.setStatus("ACTIVE");
            }
            if (batch.getAlertDays() == null) {
                batch.setAlertDays(30);
            }
            if (batch.getCreateTime() == null) {
                batch.setCreateTime(LocalDateTime.now());
            }
            goodsBatchService.save(batch);
            goods.setCount((goods.getCount() == null ? 0 : goods.getCount()) + quantity);
            goodsService.updateById(goods);
            inventoryAlertService.evaluateGoodsAlert(goods);
            inventoryAlertService.evaluateBatchAlert(batch);
            return Result.suc();
        } catch (RuntimeException ex) {
            return Result.fail(ex.getMessage());
        }
    }

    @PostMapping("/update")
    @Transactional(rollbackFor = Exception.class)
    public Result update(@RequestBody GoodsBatch batch) {
        GoodsBatch existing = goodsBatchService.getById(batch.getId());
        if (existing == null) {
            return Result.fail("批次不存在");
        }

        try {
            Goods oldGoods = validateGoods(existing.getGoodsId(), existing.getStorageId());
            Goods newGoods = validateGoods(batch.getGoodsId() == null ? existing.getGoodsId() : batch.getGoodsId(),
                    batch.getStorageId() == null ? existing.getStorageId() : batch.getStorageId());

            int oldQuantity = existing.getQuantity() == null ? 0 : existing.getQuantity();
            int nextQuantity = batch.getQuantity() == null ? oldQuantity : batch.getQuantity();
            if (nextQuantity < 0) {
                return Result.fail("批次数量不能小于 0");
            }

            if (!oldGoods.getId().equals(newGoods.getId())) {
                oldGoods.setCount((oldGoods.getCount() == null ? 0 : oldGoods.getCount()) - oldQuantity);
                newGoods.setCount((newGoods.getCount() == null ? 0 : newGoods.getCount()) + nextQuantity);
                if ((oldGoods.getCount() == null ? 0 : oldGoods.getCount()) < 0) {
                    return Result.fail("原货品库存不足，无法迁移批次");
                }
                goodsService.updateById(oldGoods);
                goodsService.updateById(newGoods);
                inventoryAlertService.evaluateGoodsAlert(oldGoods);
            } else {
                int delta = nextQuantity - oldQuantity;
                newGoods.setCount((newGoods.getCount() == null ? 0 : newGoods.getCount()) + delta);
                if ((newGoods.getCount() == null ? 0 : newGoods.getCount()) < 0) {
                    return Result.fail("货品库存不足");
                }
                goodsService.updateById(newGoods);
            }

            existing.setGoodsId(newGoods.getId());
            existing.setStorageId(batch.getStorageId() == null ? existing.getStorageId() : batch.getStorageId());
            existing.setSupplierId(batch.getSupplierId() == null ? existing.getSupplierId() : batch.getSupplierId());
            existing.setBatchNo(StringUtils.isBlank(batch.getBatchNo()) ? existing.getBatchNo() : batch.getBatchNo());
            existing.setBarcode(batch.getBarcode());
            existing.setRfidTag(batch.getRfidTag());
            existing.setProductionDate(batch.getProductionDate());
            existing.setExpiryDate(batch.getExpiryDate());
            existing.setQuantity(nextQuantity);
            existing.setAlertDays(batch.getAlertDays() == null ? existing.getAlertDays() : batch.getAlertDays());
            existing.setStatus(StringUtils.isBlank(batch.getStatus()) ? existing.getStatus() : batch.getStatus());
            existing.setRemark(batch.getRemark());

            goodsBatchService.updateById(existing);
            inventoryAlertService.evaluateGoodsAlert(newGoods);
            inventoryAlertService.evaluateBatchAlert(existing);
            return Result.suc();
        } catch (RuntimeException ex) {
            return Result.fail(ex.getMessage());
        }
    }

    @GetMapping("/del")
    @Transactional(rollbackFor = Exception.class)
    public Result del(@RequestParam Integer id) {
        GoodsBatch batch = goodsBatchService.getById(id);
        if (batch == null) {
            return Result.fail("批次不存在");
        }
        if ((batch.getQuantity() == null ? 0 : batch.getQuantity()) > 0) {
            return Result.fail("请先将批次库存出清后再删除");
        }
        return goodsBatchService.removeById(id) ? Result.suc() : Result.fail("删除批次失败");
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Integer goodsId) {
        LambdaQueryWrapper<GoodsBatch> wrapper = new LambdaQueryWrapper<>();
        if (goodsId != null) {
            wrapper.eq(GoodsBatch::getGoodsId, goodsId);
        }
        wrapper.orderByAsc(GoodsBatch::getExpiryDate).orderByDesc(GoodsBatch::getId);
        return Result.suc(goodsBatchService.list(wrapper));
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        String batchNo = (String) param.get("batchNo");
        String goodsId = (String) param.get("goodsId");
        String storageId = (String) param.get("storageId");
        String status = (String) param.get("status");
        String expiringOnly = (String) param.get("expiringOnly");

        Page<GoodsBatch> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<GoodsBatch> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(batchNo) && !"null".equals(batchNo)) {
            wrapper.like(GoodsBatch::getBatchNo, batchNo);
        }
        if (StringUtils.isNotBlank(goodsId) && !"null".equals(goodsId)) {
            wrapper.eq(GoodsBatch::getGoodsId, Integer.parseInt(goodsId));
        }
        if (StringUtils.isNotBlank(storageId) && !"null".equals(storageId)) {
            wrapper.eq(GoodsBatch::getStorageId, Integer.parseInt(storageId));
        }
        if (StringUtils.isNotBlank(status) && !"null".equals(status)) {
            wrapper.eq(GoodsBatch::getStatus, status);
        }
        if ("true".equalsIgnoreCase(expiringOnly)) {
            wrapper.le(GoodsBatch::getExpiryDate, LocalDate.now().plusDays(30));
        }
        wrapper.orderByAsc(GoodsBatch::getExpiryDate).orderByDesc(GoodsBatch::getId);

        IPage<GoodsBatch> result = goodsBatchService.page(page, wrapper);
        return Result.suc(result.getRecords(), result.getTotal());
    }

    private Goods validateGoods(Integer goodsId, Integer storageId) {
        if (goodsId == null) {
            throw new IllegalArgumentException("请选择货品");
        }
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new IllegalArgumentException("货品不存在");
        }
        if (storageId != null && goods.getStorage() != null && !storageId.equals(goods.getStorage())) {
            throw new IllegalStateException("批次仓库必须与货品所在仓库一致");
        }
        return goods;
    }
}
