package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_account")
public class FinAccount extends BaseEntity {

    private String accountName;
    private String accountNo;
    private String bankName;
    private String accountType;
    private BigDecimal balance;
    private Integer status;
}
