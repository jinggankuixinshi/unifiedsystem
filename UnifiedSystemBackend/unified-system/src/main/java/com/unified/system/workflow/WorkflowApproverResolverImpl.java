package com.unified.system.workflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unified.common.workflow.WorkflowApproverResolver;
import com.unified.common.workflow.WorkflowApproverStrategy;
import com.unified.common.workflow.entity.WfNodeTemplate;
import com.unified.system.entity.SysRole;
import com.unified.system.entity.SysUserRole;
import com.unified.system.mapper.SysRoleMapper;
import com.unified.system.mapper.SysUserRoleMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审批人解析调度：按 approver_type 分发到 WorkflowApproverStrategy（ROLE/SPECIFIC_USER/DEPT_MANAGER/DEPT_TOP/ROLE_LEVEL_UP）
 * admin 角色为用户提供审批兜底权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowApproverResolverImpl implements WorkflowApproverResolver {

    private final List<WorkflowApproverStrategy> strategies;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final Map<String, WorkflowApproverStrategy> strategyMap = new HashMap<>();

    @PostConstruct
    void initStrategyMap() {
        for (WorkflowApproverStrategy strategy : strategies) {
            strategyMap.put(strategy.approverType(), strategy);
        }
    }

    @Override
    public List<Long> resolveApprovers(WfNodeTemplate node, Long applicantId) {
        if (node == null || node.getApproverType() == null) {
            return Collections.emptyList();
        }
        WorkflowApproverStrategy strategy = strategyMap.get(node.getApproverType());
        if (strategy == null) {
            log.warn("未注册的审批人类型: {}", node.getApproverType());
            return Collections.emptyList();
        }
        return strategy.resolve(node, applicantId);
    }

    @Override
    public boolean canApprove(WfNodeTemplate node, Long applicantId, Long userId) {
        if (userId == null) {
            return false;
        }
        if (isAdmin(userId)) {
            return true;
        }
        return resolveApprovers(node, applicantId).contains(userId);
    }

    @Override
    public boolean isAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        return adminUserIds().contains(userId);
    }

    @Override
    public Integer resolveApplicantLevel(Long userId) {
        if (userId == null) {
            return null;
        }
        List<Long> roleIds = roleIdsOfUsers(List.of(userId)).get(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return null;
        }
        return roleIds.stream()
                .map(roleId -> roleLevelMap().getOrDefault(roleId, 0))
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    @Override
    public List<Long> resolveAdminUserIds() {
        return adminUserIds();
    }

    @Override
    public int resolveNodeMaxApproverLevel(WfNodeTemplate node, Long applicantId) {
        List<Long> approvers = resolveApprovers(node, applicantId);
        if (approvers.isEmpty()) {
            return 0;
        }
        Map<Long, List<Long>> userRoleIds = roleIdsOfUsers(approvers);
        Map<Long, Integer> levelByRole = roleLevelMap();
        return approvers.stream()
                .mapToInt(userId -> userRoleIds.getOrDefault(userId, Collections.emptyList()).stream()
                        .mapToInt(roleId -> levelByRole.getOrDefault(roleId, 0))
                        .max().orElse(0))
                .max().orElse(0);
    }

    // ==================== 内部方法 ====================

    private List<Long> adminUserIds() {
        List<Long> adminRoleIds = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, "admin"))
                .stream().map(SysRole::getId).collect(Collectors.toList());
        if (adminRoleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getRoleId, adminRoleIds))
                .stream().map(SysUserRole::getUserId).distinct().collect(Collectors.toList());
    }

    private Map<Long, List<Long>> roleIdsOfUsers(List<Long> userIds) {
        List<SysUserRole> links = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds));
        Map<Long, List<Long>> result = new HashMap<>();
        for (SysUserRole link : links) {
            result.computeIfAbsent(link.getUserId(), k -> new java.util.ArrayList<>()).add(link.getRoleId());
        }
        return result;
    }

    private Map<Long, Integer> roleLevelMap() {
        return roleMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysRole::getId, role -> role.getLevel() == null ? 0 : role.getLevel()));
    }
}
