package com.unified.logistics.dto;

import com.unified.logistics.entity.LogShipping;
import com.unified.logistics.entity.LogShippingItem;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ShippingCreateDTO {

    @NotNull(message = "发货单不能为空")
    private LogShipping shipping;

    private List<LogShippingItem> items;
}
