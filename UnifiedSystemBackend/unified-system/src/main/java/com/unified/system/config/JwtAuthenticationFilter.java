package com.unified.system.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unified.common.security.JwtUtil;
import com.unified.common.security.TokenManager;
import com.unified.common.security.UserContext;
import com.unified.system.entity.*;
import com.unified.system.mapper.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenManager tokenManager;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleResourceMapper roleResourceMapper;
    private final SysResourceMapper resourceMapper;
    private final SysRoleMapper roleMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            chain.doFilter(request, response);
            return;
        }

        if (!jwtUtil.validateToken(token) || tokenManager.isBlacklisted(token)) {
            if (tokenManager.isBlacklisted(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"code\":401,\"message\":\"Token已失效，请重新登录\"}");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (!tokenManager.isLatestToken(userId, token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"账号已在其他设备登录\"}");
            return;
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            chain.doFilter(request, response);
            return;
        }

        UserContext ctx = new UserContext();
        ctx.setUserId(user.getId());
        ctx.setUsername(user.getUsername());
        ctx.setRealName(user.getRealName());
        ctx.setDeptId(user.getDeptId());
        UserContext.set(ctx);

        List<SimpleGrantedAuthority> authorities = loadAuthorities(user.getId());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
        UserContext.clear();
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private List<SimpleGrantedAuthority> loadAuthorities(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        roleMapper.selectBatchIds(roleIds).stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRoleCode()))
                .forEach(authorities::add);

        Set<Long> resourceIds = roleResourceMapper.selectList(
                new LambdaQueryWrapper<SysRoleResource>().in(SysRoleResource::getRoleId, roleIds))
                .stream().map(SysRoleResource::getResourceId)
                .collect(Collectors.toSet());

        if (!resourceIds.isEmpty()) {
            resourceMapper.selectBatchIds(resourceIds).stream()
                    .map(r -> new SimpleGrantedAuthority(r.getCode()))
                    .forEach(authorities::add);
        }

        return authorities;
    }
}
