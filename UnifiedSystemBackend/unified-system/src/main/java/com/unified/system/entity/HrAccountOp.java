package com.unified.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hr_account_op")
public class HrAccountOp extends BaseEntity {

    private String opNo;
    private String opType;
    private Long targetUserId;
    private String payload;
    private Long applicantId;
    private Integer approvalStatus;
    private Integer executed;
    private String execMessage;
}
