package com.wms.service;

import com.wms.vo.ReportOverviewVO;
import com.wms.vo.SalesStatVO;
import com.wms.vo.TrendPointVO;
import com.wms.vo.TurnoverVO;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public interface ReportService {
    ReportOverviewVO buildOverview();

    List<TrendPointVO> buildInOutTrend(int days);

    List<TurnoverVO> buildTurnoverReport();

    List<SalesStatVO> buildSalesRanking();

    void exportExcel(HttpServletResponse response) throws IOException;

    void exportPdf(HttpServletResponse response) throws IOException;
}
