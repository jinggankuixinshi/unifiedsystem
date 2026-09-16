package com.unified.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_message")
public class SysMessage extends BaseEntity {

    private String title;
    private String content;
    private Long senderId;
    private Long receiverId;
    private Integer isRead;
    private LocalDateTime readTime;
    private String businessType;
    private Long businessId;
}
