package com.unified.logistics.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.logistics.dto.TransferCreateDTO;
import com.unified.logistics.entity.LogTransfer;
import com.unified.logistics.entity.LogTransferItem;
import com.unified.logistics.entity.LogTransferSign;
import com.unified.logistics.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistics/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @GetMapping
    public Result<IPage<LogTransfer>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        return Result.ok(transferService.pageTransfers(pageNum, pageSize, type, status));
    }

    @GetMapping("/{id}")
    public Result<LogTransfer> getById(@PathVariable Long id) {
        return Result.ok(transferService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<LogTransferItem>> getItems(@PathVariable Long id) {
        return Result.ok(transferService.getItems(id));
    }

    @PostMapping
    public Result<LogTransfer> create(@Valid @RequestBody TransferCreateDTO dto) {
        return Result.ok(transferService.createTransfer(dto.getTransfer(), dto.getItems() != null ? dto.getItems() : List.of()));
    }

    @PostMapping("/{id}/sign")
    public Result<LogTransferSign> sign(@PathVariable Long id) {
        return Result.ok(transferService.signTransfer(id));
    }
}
