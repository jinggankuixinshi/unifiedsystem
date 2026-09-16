package com.unified.common.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_record")
public class WfRecord extends BaseEntity {

    private Long instanceId;
    private Integer nodeOrder;
    private Long approverId;
    private String approverAction;
    private String comment;
    private LocalDateTime actionTime;
}
