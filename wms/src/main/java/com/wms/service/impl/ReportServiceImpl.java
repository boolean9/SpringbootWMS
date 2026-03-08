package com.wms.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.wms.common.InventoryActionUtils;
import com.wms.entity.Goods;
import com.wms.entity.Record;
import com.wms.entity.WarehouseTransfer;
import com.wms.service.GoodsService;
import com.wms.service.InventoryAlertService;
import com.wms.service.RecordService;
import com.wms.service.ReportService;
import com.wms.service.WarehouseTransferService;
import com.wms.vo.ReportOverviewVO;
import com.wms.vo.SalesStatVO;
import com.wms.vo.TrendPointVO;
import com.wms.vo.TurnoverVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Resource
    private GoodsService goodsService;

    @Resource
    private RecordService recordService;

    @Resource
    private InventoryAlertService inventoryAlertService;

    @Resource
    private WarehouseTransferService warehouseTransferService;

    @Override
    public ReportOverviewVO buildOverview() {
        ReportOverviewVO overview = new ReportOverviewVO();
        List<Goods> goodsList = goodsService.list();
        overview.setGoodsSkuCount(goodsList.size());
        overview.setTotalStockCount(goodsList.stream().mapToInt(item -> item.getCount() == null ? 0 : item.getCount()).sum());
        overview.setLowStockCount(inventoryAlertService.lambdaQuery()
                .eq(com.wms.entity.InventoryAlert::getAlertType, "LOW_STOCK")
                .ne(com.wms.entity.InventoryAlert::getStatus, "RESOLVED")
                .count().intValue());
        overview.setHighStockCount(inventoryAlertService.lambdaQuery()
                .eq(com.wms.entity.InventoryAlert::getAlertType, "HIGH_STOCK")
                .ne(com.wms.entity.InventoryAlert::getStatus, "RESOLVED")
                .count().intValue());
        overview.setExpiringBatchCount(inventoryAlertService.lambdaQuery()
                .in(com.wms.entity.InventoryAlert::getAlertType, List.of("EXPIRY", "EXPIRED"))
                .ne(com.wms.entity.InventoryAlert::getStatus, "RESOLVED")
                .count().intValue());
        overview.setActiveTransferCount(warehouseTransferService.lambdaQuery()
                .ne(WarehouseTransfer::getStatus, "COMPLETED")
                .count().intValue());
        return overview;
    }

    @Override
    public List<TrendPointVO> buildInOutTrend(int days) {
        int range = Math.max(days, 1);
        LocalDate startDate = LocalDate.now().minusDays(range - 1L);
        Map<LocalDate, TrendPointVO> trendMap = new LinkedHashMap<>();
        for (int i = 0; i < range; i++) {
            LocalDate day = startDate.plusDays(i);
            TrendPointVO point = new TrendPointVO();
            point.setDay(day.format(DAY_FORMATTER));
            point.setInboundCount(0);
            point.setOutboundCount(0);
            trendMap.put(day, point);
        }

        List<Record> records = recordService.lambdaQuery()
                .ge(Record::getCreatetime, startDate.atStartOfDay())
                .list();
        for (Record record : records) {
            if (record.getCreatetime() == null) {
                continue;
            }
            LocalDate day = record.getCreatetime().toLocalDate();
            TrendPointVO point = trendMap.get(day);
            if (point == null) {
                continue;
            }
            int quantity = Math.abs(record.getCount() == null ? 0 : record.getCount());
            String actionType = InventoryActionUtils.normalize(record.getActionType(), record.getAction());
            if (InventoryActionUtils.isOutbound(actionType)) {
                point.setOutboundCount(point.getOutboundCount() + quantity);
            } else {
                point.setInboundCount(point.getInboundCount() + quantity);
            }
        }

        return new ArrayList<>(trendMap.values());
    }

    @Override
    public List<TurnoverVO> buildTurnoverReport() {
        Map<Integer, Integer> outboundMap = buildRecords().stream()
                .filter(record -> InventoryActionUtils.isOutbound(InventoryActionUtils.normalize(record.getActionType(), record.getAction())))
                .collect(Collectors.groupingBy(
                        Record::getGoods,
                        Collectors.summingInt(record -> Math.abs(record.getCount() == null ? 0 : record.getCount()))
                ));

        return goodsService.list().stream()
                .map(goods -> {
                    int outbound = outboundMap.getOrDefault(goods.getId(), 0);
                    int currentStock = goods.getCount() == null ? 0 : goods.getCount();
                    BigDecimal averageStock = BigDecimal.valueOf(Math.max((currentStock + outbound) / 2.0, 1));
                    BigDecimal turnover = BigDecimal.valueOf(outbound)
                            .divide(averageStock, 2, RoundingMode.HALF_UP);
                    TurnoverVO vo = new TurnoverVO();
                    vo.setGoodsId(goods.getId());
                    vo.setGoodsName(goods.getName());
                    vo.setOutboundCount(outbound);
                    vo.setCurrentStock(currentStock);
                    vo.setTurnoverRate(turnover);
                    return vo;
                })
                .sorted(Comparator.comparing(TurnoverVO::getTurnoverRate).reversed())
                .toList();
    }

    @Override
    public List<SalesStatVO> buildSalesRanking() {
        Map<Integer, Integer> saleMap = buildRecords().stream()
                .filter(record -> "OUTBOUND".equals(InventoryActionUtils.normalize(record.getActionType(), record.getAction())))
                .collect(Collectors.groupingBy(
                        Record::getGoods,
                        Collectors.summingInt(record -> Math.abs(record.getCount() == null ? 0 : record.getCount()))
                ));

        Map<Integer, Goods> goodsMap = goodsService.list().stream()
                .collect(Collectors.toMap(Goods::getId, item -> item));

        return saleMap.entrySet().stream()
                .map(entry -> {
                    SalesStatVO vo = new SalesStatVO();
                    vo.setGoodsId(entry.getKey());
                    Goods goods = goodsMap.get(entry.getKey());
                    vo.setGoodsName(goods == null ? "-" : goods.getName());
                    vo.setSaleCount(entry.getValue());
                    return vo;
                })
                .sorted(Comparator.comparing(SalesStatVO::getSaleCount).reversed())
                .toList();
    }

    @Override
    public void exportExcel(HttpServletResponse response) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            writeOverviewSheet(workbook);
            writeTrendSheet(workbook);
            writeTurnoverSheet(workbook);
            writeSalesSheet(workbook);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=wms-report.xlsx");
            workbook.write(response.getOutputStream());
        }
    }

    @Override
    public void exportPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=wms-report.pdf");

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            document.add(new Paragraph("WMS Analytics Report"));
            document.add(new Paragraph("Generated at: " + java.time.LocalDateTime.now()));
            document.add(new Paragraph(" "));

            ReportOverviewVO overview = buildOverview();
            document.add(new Paragraph("Overview"));
            PdfPTable overviewTable = new PdfPTable(2);
            addOverviewCell(overviewTable, "Goods SKU", String.valueOf(overview.getGoodsSkuCount()));
            addOverviewCell(overviewTable, "Total Stock", String.valueOf(overview.getTotalStockCount()));
            addOverviewCell(overviewTable, "Low Stock Alerts", String.valueOf(overview.getLowStockCount()));
            addOverviewCell(overviewTable, "High Stock Alerts", String.valueOf(overview.getHighStockCount()));
            addOverviewCell(overviewTable, "Expiring Batches", String.valueOf(overview.getExpiringBatchCount()));
            addOverviewCell(overviewTable, "Pending Transfers", String.valueOf(overview.getActiveTransferCount()));
            document.add(overviewTable);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Top Sales"));
            PdfPTable salesTable = new PdfPTable(3);
            addHeaderCells(salesTable, "Rank", "Goods", "Outbound Qty");
            List<SalesStatVO> ranking = buildSalesRanking();
            for (int i = 0; i < Math.min(ranking.size(), 10); i++) {
                SalesStatVO item = ranking.get(i);
                salesTable.addCell(String.valueOf(i + 1));
                salesTable.addCell(item.getGoodsName());
                salesTable.addCell(String.valueOf(item.getSaleCount()));
            }
            document.add(salesTable);
        } catch (DocumentException ex) {
            throw new IOException(ex);
        } finally {
            document.close();
        }
    }

    private List<Record> buildRecords() {
        return recordService.list();
    }

    private void writeOverviewSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("Overview");
        ReportOverviewVO overview = buildOverview();
        String[][] rows = {
                {"Goods SKU", String.valueOf(overview.getGoodsSkuCount())},
                {"Total Stock", String.valueOf(overview.getTotalStockCount())},
                {"Low Stock Alerts", String.valueOf(overview.getLowStockCount())},
                {"High Stock Alerts", String.valueOf(overview.getHighStockCount())},
                {"Expiring Batches", String.valueOf(overview.getExpiringBatchCount())},
                {"Pending Transfers", String.valueOf(overview.getActiveTransferCount())}
        };
        for (int i = 0; i < rows.length; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(rows[i][0]);
            row.createCell(1).setCellValue(rows[i][1]);
        }
    }

    private void writeTrendSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("InOutTrend");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Day");
        header.createCell(1).setCellValue("Inbound");
        header.createCell(2).setCellValue("Outbound");
        List<TrendPointVO> trend = buildInOutTrend(14);
        for (int i = 0; i < trend.size(); i++) {
            TrendPointVO item = trend.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(item.getDay());
            row.createCell(1).setCellValue(item.getInboundCount());
            row.createCell(2).setCellValue(item.getOutboundCount());
        }
    }

    private void writeTurnoverSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("Turnover");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Goods");
        header.createCell(1).setCellValue("Outbound");
        header.createCell(2).setCellValue("Current Stock");
        header.createCell(3).setCellValue("Turnover Rate");
        List<TurnoverVO> turnoverList = buildTurnoverReport();
        for (int i = 0; i < turnoverList.size(); i++) {
            TurnoverVO item = turnoverList.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(item.getGoodsName());
            row.createCell(1).setCellValue(item.getOutboundCount());
            row.createCell(2).setCellValue(item.getCurrentStock());
            row.createCell(3).setCellValue(item.getTurnoverRate().doubleValue());
        }
    }

    private void writeSalesSheet(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet("SalesRanking");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Goods");
        header.createCell(1).setCellValue("Outbound Qty");
        List<SalesStatVO> sales = buildSalesRanking();
        for (int i = 0; i < sales.size(); i++) {
            SalesStatVO item = sales.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(item.getGoodsName());
            row.createCell(1).setCellValue(item.getSaleCount());
        }
    }

    private void addOverviewCell(PdfPTable table, String label, String value) {
        table.addCell(label);
        table.addCell(value);
    }

    private void addHeaderCells(PdfPTable table, String... titles) {
        for (String title : titles) {
            PdfPCell cell = new PdfPCell();
            cell.setPhrase(new Paragraph(title));
            table.addCell(cell);
        }
    }
}
