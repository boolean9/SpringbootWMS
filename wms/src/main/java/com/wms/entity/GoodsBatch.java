package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("goods_batch")
public class GoodsBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("goods_id")
    private Integer goodsId;

    @TableField("storage_id")
    private Integer storageId;

    @TableField("supplier_id")
    private Integer supplierId;

    @TableField("batch_no")
    private String batchNo;

    private String barcode;

    @TableField("rfid_tag")
    private String rfidTag;

    @TableField("production_date")
    private LocalDate productionDate;

    @TableField("expiry_date")
    private LocalDate expiryDate;

    private Integer quantity;

    @TableField("alert_days")
    private Integer alertDays;

    private String status;

    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;
}
