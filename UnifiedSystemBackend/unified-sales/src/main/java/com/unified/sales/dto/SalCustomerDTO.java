package com.unified.sales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SalCustomerDTO {

    @NotBlank(message = "客户编码不能为空")
    private String customerCode;

    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    private String contactPerson;
    private String contactPhone;
    private String address;
    private Integer status;
}
