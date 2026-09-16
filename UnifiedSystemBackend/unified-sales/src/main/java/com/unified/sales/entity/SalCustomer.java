package com.unified.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_customer")
public class SalCustomer extends BaseEntity {

    private String customerCode;
    private String customerName;
    private String contactPerson;
    private String phone;
    private String address;
    private String status;
}
