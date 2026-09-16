package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_supplier")
public class FinSupplier extends BaseEntity {

    private String supplierCode;
    private String supplierName;
    private String contactPerson;
    private String phone;
    private String bankAccount;
    private String bankName;
    private Integer status;
}
