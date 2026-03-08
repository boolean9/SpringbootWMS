package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.Storage;
import com.wms.entity.WarehouseTransfer;
import com.wms.service.GoodsService;
import com.wms.service.InventoryFlowService;
import com.wms.service.StorageService;
import com.wms.service.WarehouseTransferService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;

@RestController
@RequestMapping("/transfer")
public class TransferController {

    @Resource
    private WarehouseTransferService warehouseTransferService;

    @Resource
    private GoodsService goodsService;

    @Resource
    private StorageService storageService;

    @Resource
    private InventoryFlowService inventoryFlowService;

    @PostMapping("/save")
    public Result save(@RequestBody WarehouseTransfer transfer) {
        try {
            Goods goods = goodsService.getById(transfer.getGoodsId());
            if (goods == null) {
                return Result.fail("货品不存在");
            }
            Storage fromStorage = storageService.getById(transfer.getFromStorageId());
            Storage toStorage = storageService.getById(transfer.getToStorageId());
            if (fromStorage == null || toStorage == null) {
                return Result.fail("仓库不存在");
            }
            transfer.setGoodsName(goods.getName());
            transfer.setFromStorageName(fromStorage.getName());
            transfer.setToStorageName(toStorage.getName());
            transfer.setStatus("PENDING");
            transfer.setCreateTime(LocalDateTime.now());
            return warehouseTransferService.save(transfer) ? Result.suc() : Result.fail("创建调拨单失败");
        } catch (Exception ex) {
            return Result.fail("创建调拨单失败: " + ex.getMessage());
        }
    }

    @PostMapping("/update")
    public Result update(@RequestBody WarehouseTransfer transfer) {
        try {
            return warehouseTransferService.updateById(transfer) ? Result.suc() : Result.fail("更新调拨单失败");
        } catch (Exception ex) {
            return Result.fail("更新调拨单失败: " + ex.getMessage());
        }
    }

    @PostMapping("/execute")
    public Result execute(@RequestBody WarehouseTransfer transfer) {
        try {
            inventoryFlowService.executeTransfer(transfer);
            return Result.suc();
        } catch (RuntimeException ex) {
            return Result.fail(ex.getMessage());
        }
    }

    @GetMapping("/del")
    public Result del(@RequestParam Integer id) {
        try {
            WarehouseTransfer transfer = warehouseTransferService.getById(id);
            if (transfer == null) {
                return Result.fail("调拨单不存在");
            }
            if ("COMPLETED".equalsIgnoreCase(transfer.getStatus())) {
                return Result.fail("已执行的调拨单不允许删除");
            }
            return warehouseTransferService.removeById(id) ? Result.suc() : Result.fail("删除调拨单失败");
        } catch (Exception ex) {
            return Result.fail("删除调拨单失败: " + ex.getMessage());
        }
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        try {
            HashMap param = query.getParam();
            String keyword = (String) param.get("keyword");
            String status = (String) param.get("status");
            String fromStorageId = (String) param.get("fromStorageId");
            String toStorageId = (String) param.get("toStorageId");

            Page<WarehouseTransfer> page = new Page<>();
            page.setCurrent(query.getPageNum());
            page.setSize(query.getPageSize());

            LambdaQueryWrapper<WarehouseTransfer> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.isNotBlank(keyword) && !"null".equals(keyword)) {
                wrapper.like(WarehouseTransfer::getGoodsName, keyword);
            }
            if (StringUtils.isNotBlank(status) && !"null".equals(status)) {
                wrapper.eq(WarehouseTransfer::getStatus, status);
            }
            if (StringUtils.isNotBlank(fromStorageId) && !"null".equals(fromStorageId)) {
                wrapper.eq(WarehouseTransfer::getFromStorageId, Integer.parseInt(fromStorageId));
            }
            if (StringUtils.isNotBlank(toStorageId) && !"null".equals(toStorageId)) {
                wrapper.eq(WarehouseTransfer::getToStorageId, Integer.parseInt(toStorageId));
            }
            wrapper.orderByDesc(WarehouseTransfer::getCreateTime).orderByDesc(WarehouseTransfer::getId);

            IPage<WarehouseTransfer> result = warehouseTransferService.page(page, wrapper);
            return Result.suc(result.getRecords(), result.getTotal());
        } catch (Exception ex) {
            return Result.fail("获取调拨列表失败: " + ex.getMessage());
        }
    }
}
