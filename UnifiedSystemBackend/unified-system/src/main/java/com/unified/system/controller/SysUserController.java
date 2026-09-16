package com.unified.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.system.dto.SysUserDTO;
import com.unified.system.service.SysUserService;
import com.unified.system.vo.SysUserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<SysUserVO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        IPage<SysUserVO> page = sysUserService.pageUsers(pageNum, pageSize, keyword);
        return Result.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<SysUserVO> getById(@PathVariable Long id) {
        return Result.ok(sysUserService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<SysUserVO> create(@Valid @RequestBody SysUserDTO dto) {
        return Result.ok(sysUserService.createUser(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<SysUserVO> update(@PathVariable Long id, @Valid @RequestBody SysUserDTO dto) {
        return Result.ok(sysUserService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<?> delete(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.ok();
    }
}
