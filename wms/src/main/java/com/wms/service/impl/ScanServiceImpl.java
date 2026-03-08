package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.dto.ScanOperationDTO;
import com.wms.entity.Goods;
import com.wms.entity.GoodsBatch;
import com.wms.entity.Record;
import com.wms.service.GoodsBatchService;
import com.wms.service.GoodsService;
import com.wms.service.InventoryFlowService;
import com.wms.service.ScanService;
import com.wms.vo.ScanResolveVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ScanServiceImpl implements ScanService {

    @Resource
    private GoodsService goodsService;

    @Resource
    private GoodsBatchService goodsBatchService;

    @Resource
    private InventoryFlowService inventoryFlowService;

    @Override
    public ScanResolveVO resolve(String code) {
        ScanResolveVO vo = new ScanResolveVO();
        vo.setCode(code);
        if (code == null || code.trim().isEmpty()) {
            vo.setMatched(false);
            return vo;
        }

        LambdaQueryWrapper<GoodsBatch> batchWrapper = new LambdaQueryWrapper<>();
        batchWrapper.and(wrapper -> wrapper.eq(GoodsBatch::getBarcode, code).or().eq(GoodsBatch::getRfidTag, code));
        List<GoodsBatch> batches = goodsBatchService.list(batchWrapper);
        Goods goods = null;
        if (!batches.isEmpty()) {
            goods = goodsService.getById(batches.get(0).getGoodsId());
        } else {
            LambdaQueryWrapper<Goods> goodsWrapper = new LambdaQueryWrapper<>();
            goodsWrapper.and(wrapper -> wrapper.eq(Goods::getBarcode, code).or().eq(Goods::getRfidTag, code));
            goods = goodsService.getOne(goodsWrapper.last("limit 1"));
            if (goods != null) {
                batches = goodsBatchService.lambdaQuery()
                        .eq(GoodsBatch::getGoodsId, goods.getId())
                        .orderByAsc(GoodsBatch::getExpiryDate)
                        .list();
            }
        }

        vo.setGoods(goods);
        vo.setBatches(batches);
        vo.setMatched(goods != null);
        return vo;
    }

    @Override
    public void operate(ScanOperationDTO dto) {
        ScanResolveVO resolveVO = resolve(dto.getCode());
        if (!resolveVO.isMatched() || resolveVO.getGoods() == null) {
            throw new IllegalArgumentException("未识别到条码或 RFID");
        }

        GoodsBatch matchedBatch = resolveVO.getBatches().stream()
                .filter(item -> dto.getCode().equals(item.getBarcode()) || dto.getCode().equals(item.getRfidTag()))
                .findFirst()
                .orElseGet(() -> resolveVO.getBatches().stream()
                        .filter(item -> (item.getQuantity() == null ? 0 : item.getQuantity()) > 0)
                        .min(Comparator.comparing(GoodsBatch::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(GoodsBatch::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(null));

        Record record = new Record();
        record.setGoods(resolveVO.getGoods().getId());
        record.setBatchId(matchedBatch == null ? null : matchedBatch.getId());
        record.setSupplierId(dto.getSupplierId() != null
                ? dto.getSupplierId()
                : matchedBatch == null ? resolveVO.getGoods().getSupplierId() : matchedBatch.getSupplierId());
        record.setUserid(dto.getUserId());
        record.setAdminId(dto.getAdminId());
        record.setCount(dto.getQuantity() == null ? 1 : dto.getQuantity());
        record.setActionType(dto.getActionType());
        record.setRemark(dto.getRemark());
        record.setScanCode(dto.getCode());
        inventoryFlowService.processRecord(record);
    }
}
