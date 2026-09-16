package com.unified.production.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.production.entity.ProdProductionPlan;
import com.unified.production.entity.ProdProductionSchedule;
import com.unified.production.service.ProductionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/production/plans")
@RequiredArgsConstructor
public class ProductionPlanController {

    private final ProductionPlanService planService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<ProdProductionPlan>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        return Result.ok(planService.pagePlans(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<ProdProductionPlan> getById(@PathVariable Long id) {
        return Result.ok(planService.getById(id));
    }

    @GetMapping("/{id}/schedules")
    @PreAuthorize("isAuthenticated()")
    public Result<List<ProdProductionSchedule>> getSchedules(@PathVariable Long id) {
        return Result.ok(planService.getSchedules(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<ProdProductionPlan> create(@Valid @RequestBody ProdProductionPlan plan) {
        return Result.ok(planService.createPlan(plan));
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('admin', 'production')")
    public Result<ProdProductionSchedule> schedule(@Valid @RequestBody ProdProductionSchedule schedule) {
        return Result.ok(planService.scheduleProduction(schedule));
    }
}
