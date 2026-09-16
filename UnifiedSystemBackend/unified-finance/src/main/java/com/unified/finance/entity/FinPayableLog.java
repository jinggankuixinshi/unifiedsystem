package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_payable_log")
public class FinPayableLog extends BaseEntity {

    private Long payableId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String remark;
}
