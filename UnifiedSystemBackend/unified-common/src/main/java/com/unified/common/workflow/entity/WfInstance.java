package com.unified.common.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_instance")
public class WfInstance extends BaseEntity {

    private Long templateId;
    private Integer branchNo;
    private String businessType;
    private Long businessId;
    private Long applicantId;
    private Long delegateUserId;
    private Integer delegateNodeOrder;
    private Integer currentNodeOrder;
    private String status;
    private LocalDateTime approvalTime;
    private String result;
}
