package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_salary_item")
public class FinSalaryItem extends BaseEntity {

    private Long salaryId;
    private String itemName;
    private BigDecimal amount;
    private String itemType;
}
