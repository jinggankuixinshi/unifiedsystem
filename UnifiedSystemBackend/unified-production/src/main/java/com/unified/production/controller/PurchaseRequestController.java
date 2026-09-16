package com.unified.production.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.production.dto.PurchaseRequestDTO;
import com.unified.production.entity.ProdPurchaseRequest;
import com.unified.production.entity.ProdPurchaseRequestItem;
import com.unified.production.service.PurchaseRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/production/purchase")
@RequiredArgsConstructor
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<ProdPurchaseRequest>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        return Result.ok(purchaseRequestService.pageRequests(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ProdPurchaseRequest> getById(@PathVariable Long id) {
        return Result.ok(purchaseRequestService.getById(id));
    }

    @GetMapping("/{id}/items")
    @PreAuthorize("isAuthenticated()")
    public Result<List<ProdPurchaseRequestItem>> getItems(@PathVariable Long id) {
        return Result.ok(purchaseRequestService.getRequestItems(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<ProdPurchaseRequest> create(@Valid @RequestBody PurchaseRequestDTO dto) {
        List<ProdPurchaseRequestItem> items = dto.getItems() != null ? dto.getItems() : Collections.emptyList();
        return Result.ok(purchaseRequestService.createRequest(dto.getRequest(), items));
    }
}
