package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("inventory_alert")
public class InventoryAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("source_key")
    private String sourceKey;

    @TableField("goods_id")
    private Integer goodsId;

    @TableField("goods_name")
    private String goodsName;

    @TableField("storage_id")
    private Integer storageId;

    @TableField("storage_name")
    private String storageName;

    @TableField("batch_id")
    private Integer batchId;

    @TableField("batch_no")
    private String batchNo;

    @TableField("alert_type")
    private String alertType;

    @TableField("alert_level")
    private String alertLevel;

    private String content;

    @TableField("current_value")
    private BigDecimal currentValue;

    @TableField("threshold_value")
    private BigDecimal thresholdValue;

    private String status;

    @TableField("notify_channels")
    private String notifyChannels;

    private Boolean notified;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
