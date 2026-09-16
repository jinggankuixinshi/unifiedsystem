package com.unified.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.common.security.UserContext;
import com.unified.system.entity.SysMessage;
import com.unified.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class SysMessageController {

    private final SysMessageService messageService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<SysMessage>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.get().getUserId();
        return Result.ok(messageService.pageMessages(userId, pageNum, pageSize));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Result<java.util.Map<String, Object>> unreadCount() {
        Long userId = UserContext.get().getUserId();
        return Result.ok(java.util.Map.of("unreadCount", messageService.getUnreadCount(userId)));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public Result<?> markRead(@PathVariable Long id) {
        Long userId = UserContext.get().getUserId();
        messageService.markRead(id, userId);
        return Result.ok();
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public Result<?> markAllRead() {
        Long userId = UserContext.get().getUserId();
        messageService.markAllRead(userId);
        return Result.ok();
    }
}
