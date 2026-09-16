package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_expense_item")
public class FinExpenseItem extends BaseEntity {

    private Long expenseId;
    private String itemName;
    private BigDecimal amount;
    private String remark;
}
