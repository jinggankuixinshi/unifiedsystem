package com.unified.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.PageResult;
import com.unified.common.core.Result;
import com.unified.system.entity.SysAuditLog;
import com.unified.system.service.SysAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final SysAuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public Result<IPage<SysAuditLog>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.ok(auditLogService.pageLogs(pageNum, pageSize, keyword));
    }
}
