package com.unified.system.workflow.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unified.common.workflow.entity.WfNodeTemplate;
import com.unified.system.entity.SysUser;
import com.unified.system.entity.SysUserRole;
import com.unified.system.mapper.SysRoleMapper;
import com.unified.system.mapper.SysUserMapper;
import com.unified.system.mapper.SysUserRoleMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门维度策略基类：提供部门成员与其最高角色等级的解析
 */
public abstract class AbstractDeptStrategy {

    protected final SysUserMapper userMapper;
    protected final SysUserRoleMapper userRoleMapper;
    protected final SysRoleMapper roleMapper;

    protected AbstractDeptStrategy(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
    }

    protected Long deptIdOf(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = userMapper.selectById(userId);
        return user == null ? null : user.getDeptId();
    }

    /** 部门内启用用户的「用户 -> 最高角色等级」映射 */
    protected Map<Long, Integer> memberMaxLevels(Long deptId) {
        if (deptId == null) {
            return Collections.emptyMap();
        }
        List<Long> memberIds = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeptId, deptId)
                        .eq(SysUser::getStatus, 1))
                .stream().map(SysUser::getId).collect(Collectors.toList());
        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysUserRole> links = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, memberIds));
        if (links.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> levelByRole = roleMapper.selectBatchIds(links.stream()
                        .map(SysUserRole::getRoleId).distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(role -> role.getId(),
                        role -> role.getLevel() == null ? 0 : role.getLevel()));
        Map<Long, Integer> result = new HashMap<>();
        for (SysUserRole link : links) {
            int level = levelByRole.getOrDefault(link.getRoleId(), 0);
            result.merge(link.getUserId(), level, Math::max);
        }
        return result;
    }

    protected abstract String approverType();
}
