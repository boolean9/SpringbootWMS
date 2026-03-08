package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Supplier;
import com.wms.service.SupplierService;
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
@RequestMapping("/supplier")
public class SupplierController {

    @Resource
    private SupplierService supplierService;

    @PostMapping("/save")
    public Result save(@RequestBody Supplier supplier) {
        if (StringUtils.isBlank(supplier.getCode()) || StringUtils.isBlank(supplier.getName())) {
            return Result.fail("供应商编码和名称不能为空");
        }
        if (StringUtils.isBlank(supplier.getStatus())) {
            supplier.setStatus("ACTIVE");
        }
        if (supplier.getCreateTime() == null) {
            supplier.setCreateTime(LocalDateTime.now());
        }
        return supplierService.save(supplier) ? Result.suc() : Result.fail("保存供应商失败");
    }

    @PostMapping("/update")
    public Result update(@RequestBody Supplier supplier) {
        return supplierService.updateById(supplier) ? Result.suc() : Result.fail("更新供应商失败");
    }

    @GetMapping("/del")
    public Result del(@RequestParam String id) {
        return supplierService.removeById(id) ? Result.suc() : Result.fail("删除供应商失败");
    }

    @GetMapping("/list")
    public Result list() {
        try {
            return Result.suc(supplierService.lambdaQuery().orderByDesc(Supplier::getId).list());
        } catch (Exception ex) {
            return Result.fail("获取供应商列表失败: " + ex.getMessage());
        }
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        String keyword = (String) param.get("keyword");
        String status = (String) param.get("status");

        Page<Supplier> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword) && !"null".equals(keyword)) {
            wrapper.and(item -> item.like(Supplier::getCode, keyword)
                    .or().like(Supplier::getName, keyword)
                    .or().like(Supplier::getContactPerson, keyword));
        }
        if (StringUtils.isNotBlank(status) && !"null".equals(status)) {
            wrapper.eq(Supplier::getStatus, status);
        }
        wrapper.orderByDesc(Supplier::getId);

        IPage<Supplier> result = supplierService.page(page, wrapper);
        return Result.suc(result.getRecords(), result.getTotal());
    }
}
