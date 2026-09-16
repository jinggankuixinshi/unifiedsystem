package com.unified.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_audit_log")
public class SysAuditLog extends BaseEntity {

    private Long userId;
    private String username;
    private String module;
    private String operation;
    private String method;
    private String params;
    private Long duration;
    private String ip;
    private String result;
}
