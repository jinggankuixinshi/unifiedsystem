package com.unified.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttLeaveDTO {

    @NotBlank(message = "请假类型不能为空")
    private String leaveType;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private String reason;

    @NotNull(message = "请假时长不能为空")
    private Double duration;
}
