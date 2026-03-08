package com.wms.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.common.Result;
import com.wms.entity.OperationLog;
import com.wms.service.OperationLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
public class OperationLogAspect {

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private ObjectMapper objectMapper;

    @Around("execution(public * com.wms.controller..*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = currentRequest();
        OperationLog log = new OperationLog();
        log.setCreateTime(LocalDateTime.now());
        log.setModuleName(joinPoint.getSignature().getDeclaringType().getSimpleName().replace("Controller", ""));
        log.setActionName(joinPoint.getSignature().getName());
        log.setRequestMethod(request == null ? "-" : request.getMethod());
        log.setRequestPath(request == null ? "-" : request.getRequestURI());
        log.setRequestParams(serializeArgs(joinPoint.getArgs()));
        log.setIpAddress(resolveIp(request));
        log.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
        fillOperator(log, request);

        try {
            Object result = joinPoint.proceed();
            log.setSuccess(resolveSuccess(result));
            log.setMessage(resolveMessage(result, true));
            return result;
        } catch (Throwable ex) {
            log.setSuccess(false);
            log.setMessage(ex.getMessage());
            throw ex;
        } finally {
            try {
                operationLogService.save(log);
            } catch (Exception ignored) {
            }
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private void fillOperator(OperationLog log, HttpServletRequest request) {
        if (request == null) {
            return;
        }

        log.setOperatorId(parseInteger(request.getHeader("X-User-Id")));
        log.setOperatorName(decodeHeader(request.getHeader("X-User-Name")));
        log.setOperatorNo(request.getHeader("X-User-No"));
        log.setRoleId(parseInteger(request.getHeader("X-User-Role")));
    }

    private String decodeHeader(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return value;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String serializeArgs(Object[] args) {
        List<Object> filtered = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest || arg instanceof jakarta.servlet.http.HttpServletResponse) {
                continue;
            }
            filtered.add(arg);
        }
        try {
            String json = objectMapper.writeValueAsString(filtered);
            return json.length() > 2000 ? json.substring(0, 2000) : json;
        } catch (JsonProcessingException ex) {
            return "[unserializable]";
        }
    }

    private boolean resolveSuccess(Object result) {
        if (result instanceof Result res) {
            return res.getCode() == 200;
        }
        if (result instanceof Boolean bool) {
            return bool;
        }
        return true;
    }

    private String resolveMessage(Object result, boolean defaultSuccess) {
        if (result instanceof Result res) {
            return res.getMsg();
        }
        return defaultSuccess ? "OK" : "FAILED";
    }
}
