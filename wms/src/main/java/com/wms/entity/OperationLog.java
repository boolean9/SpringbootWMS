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
@TableName("operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("operator_id")
    private Integer operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("operator_no")
    private String operatorNo;

    @TableField("role_id")
    private Integer roleId;

    @TableField("module_name")
    private String moduleName;

    @TableField("action_name")
    private String actionName;

    @TableField("request_method")
    private String requestMethod;

    @TableField("request_path")
    private String requestPath;

    @TableField("request_params")
    private String requestParams;

    private Boolean success;

    private String message;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("create_time")
    private LocalDateTime createTime;
}
