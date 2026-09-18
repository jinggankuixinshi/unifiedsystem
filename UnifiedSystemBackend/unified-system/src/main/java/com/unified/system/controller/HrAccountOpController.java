package com.unified.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.common.security.UserContext;
import com.unified.system.dto.HrAccountOpDTO;
import com.unified.system.entity.HrAccountOp;
import com.unified.system.service.HrAccountOpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hr/account-ops")
@RequiredArgsConstructor
public class HrAccountOpController {

    private final HrAccountOpService accountOpService;

    @PostMapping
    @PreAuthorize("hasAnyRole('admin', 'hr_manager', 'hr_staff')")
    public Result<HrAccountOp> submit(@Valid @RequestBody HrAccountOpDTO dto) {
        HrAccountOp op = accountOpService.submit(dto, UserContext.get().getUserId());
        return Result.ok(messageOf(op), op);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('admin', 'hr_manager', 'hr_staff')")
    public Result<IPage<HrAccountOp>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.ok(accountOpService.pageOps(pageNum, pageSize, status));
    }

    private String messageOf(HrAccountOp op) {
        if (op.getExecuted() != null && op.getExecuted() == 1) {
            return "操作已执行";
        }
        if (op.getExecuted() != null && op.getExecuted() == -1) {
            return "审批已通过但执行失败：" + op.getExecMessage();
        }
        return "已提交审批";
    }
}
