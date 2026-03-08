package com.wms.controller;

import com.wms.common.Result;
import com.wms.service.ReportService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    @GetMapping("/overview")
    public Result overview() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("overview", reportService.buildOverview());
        payload.put("trend", reportService.buildInOutTrend(7));
        payload.put("turnover", reportService.buildTurnoverReport());
        payload.put("sales", reportService.buildSalesRanking());
        return Result.suc(payload);
    }

    @GetMapping("/trend")
    public Result trend(@RequestParam(defaultValue = "7") int days) {
        return Result.suc(reportService.buildInOutTrend(days));
    }

    @GetMapping("/turnover")
    public Result turnover() {
        return Result.suc(reportService.buildTurnoverReport());
    }

    @GetMapping("/sales")
    public Result sales() {
        return Result.suc(reportService.buildSalesRanking());
    }

    @GetMapping("/export/excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        reportService.exportExcel(response);
    }

    @GetMapping("/export/pdf")
    public void exportPdf(HttpServletResponse response) throws IOException {
        reportService.exportPdf(response);
    }
}
