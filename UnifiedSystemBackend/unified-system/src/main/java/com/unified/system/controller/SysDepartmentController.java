package com.unified.system.controller;

import com.unified.common.core.Result;
import com.unified.system.dto.SortItemDTO;
import com.unified.system.dto.SysDepartmentDTO;
import com.unified.system.entity.SysDepartment;
import com.unified.system.service.SysDepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/departments")
@RequiredArgsConstructor
public class SysDepartmentController {

    private final SysDepartmentService deptService;

    @GetMapping("/tree")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysDepartment>> tree() {
        return Result.ok(deptService.getDeptTree());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysDepartment>> listAll() {
        return Result.ok(deptService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SysDepartment> getById(@PathVariable Long id) {
        return Result.ok(deptService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<SysDepartment> create(@Valid @RequestBody SysDepartmentDTO dto) {
        return Result.ok(deptService.createDept(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<SysDepartment> update(@PathVariable Long id, @Valid @RequestBody SysDepartmentDTO dto) {
        return Result.ok(deptService.updateDept(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<?> delete(@PathVariable Long id) {
        deptService.deleteDept(id);
        return Result.ok();
    }

    @PutMapping("/sort")
    @PreAuthorize("hasRole('admin')")
    public Result<?> sort(@RequestBody List<SortItemDTO> items) {
        deptService.sortDepts(items);
        return Result.ok();
    }
}
