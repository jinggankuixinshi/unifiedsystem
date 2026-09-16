package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_receivable_log")
public class FinReceivableLog extends BaseEntity {

    private Long receivableId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String remark;
}
