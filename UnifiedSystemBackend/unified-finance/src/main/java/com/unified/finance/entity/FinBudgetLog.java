package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_budget_log")
public class FinBudgetLog extends BaseEntity {

    private Long budgetId;
    private BigDecimal amount;
    private Long businessId;
    private String businessType;
    private String remark;
}
