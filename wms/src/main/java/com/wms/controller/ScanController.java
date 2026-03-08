package com.wms.controller;

import com.wms.common.Result;
import com.wms.dto.ScanOperationDTO;
import com.wms.service.ScanService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scan")
public class ScanController {

    @Resource
    private ScanService scanService;

    @GetMapping("/resolve")
    public Result resolve(@RequestParam String code) {
        return Result.suc(scanService.resolve(code));
    }

    @PostMapping("/operate")
    public Result operate(@RequestBody ScanOperationDTO dto) {
        try {
            scanService.operate(dto);
            return Result.suc();
        } catch (RuntimeException ex) {
            return Result.fail(ex.getMessage());
        }
    }
}
