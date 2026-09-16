package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_budget")
public class FinBudget extends BaseEntity {

    private Long deptId;
    private Long subjectId;
    private Integer budgetYear;
    private BigDecimal budgetAmount;
    private BigDecimal executedAmount;
}
