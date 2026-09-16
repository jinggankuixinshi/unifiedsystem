package com.unified.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("att_leave")
public class AttLeave extends BaseEntity {

    private Long userId;
    private String leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double duration;
    private String reason;
    private Integer approvalStatus;
}
