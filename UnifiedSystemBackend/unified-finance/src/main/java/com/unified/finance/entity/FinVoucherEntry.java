package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_voucher_entry")
public class FinVoucherEntry extends BaseEntity {

    private Long voucherId;
    private Long subjectId;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String summary;
}
