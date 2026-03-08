package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.InventoryActionUtils;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Record;
import com.wms.service.InventoryFlowService;
import com.wms.service.RecordService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/record")
public class RecordController {

    @Resource
    private RecordService recordService;

    @Resource
    private InventoryFlowService inventoryFlowService;

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        String name = (String) param.get("name");
        String goodstype = (String) param.get("goodstype");
        String storage = (String) param.get("storage");
        String roleId = (String) param.get("roleId");
        String userId = (String) param.get("userId");
        String actionType = (String) param.get("actionType");
        String batchNo = (String) param.get("batchNo");
        String supplierId = (String) param.get("supplierId");

        Page<Record> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        QueryWrapper<Record> wrapper = new QueryWrapper<>();
        if ("2".equals(roleId) && StringUtils.isNotBlank(userId)) {
            wrapper.eq("a.userId", userId);
        }
        if (StringUtils.isNotBlank(name) && !"null".equals(name)) {
            wrapper.like("b.name", name);
        }
        if (StringUtils.isNotBlank(goodstype) && !"null".equals(goodstype)) {
            wrapper.eq("d.id", goodstype);
        }
        if (StringUtils.isNotBlank(storage) && !"null".equals(storage)) {
            wrapper.eq("c.id", storage);
        }
        if (StringUtils.isNotBlank(actionType) && !"null".equals(actionType)) {
            wrapper.apply(
                    "coalesce(a.action_type, case when a.count < 0 then 'OUTBOUND' else 'INBOUND' end) = {0}",
                    InventoryActionUtils.normalize(actionType, actionType)
            );
        }
        if (StringUtils.isNotBlank(batchNo) && !"null".equals(batchNo)) {
            wrapper.like("gb.batch_no", batchNo);
        }
        if (StringUtils.isNotBlank(supplierId) && !"null".equals(supplierId)) {
            wrapper.eq("a.supplier_id", supplierId);
        }
        wrapper.orderByDesc("a.createtime");

        IPage result = recordService.pageCC(page, wrapper);
        return Result.suc(result.getRecords(), result.getTotal());
    }

    @PostMapping("/save")
    public Result save(@RequestBody Record record) {
        try {
            inventoryFlowService.processRecord(record);
            return Result.suc();
        } catch (RuntimeException ex) {
            return Result.fail(ex.getMessage());
        }
    }
}
