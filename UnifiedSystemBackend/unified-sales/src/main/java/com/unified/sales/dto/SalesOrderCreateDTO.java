package com.unified.sales.dto;

import com.unified.sales.entity.SalSalesOrder;
import com.unified.sales.entity.SalSalesOrderItem;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SalesOrderCreateDTO {

    @NotNull(message = "报单信息不能为空")
    private SalSalesOrder order;

    private List<SalSalesOrderItem> items;
}
