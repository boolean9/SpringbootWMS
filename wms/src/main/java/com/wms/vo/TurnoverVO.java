package com.wms.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TurnoverVO {
    private Integer goodsId;
    private String goodsName;
    private Integer outboundCount;
    private Integer currentStock;
    private BigDecimal turnoverRate;
}
