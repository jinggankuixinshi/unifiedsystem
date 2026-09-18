package com.unified.system.workflow.strategy;

import com.unified.common.workflow.WorkflowApproverStrategy;
import com.unified.common.workflow.entity.WfNodeTemplate;
import com.unified.system.entity.SysDepartment;
import com.unified.system.mapper.SysDepartmentMapper;
import com.unified.system.mapper.SysRoleMapper;
import com.unified.system.mapper.SysUserMapper;
import com.unified.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 部门负责人（sys_department.leader_id）
 */
@Component
public class DeptManagerApproverStrategy extends AbstractDeptStrategy implements WorkflowApproverStrategy {

    private final SysDepartmentMapper departmentMapper;

    public DeptManagerApproverStrategy(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper,
                                       SysRoleMapper roleMapper, SysDepartmentMapper departmentMapper) {
        super(userMapper, userRoleMapper, roleMapper);
        this.departmentMapper = departmentMapper;
    }

    @Override
    public String approverType() {
        return "DEPT_MANAGER";
    }

    @Override
    public List<Long> resolve(WfNodeTemplate node, Long applicantId) {
        Long deptId = deptIdOf(applicantId);
        if (deptId == null) {
            return Collections.emptyList();
        }
        SysDepartment dept = departmentMapper.selectById(deptId);
        if (dept == null || dept.getLeaderId() == null) {
            return Collections.emptyList();
        }
        return List.of(dept.getLeaderId());
    }
}
