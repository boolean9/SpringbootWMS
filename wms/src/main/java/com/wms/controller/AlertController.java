package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.InventoryAlert;
import com.wms.service.InventoryAlertService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/alert")
public class AlertController {

    @Resource
    private InventoryAlertService inventoryAlertService;

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        String keyword = (String) param.get("keyword");
        String alertType = (String) param.get("alertType");
        String status = (String) param.get("status");

        Page<InventoryAlert> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<InventoryAlert> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword) && !"null".equals(keyword)) {
            wrapper.and(item -> item.like(InventoryAlert::getGoodsName, keyword)
                    .or().like(InventoryAlert::getContent, keyword)
                    .or().like(InventoryAlert::getBatchNo, keyword));
        }
        if (StringUtils.isNotBlank(alertType) && !"null".equals(alertType)) {
            wrapper.eq(InventoryAlert::getAlertType, alertType);
        }
        if (StringUtils.isNotBlank(status) && !"null".equals(status)) {
            wrapper.eq(InventoryAlert::getStatus, status);
        }
        wrapper.orderByDesc(InventoryAlert::getCreatedAt);

        IPage<InventoryAlert> result = inventoryAlertService.page(page, wrapper);
        return Result.suc(result.getRecords(), result.getTotal());
    }

    @GetMapping("/refresh")
    public Result refresh() {
        inventoryAlertService.refreshAlerts();
        return Result.suc("预警刷新完成");
    }

    @GetMapping("/recent")
    public Result recent() {
        return Result.suc(inventoryAlertService.lambdaQuery().orderByDesc(InventoryAlert::getCreatedAt).last("limit 8").list());
    }

    @GetMapping("/markRead")
    public Result markRead(@RequestParam Integer id) {
        InventoryAlert alert = inventoryAlertService.getById(id);
        if (alert == null) {
            return Result.fail("预警不存在");
        }
        alert.setStatus("PROCESSED");
        return inventoryAlertService.updateById(alert) ? Result.suc() : Result.fail("更新预警失败");
    }
}
