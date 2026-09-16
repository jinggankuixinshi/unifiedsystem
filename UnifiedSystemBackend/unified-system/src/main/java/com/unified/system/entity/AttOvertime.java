package com.unified.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("att_overtime")
public class AttOvertime extends BaseEntity {

    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double duration;
    private String reason;
    private Integer approvalStatus;
}
