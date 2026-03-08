package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("warehouse_transfer")
public class WarehouseTransfer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("goods_id")
    private Integer goodsId;

    @TableField("goods_name")
    private String goodsName;

    @TableField("batch_id")
    private Integer batchId;

    @TableField("batch_no")
    private String batchNo;

    @TableField("from_storage_id")
    private Integer fromStorageId;

    @TableField("from_storage_name")
    private String fromStorageName;

    @TableField("to_storage_id")
    private Integer toStorageId;

    @TableField("to_storage_name")
    private String toStorageName;

    private Integer quantity;

    private String status;

    private String reason;

    @TableField("operator_id")
    private Integer operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("execute_time")
    private LocalDateTime executeTime;
}
