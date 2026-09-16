package com.unified.logistics.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.logistics.entity.*;
import com.unified.logistics.service.LogisticsWarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logistics/warehouse")
@RequiredArgsConstructor
public class LogisticsWarehouseController {

    private final LogisticsWarehouseService warehouseService;

    @GetMapping
    public Result<IPage<LogWarehouse>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(warehouseService.pageStock(pageNum, pageSize));
    }

    @PostMapping("/inbound")
    public Result<LogInbound> inbound(@RequestBody LogInbound inbound,
                                       @RequestParam(required = false) List<LogInboundItem> items) {
        return Result.ok(warehouseService.createInbound(inbound, items != null ? items : List.of()));
    }

    @PostMapping("/outbound")
    public Result<LogOutbound> outbound(@RequestBody LogOutbound outbound,
                                         @RequestParam(required = false) List<LogOutboundItem> items) {
        return Result.ok(warehouseService.createOutbound(outbound, items != null ? items : List.of()));
    }

    @PostMapping("/quality-check")
    public Result<LogQualityCheck> qualityCheck(@RequestBody LogQualityCheck check) {
        return Result.ok(warehouseService.createQualityCheck(check));
    }
}
