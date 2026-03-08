package com.wms.dto;

import lombok.Data;

@Data
public class ScanOperationDTO {
    private String code;
    private Integer quantity;
    private Integer userId;
    private Integer adminId;
    private Integer supplierId;
    private String actionType;
    private String remark;
}
