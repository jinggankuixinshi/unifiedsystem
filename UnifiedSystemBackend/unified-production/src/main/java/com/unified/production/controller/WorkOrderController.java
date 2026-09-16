package com.unified.production.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.production.entity.ProdWorkOrder;
import com.unified.production.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/production/orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<ProdWorkOrder>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        return Result.ok(workOrderService.pageOrders(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ProdWorkOrder> getById(@PathVariable Long id) {
        return Result.ok(workOrderService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<ProdWorkOrder> start(@RequestParam Long scheduleId, @Valid @RequestBody ProdWorkOrder order) {
        return Result.ok(workOrderService.startWorkOrder(scheduleId, order));
    }

    @PutMapping("/{id}/finish")
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<ProdWorkOrder> finish(@PathVariable Long id, @RequestParam BigDecimal actualOutput) {
        return Result.ok(workOrderService.finishWorkOrder(id, actualOutput));
    }
}
