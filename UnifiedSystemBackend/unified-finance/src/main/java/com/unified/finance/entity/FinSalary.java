package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_salary")
public class FinSalary extends BaseEntity {

    private Long userId;
    private String salaryMonth;
    private BigDecimal baseSalary;
    private BigDecimal performanceBonus;
    private BigDecimal overtimePay;
    private BigDecimal deduction;
    private BigDecimal socialInsurance;
    private BigDecimal housingFund;
    private BigDecimal netSalary;
    private Integer status;
}
