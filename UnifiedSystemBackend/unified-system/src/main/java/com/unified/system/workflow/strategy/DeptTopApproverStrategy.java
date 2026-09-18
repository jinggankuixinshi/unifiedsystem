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
 * 部门最高管：申请人所属部门内角色等级最高的用户（多用户同级则任一可审，排除申请人本人）
 */
@Component
public class DeptTopApproverStrategy extends AbstractDeptStrategy implements WorkflowApproverStrategy {

    public DeptTopApproverStrategy(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper, SysRoleMapper roleMapper) {
        super(userMapper, userRoleMapper, roleMapper);
    }

    @Override
    public String approverType() {
        return "DEPT_TOP";
    }

    @Override
    public List<Long> resolve(WfNodeTemplate node, Long applicantId) {
        Map<Long, Integer> memberLevels = memberMaxLevels(deptIdOf(applicantId));
        if (memberLevels.isEmpty()) {
            return List.of();
        }
        int maxLevel = memberLevels.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (maxLevel <= 0) {
            return List.of();
        }
        return memberLevels.entrySet().stream()
                .filter(entry -> entry.getValue() == maxLevel && !entry.getKey().equals(applicantId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
