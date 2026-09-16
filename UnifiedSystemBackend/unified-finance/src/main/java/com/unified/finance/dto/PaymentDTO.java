package com.unified.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentDTO {

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private String paymentMethod;
}
