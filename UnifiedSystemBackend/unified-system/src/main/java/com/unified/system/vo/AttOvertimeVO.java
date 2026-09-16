package com.unified.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttOvertimeVO {

    private Long id;
    private Long userId;
    private String realName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private Double duration;
    private Integer approvalStatus;
    private LocalDateTime createTime;
}
