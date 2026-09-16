package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_expense")
public class FinExpense extends BaseEntity {

    private String expenseNo;
    private Long applicantId;
    private Long deptId;
    private String expenseType;
    private BigDecimal totalAmount;
    private Integer approvalStatus;
    private String remark;
}
