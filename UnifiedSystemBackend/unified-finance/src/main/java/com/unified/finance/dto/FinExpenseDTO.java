package com.unified.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinExpenseDTO {

    @NotBlank(message = "报销类型不能为空")
    private String expenseType;

    @NotBlank(message = "报销事由不能为空")
    private String reason;

    @NotNull(message = "报销金额不能为空")
    @Positive(message = "报销金额必须大于0")
    private BigDecimal totalAmount;
}
