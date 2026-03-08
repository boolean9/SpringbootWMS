package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    private Integer storage;

    @TableField("goodsType")
    private Integer goodstype;

    private Integer count;

    private String remark;

    @TableField(exist = false)
    private String barcode;

    @TableField(exist = false)
    private String rfidTag;

    @TableField(exist = false)
    private Integer minStock;

    @TableField(exist = false)
    private Integer maxStock;

    @TableField(exist = false)
    private Integer supplierId;
}
