package com.unified.system.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.util.SequenceGenerator;
import com.unified.common.workflow.WorkflowApproverResolver;
import com.unified.common.workflow.WorkflowEngine;
import com.unified.system.dto.HrAccountOpDTO;
import com.unified.system.dto.SysUserDTO;
import com.unified.system.entity.HrAccountOp;
import com.unified.system.entity.SysUser;
import com.unified.system.mapper.HrAccountOpMapper;
import com.unified.system.mapper.SysUserMapper;
import com.unified.system.service.HrAccountOpService;
import com.unified.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人事账号操作：注册/启用/禁用/删除
 * 非管理员提交 → 走 hr_account_op 审批流；管理员提交 → 直接执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HrAccountOpServiceImpl extends ServiceImpl<HrAccountOpMapper, HrAccountOp> implements HrAccountOpService {

    private static final String BUSINESS_TYPE = "hr_account_op";
    private static final List<String> OP_TYPES = List.of("register", "enable", "disable", "delete");

    private final SysUserService sysUserService;
    private final SysUserMapper userMapper;
    private final WorkflowEngine workflowEngine;
    private final WorkflowApproverResolver approverResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HrAccountOp submit(HrAccountOpDTO dto, Long applicantId) {
        validate(dto, applicantId);

        HrAccountOp op = new HrAccountOp();
        op.setOpNo(SequenceGenerator.generate("HR"));
        op.setOpType(dto.getOpType());
        op.setTargetUserId(dto.getTargetUserId());
        op.setApplicantId(applicantId);
        if ("register".equals(dto.getOpType())) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("username", dto.getUsername());
            payload.put("realName", dto.getRealName());
            payload.put("deptId", dto.getDeptId());
            payload.put("phone", dto.getPhone());
            payload.put("email", dto.getEmail());
            op.setPayload(JSONUtil.toJsonStr(payload));
        }
        boolean admin = approverResolver.isAdmin(applicantId);
        op.setApprovalStatus(admin ? 1 : 0);
        op.setExecuted(0);
        save(op);

        if (admin) {
            execute(op);
        } else {
            workflowEngine.startWorkflow(BUSINESS_TYPE, op.getId(), applicantId, Map.of());
        }
        log.info("人事账号操作单: opNo={}, type={}, applicantId={}, adminDirect={}", op.getOpNo(), op.getOpType(), applicantId, admin);
        return op;
    }

    @Override
    public IPage<HrAccountOp> pageOps(int pageNum, int pageSize, Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<HrAccountOp> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(HrAccountOp::getApprovalStatus, status);
        }
        wrapper.orderByDesc(HrAccountOp::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void execute(HrAccountOp op) {
        try {
            switch (op.getOpType()) {
                case "register" -> executeRegister(op);
                case "enable" -> updateStatus(op.getTargetUserId(), 1);
                case "disable" -> updateStatus(op.getTargetUserId(), 0);
                case "delete" -> sysUserService.deleteUser(op.getTargetUserId());
                default -> throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "不支持的操作类型");
            }
            op.setExecuted(1);
            op.setExecMessage("执行成功");
        } catch (Exception e) {
            log.error("账号操作单执行失败: opNo={}, err={}", op.getOpNo(), e.getMessage());
            op.setExecuted(-1);
            op.setExecMessage("执行失败：" + e.getMessage());
        } finally {
            updateById(op);
        }
    }

    private void executeRegister(HrAccountOp op) {
        JSONObject payload = JSONUtil.parseObj(op.getPayload() == null ? "{}" : op.getPayload());
        String username = payload.getStr("username");
        SysUserDTO dto = new SysUserDTO();
        dto.setUsername(username);
        dto.setPassword(username + "123");
        dto.setRealName(payload.getStr("realName"));
        dto.setDeptId(payload.getLong("deptId"));
        dto.setPhone(payload.getStr("phone"));
        dto.setEmail(payload.getStr("email"));
        dto.setStatus(1);
        sysUserService.createUser(dto);
    }

    private void updateStatus(Long userId, int status) {
        SysUser update = new SysUser();
        update.setId(userId);
        update.setStatus(status);
        userMapper.updateById(update);
    }

    private void validate(HrAccountOpDTO dto, Long applicantId) {
        if (!OP_TYPES.contains(dto.getOpType())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "不支持的操作类型");
        }
        if ("register".equals(dto.getOpType())) {
            if (dto.getUsername() == null || dto.getUsername().isBlank()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "登录账号不能为空");
            }
            if (dto.getRealName() == null || dto.getRealName().isBlank()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "姓名不能为空");
            }
            if (dto.getDeptId() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "所属部门不能为空");
            }
            if (sysUserService.getByUsername(dto.getUsername()) != null) {
                throw new BusinessException(ErrorCode.USERNAME_EXISTS);
            }
            return;
        }
        if (dto.getTargetUserId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "目标用户不能为空");
        }
        if (dto.getTargetUserId().equals(applicantId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "不能对本人账号执行该操作");
        }
        if (userMapper.selectById(dto.getTargetUserId()) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}
