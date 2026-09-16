package com.unified.production.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.production.entity.ProdQualityCheck;
import com.unified.production.service.QualityCheckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/production/quality")
@RequiredArgsConstructor
public class ProdQualityController {

    private final QualityCheckService qualityCheckService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<ProdQualityCheck>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String checkType) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        return Result.ok(qualityCheckService.pageChecks(pageNum, pageSize, checkType));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<ProdQualityCheck> check(@Valid @RequestBody ProdQualityCheck check) {
        return Result.ok(qualityCheckService.performCheck(check));
    }
}
