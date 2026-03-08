package com.wms.vo;

import lombok.Data;

@Data
public class TrendPointVO {
    private String day;
    private Integer inboundCount;
    private Integer outboundCount;
}
