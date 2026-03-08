package com.wms.vo;

import lombok.Data;

@Data
public class ReportOverviewVO {
    private Integer goodsSkuCount;
    private Integer totalStockCount;
    private Integer lowStockCount;
    private Integer highStockCount;
    private Integer expiringBatchCount;
    private Integer activeTransferCount;
}
