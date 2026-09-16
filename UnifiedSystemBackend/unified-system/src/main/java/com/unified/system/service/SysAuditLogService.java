package com.unified.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.system.entity.SysAuditLog;
import com.unified.system.mapper.SysAuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysAuditLogService extends ServiceImpl<SysAuditLogMapper, SysAuditLog> {

    @Async
    public void recordAsync(Long userId, String username, String module, String operation, String method, String params, long duration, String ip, String result) {
        SysAuditLog log = new SysAuditLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setModule(module);
        log.setOperation(operation);
        log.setMethod(method);
        log.setParams(params);
        log.setDuration(duration);
        log.setIp(ip);
        log.setResult(result);
        save(log);
    }

    public IPage<SysAuditLog> pageLogs(int pageNum, int pageSize, String keyword) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;

        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<SysAuditLog>()
                .and(keyword != null && !keyword.isEmpty(), w ->
                        w.like(SysAuditLog::getUsername, keyword)
                         .or().like(SysAuditLog::getModule, keyword)
                         .or().like(SysAuditLog::getOperation, keyword))
                .orderByDesc(SysAuditLog::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}
