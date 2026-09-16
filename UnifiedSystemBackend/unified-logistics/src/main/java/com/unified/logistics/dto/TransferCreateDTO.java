package com.unified.logistics.dto;

import com.unified.logistics.entity.LogTransfer;
import com.unified.logistics.entity.LogTransferItem;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TransferCreateDTO {

    @NotNull(message = "调拨单不能为空")
    private LogTransfer transfer;

    private List<LogTransferItem> items;
}
