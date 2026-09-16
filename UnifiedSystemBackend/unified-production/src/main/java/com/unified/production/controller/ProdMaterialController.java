package com.unified.production.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unified.common.core.Result;
import com.unified.production.entity.ProdMaterial;
import com.unified.production.service.ProdMaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production/materials")
@RequiredArgsConstructor
public class ProdMaterialController {

    private final ProdMaterialService materialService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Page<ProdMaterial>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<ProdMaterial> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ProdMaterial::getMaterialName, keyword)
                   .or().like(ProdMaterial::getMaterialCode, keyword);
        }
        return Result.ok(materialService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ProdMaterial> getById(@PathVariable Long id) {
        return Result.ok(materialService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<?> create(@Valid @RequestBody ProdMaterial material) {
        materialService.save(material);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody ProdMaterial material) {
        material.setId(id);
        materialService.updateById(material);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<?> delete(@PathVariable Long id) {
        materialService.removeById(id);
        return Result.ok();
    }
}
