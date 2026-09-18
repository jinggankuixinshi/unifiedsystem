package com.unified.system.workflow.strategy;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unified.common.workflow.WorkflowApproverStrategy;
import com.unified.common.workflow.entity.WfNodeTemplate;
import com.unified.system.entity.SysUserRole;
import com.unified.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleApproverStrategy implements WorkflowApproverStrategy {

    private final SysUserRoleMapper userRoleMapper;

    @Override
    public String approverType() {
        return "ROLE";
    }

    @Override
    public List<Long> resolve(WfNodeTemplate node, Long applicantId) {
        if (node.getApproverId() == null) {
            return Collections.emptyList();
        }
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, node.getApproverId()))
                .stream().map(SysUserRole::getUserId).distinct().toList();
    }
}
