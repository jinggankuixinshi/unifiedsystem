package com.unified.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysMessageVO {

    private Long id;
    private String title;
    private String content;
    private Long senderId;
    private String senderName;
    private Integer isRead;
    private String businessType;
    private Long businessId;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
