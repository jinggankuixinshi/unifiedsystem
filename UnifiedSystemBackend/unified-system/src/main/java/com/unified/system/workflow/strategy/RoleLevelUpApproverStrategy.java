package com.unified.system.workflow.strategy;

import com.unified.common.workflow.WorkflowApproverStrategy;
import com.unified.common.workflow.entity.WfNodeTemplate;
import com.unified.system.mapper.SysRoleMapper;
import com.unified.system.mapper.SysUserMapper;
import com.unified.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 等级上推：同部门内「角色等级严格高于申请人」的最低一档用户（排除申请人本人）
 */
@Component
public class RoleLevelUpApproverStrategy extends AbstractDeptStrategy implements WorkflowApproverStrategy {

    public RoleLevelUpApproverStrategy(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper) {
        super(userMapper, userRoleMapper, roleMapper);
    }

    @Override
    public String approverType() {
        return "ROLE_LEVEL_UP";
    }

    @Override
    public List<Long> resolve(WfNodeTemplate node, Long applicantId) {
        Map<Long, Integer> memberLevels = memberMaxLevels(deptIdOf(applicantId));
        if (memberLevels.isEmpty()) {
            return List.of();
        }
        int applicantLevel = memberLevels.getOrDefault(applicantId, 0);
        int targetLevel = memberLevels.values().stream()
                .filter(level -> level > applicantLevel)
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);
        if (targetLevel <= 0) {
            return List.of();
        }
        return memberLevels.entrySet().stream()
                .filter(entry -> entry.getValue() == targetLevel && !entry.getKey().equals(applicantId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
