package com.unified.system.audit;

import com.unified.common.audit.Auditable;
import com.unified.common.security.UserContext;
import com.unified.system.service.SysAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final SysAuditLogService auditLogService;
    private final HttpServletRequest request;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long start = System.currentTimeMillis();
        UserContext ctx = UserContext.get();
        String username = ctx != null ? ctx.getUsername() : "system";
        Long userId = ctx != null ? ctx.getUserId() : 0L;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String params = Arrays.toString(joinPoint.getArgs());
        if (params.length() > 500) {
            params = params.substring(0, 500) + "...";
        }

        String result = "success";
        try {
            Object ret = joinPoint.proceed();
            return ret;
        } catch (Throwable e) {
            result = "error: " + e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            String ip = request.getRemoteAddr();
            auditLogService.recordAsync(userId, username, auditable.module(), auditable.operation(),
                    signature.getMethod().getName(), params, duration, ip, result);
        }
    }
}
