package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_voucher")
public class FinVoucher extends BaseEntity {

    private String voucherNo;
    private LocalDate voucherDate;
    private String summary;
    private Long creatorId;
    private Long reviewerId;
    private Integer status;
}
