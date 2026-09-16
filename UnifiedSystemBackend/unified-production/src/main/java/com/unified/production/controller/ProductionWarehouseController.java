package com.unified.production.controller;

import com.unified.common.core.Result;
import com.unified.production.dto.InboundRequest;
import com.unified.production.dto.OutboundRequest;
import com.unified.production.entity.ProdInbound;
import com.unified.production.entity.ProdOutbound;
import com.unified.production.service.ProductionWarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/production/warehouse")
@RequiredArgsConstructor
public class ProductionWarehouseController {

    private final ProductionWarehouseService warehouseService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<?> listAll() {
        return Result.ok(warehouseService.list());
    }

    @PostMapping("/inbound")
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<ProdInbound> inbound(@Valid @RequestBody InboundRequest req) {
        return Result.ok(warehouseService.createInbound(req.getInbound(),
                req.getItems() != null ? req.getItems() : Collections.emptyList()));
    }

    @PostMapping("/outbound")
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<ProdOutbound> outbound(@Valid @RequestBody OutboundRequest req) {
        return Result.ok(warehouseService.createOutbound(req.getOutbound(),
                req.getItems() != null ? req.getItems() : Collections.emptyList()));
    }
}
