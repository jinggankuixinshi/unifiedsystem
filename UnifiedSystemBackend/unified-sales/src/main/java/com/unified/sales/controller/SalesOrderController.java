package com.unified.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.sales.entity.SalSalesOrder;
import com.unified.sales.entity.SalSalesOrderItem;
import com.unified.sales.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales/orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService orderService;

    @GetMapping
    public Result<IPage<SalSalesOrder>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.ok(orderService.pageOrders(pageNum, pageSize, status, null));
    }

    @GetMapping("/{id}")
    public Result<SalSalesOrder> getById(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    @GetMapping("/{id}/items")
    public Result<List<SalSalesOrderItem>> getItems(@PathVariable Long id) {
        return Result.ok(orderService.getOrderItems(id));
    }

    @PostMapping
    public Result<SalSalesOrder> create(@RequestBody SalSalesOrder order,
                                         @RequestBody(required = false) List<SalSalesOrderItem> items) {
        return Result.ok(orderService.createOrder(order, items != null ? items : List.of()));
    }
}
