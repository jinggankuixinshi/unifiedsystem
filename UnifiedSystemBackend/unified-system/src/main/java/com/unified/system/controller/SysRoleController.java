package com.unified.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.system.dto.SortItemDTO;
import com.unified.system.dto.SysRoleDTO;
import com.unified.system.entity.SysRole;
import com.unified.system.service.SysRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<SysRole>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.ok(roleService.pageRoles(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public Result<?> listAll() {
        return Result.ok(roleService.list());
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<SysRole> create(@Valid @RequestBody SysRoleDTO dto) {
        return Result.ok(roleService.createRole(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<SysRole> update(@PathVariable Long id, @Valid @RequestBody SysRoleDTO dto) {
        return Result.ok(roleService.updateRole(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<?> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.ok();
    }

    @PutMapping("/sort")
    @PreAuthorize("hasRole('admin')")
    public Result<?> sort(@RequestBody List<SortItemDTO> items) {
        roleService.sortRoles(items);
        return Result.ok();
    }
}
