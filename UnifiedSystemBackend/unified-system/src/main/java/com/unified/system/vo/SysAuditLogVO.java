package com.unified.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysAuditLogVO {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String operation;
    private String module;
    private String target;
    private String detail;
    private String ip;
    private LocalDateTime createTime;
}
