package com.unified.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WfRecordVO {

    private Long id;
    private Long instanceId;
    private Integer nodeOrder;
    private Long approverId;
    private String approverName;
    private String approverAction;
    private String comment;
    private LocalDateTime actionTime;
}
