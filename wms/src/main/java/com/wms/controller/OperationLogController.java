package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.OperationLog;
import com.wms.service.OperationLogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/operationLog")
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        String keyword = (String) param.get("keyword");
        String moduleName = (String) param.get("moduleName");
        String success = (String) param.get("success");

        Page<OperationLog> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword) && !"null".equals(keyword)) {
            wrapper.and(item -> item.like(OperationLog::getOperatorName, keyword)
                    .or().like(OperationLog::getRequestPath, keyword)
                    .or().like(OperationLog::getMessage, keyword));
        }
        if (StringUtils.isNotBlank(moduleName) && !"null".equals(moduleName)) {
            wrapper.eq(OperationLog::getModuleName, moduleName);
        }
        if (StringUtils.isNotBlank(success) && !"null".equals(success)) {
            wrapper.eq(OperationLog::getSuccess, Boolean.parseBoolean(success));
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        IPage<OperationLog> result = operationLogService.page(page, wrapper);
        return Result.suc(result.getRecords(), result.getTotal());
    }
}
