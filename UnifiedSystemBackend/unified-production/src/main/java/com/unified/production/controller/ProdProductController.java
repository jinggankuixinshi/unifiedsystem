package com.unified.production.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unified.common.core.Result;
import com.unified.production.entity.ProdProduct;
import com.unified.production.service.ProdProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production/products")
@RequiredArgsConstructor
public class ProdProductController {

    private final ProdProductService productService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Page<ProdProduct>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<ProdProduct> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ProdProduct::getProductName, keyword)
                   .or().like(ProdProduct::getProductCode, keyword);
        }
        return Result.ok(productService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ProdProduct> getById(@PathVariable Long id) {
        return Result.ok(productService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<?> create(@Valid @RequestBody ProdProduct product) {
        productService.save(product);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody ProdProduct product) {
        product.setId(id);
        productService.updateById(product);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<?> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.ok();
    }
}
