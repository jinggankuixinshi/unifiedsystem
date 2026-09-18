package com.unified.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unified.common.core.Result;
import com.unified.common.security.JwtUtil;
import com.unified.common.security.TokenManager;
import com.unified.common.security.UserContext;
import com.unified.system.dto.LoginDTO;
import com.unified.system.entity.*;
import com.unified.system.mapper.*;
import com.unified.system.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final TokenManager tokenManager;
    private final JwtUtil jwtUtil;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleResourceMapper roleResourceMapper;
    private final SysResourceMapper resourceMapper;
    private final SysRoleMapper roleMapper;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        Map<String, Object> loginResult = sysUserService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return Result.ok(loginResult);
    }

    @GetMapping("/user-info")
    public Result<Map<String, Object>> userInfo() {
        UserContext ctx = UserContext.get();
        SysUser user = sysUserService.getById(ctx.getUserId());
        Map<String, Object> info = new HashMap<>();
        info.put("userId", user.getId());
        info.put("username", user.getUsername());
        info.put("realName", user.getRealName());
        info.put("deptId", user.getDeptId());
        info.put("roles", loadRoles(user.getId()));
        info.put("permissions", loadPermissions(user.getId()));
        return Result.ok(info);
    }

    private List<String> loadRoles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return roleMapper.selectBatchIds(roleIds).stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }

    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            tokenManager.addBlacklist(token);
        }
        return Result.ok();
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private List<String> loadPermissions(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> resourceIds = roleResourceMapper.selectList(
                new LambdaQueryWrapper<SysRoleResource>().in(SysRoleResource::getRoleId, roleIds))
                .stream().map(SysRoleResource::getResourceId)
                .collect(Collectors.toSet());

        if (resourceIds.isEmpty()) {
            return Collections.emptyList();
        }

        return resourceMapper.selectBatchIds(resourceIds).stream()
                .map(SysResource::getCode)
                .collect(Collectors.toList());
    }
}
