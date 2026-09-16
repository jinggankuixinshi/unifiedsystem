package com.unified.logistics.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.logistics.dto.ShippingCreateDTO;
import com.unified.logistics.entity.*;
import com.unified.logistics.service.PickingService;
import com.unified.logistics.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
public class PickingShippingController {

    private final PickingService pickingService;
    private final ShippingService shippingService;

    @PostMapping("/pickings")
    public Result<LogPicking> createPicking(@RequestParam Long shippingId) {
        return Result.ok(pickingService.createPicking(shippingId));
    }

    @GetMapping("/pickings/{id}/items")
    public Result<List<LogPickingItem>> pickingItems(@PathVariable Long id) {
        return Result.ok(pickingService.getPickingItems(id));
    }

    @GetMapping("/shippings")
    public Result<IPage<LogShipping>> listShipping(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.ok(shippingService.pageShipping(pageNum, pageSize, status));
    }

    @GetMapping("/shippings/{id}")
    public Result<LogShipping> getShipping(@PathVariable Long id) {
        return Result.ok(shippingService.getById(id));
    }

    @GetMapping("/shippings/{id}/items")
    public Result<List<LogShippingItem>> shippingItems(@PathVariable Long id) {
        return Result.ok(shippingService.getItems(id));
    }

    @PostMapping("/shippings")
    public Result<LogShipping> createShipping(@Valid @RequestBody ShippingCreateDTO dto) {
        return Result.ok(shippingService.createShipping(dto.getShipping(), dto.getItems() != null ? dto.getItems() : List.of()));
    }

    @PutMapping("/shippings/{id}/print")
    public Result<?> markPrinted(@PathVariable Long id) {
        shippingService.markPrinted(id);
        return Result.ok();
    }
}
