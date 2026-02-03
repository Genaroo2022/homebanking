package com.homebanking.adapter.in.web.audit;

import com.homebanking.adapter.in.web.annotation.Auditable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class AuditAspect {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String username = resolveUsername();
        String action = auditable.action().isBlank()
                ? joinPoint.getSignature().toShortString()
                : auditable.action();
        String args = Arrays.toString(joinPoint.getArgs());
        RequestInfo requestInfo = resolveRequestInfo();
        LocalDateTime timestamp = LocalDateTime.now();

        String json = toJson(
                username,
                action,
                args,
                timestamp,
                requestInfo
        );
        auditLog.info(json);
        return joinPoint.proceed();
    }

    private String resolveUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        return authentication.getName();
    }

    private RequestInfo resolveRequestInfo() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return RequestInfo.empty();
        }
        HttpServletRequest request = attrs.getRequest();
        if (request == null) {
            return RequestInfo.empty();
        }
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String method = request.getMethod();
        String path = request.getRequestURI();
        String requestId = request.getHeader("X-Request-Id");
        return new RequestInfo(ip, userAgent, method, path, requestId);
    }

    private String toJson(
            String username,
            String action,
            String args,
            LocalDateTime timestamp,
            RequestInfo requestInfo) {
        return "{"
                + "\"user\":\"" + escape(username) + "\","
                + "\"action\":\"" + escape(action) + "\","
                + "\"args\":\"" + escape(args) + "\","
                + "\"at\":\"" + escape(timestamp.toString()) + "\","
                + "\"ip\":\"" + escape(requestInfo.ip()) + "\","
                + "\"userAgent\":\"" + escape(requestInfo.userAgent()) + "\","
                + "\"method\":\"" + escape(requestInfo.method()) + "\","
                + "\"path\":\"" + escape(requestInfo.path()) + "\","
                + "\"requestId\":\"" + escape(requestInfo.requestId()) + "\""
                + "}";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private record RequestInfo(String ip, String userAgent, String method, String path, String requestId) {
        static RequestInfo empty() {
            return new RequestInfo("", "", "", "", "");
        }
    }
}
