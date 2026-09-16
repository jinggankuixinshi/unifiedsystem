package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_receivable")
public class FinReceivable extends BaseEntity {

    private Long customerId;
    private Long salesOrderId;
    private BigDecimal amount;
    private BigDecimal receivedAmount;
    private BigDecimal balance;
    private LocalDate dueDate;
    private Integer status;
}
