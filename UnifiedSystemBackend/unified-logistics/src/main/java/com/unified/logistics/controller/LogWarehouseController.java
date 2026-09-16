package com.unified.logistics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unified.common.core.Result;
import com.unified.logistics.entity.LogWarehouse;
import com.unified.logistics.service.LogWarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logistics/warehouse-records")
@RequiredArgsConstructor
public class LogWarehouseController {

    private final LogWarehouseService warehouseService;

    @GetMapping
    public Result<Page<LogWarehouse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(warehouseService.page(new Page<>(page, pageSize)));
    }

    @GetMapping("/{id}")
    public Result<LogWarehouse> getById(@PathVariable Long id) {
        return Result.ok(warehouseService.getById(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody LogWarehouse warehouse) {
        warehouseService.save(warehouse);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody LogWarehouse warehouse) {
        warehouse.setId(id);
        warehouseService.updateById(warehouse);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        warehouseService.removeById(id);
        return Result.ok();
    }
}
