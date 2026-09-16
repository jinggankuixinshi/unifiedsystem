package com.unified.production.controller;

import com.unified.common.core.Result;
import com.unified.production.dto.BomRequest;
import com.unified.production.entity.ProdBom;
import com.unified.production.entity.ProdBomItem;
import com.unified.production.service.BomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/production/bom")
@RequiredArgsConstructor
public class BomController {

    private final BomService bomService;

    @GetMapping("/product/{productId}")
    @PreAuthorize("isAuthenticated()")
    public Result<ProdBom> getActive(@PathVariable Long productId) {
        return Result.ok(bomService.getActiveByProduct(productId));
    }

    @GetMapping("/{bomId}/items")
    @PreAuthorize("isAuthenticated()")
    public Result<List<ProdBomItem>> getItems(@PathVariable Long bomId) {
        return Result.ok(bomService.getBomItems(bomId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<ProdBom> create(@Valid @RequestBody BomRequest request) {
        List<ProdBomItem> items = request.getItems() != null ? request.getItems() : Collections.emptyList();
        return Result.ok(bomService.createBom(request.getBom(), items));
    }
}
