package com.unified.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.common.security.UserContext;
import com.unified.system.dto.AttLeaveDTO;
import com.unified.system.dto.AttOvertimeDTO;
import com.unified.system.service.AttLeaveService;
import com.unified.system.service.AttOvertimeService;
import com.unified.system.service.AttRecordService;
import com.unified.system.vo.AttLeaveVO;
import com.unified.system.vo.AttOvertimeVO;
import com.unified.system.vo.AttRecordVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttRecordService attRecordService;
    private final AttLeaveService attLeaveService;
    private final AttOvertimeService attOvertimeService;

    @PostMapping("/clock-in")
    @PreAuthorize("isAuthenticated()")
    public Result<?> clockIn() {
        attRecordService.clockIn(UserContext.get().getUserId());
        return Result.ok();
    }

    @PostMapping("/clock-out")
    @PreAuthorize("isAuthenticated()")
    public Result<?> clockOut() {
        attRecordService.clockOut(UserContext.get().getUserId());
        return Result.ok();
    }

    @GetMapping("/records")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<AttRecordVO>> listRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId) {
        return Result.ok(attRecordService.pageRecords(pageNum, pageSize, userId));
    }

    @PostMapping("/leave")
    @PreAuthorize("isAuthenticated()")
    public Result<AttLeaveVO> applyLeave(@Valid @RequestBody AttLeaveDTO dto) {
        return Result.ok(attLeaveService.apply(UserContext.get().getUserId(), dto));
    }

    @GetMapping("/leave")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<AttLeaveVO>> listLeaves(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(attLeaveService.pageLeaves(pageNum, pageSize, userId, status));
    }

    @PostMapping("/overtime")
    @PreAuthorize("isAuthenticated()")
    public Result<AttOvertimeVO> applyOvertime(@Valid @RequestBody AttOvertimeDTO dto) {
        return Result.ok(attOvertimeService.apply(UserContext.get().getUserId(), dto));
    }

    @GetMapping("/overtime")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<AttOvertimeVO>> listOvertimes(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(attOvertimeService.pageOvertimes(pageNum, pageSize, userId, status));
    }
}
