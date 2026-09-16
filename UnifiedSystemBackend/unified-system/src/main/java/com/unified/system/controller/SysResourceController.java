package com.unified.system.controller;

import com.unified.common.core.Result;
import com.unified.system.dto.SortItemDTO;
import com.unified.system.dto.SysResourceDTO;
import com.unified.system.entity.SysResource;
import com.unified.system.service.SysResourceService;
import com.unified.system.vo.SysResourceVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/resources")
@RequiredArgsConstructor
public class SysResourceController {

    private final SysResourceService resourceService;

    @GetMapping("/tree")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysResourceVO>> tree() {
        return Result.ok(resourceService.getResourceTree());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysResource>> listAll() {
        return Result.ok(resourceService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SysResource> getById(@PathVariable Long id) {
        return Result.ok(resourceService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<SysResource> create(@Valid @RequestBody SysResourceDTO dto) {
        return Result.ok(resourceService.createResource(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<SysResource> update(@PathVariable Long id, @Valid @RequestBody SysResourceDTO dto) {
        return Result.ok(resourceService.updateResource(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<?> delete(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return Result.ok();
    }

    @PutMapping("/sort")
    @PreAuthorize("hasRole('admin')")
    public Result<?> sort(@RequestBody List<SortItemDTO> items) {
        resourceService.sortResources(items);
        return Result.ok();
    }
}
