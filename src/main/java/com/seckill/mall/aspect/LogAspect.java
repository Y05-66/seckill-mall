package com.seckill.mall.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seckill.mall.annotation.Log;
import com.seckill.mall.entity.OperationLog;
import com.seckill.mall.mapper.OperationLogMapper;
import com.seckill.mall.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 拦截带有 @Log 注解的方法，自动记录操作日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogMapper operationLogMapper;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        long startTime = System.currentTimeMillis();

        OperationLog opLog = new OperationLog();
        opLog.setOperation(logAnnotation.value());
        opLog.setMethod(((MethodSignature) joinPoint.getSignature()).getMethod().getName());
        opLog.setUrl(request.getRequestURI());
        opLog.setIp(getClientIp());

        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) auth.getPrincipal();
            opLog.setUserId(loginUser.getUserId());
            opLog.setUsername(loginUser.getUsername());
        }

        // 请求参数
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                opLog.setParams(objectMapper.writeValueAsString(args));
            }
        } catch (Exception e) {
            opLog.setParams("参数序列化失败");
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            opLog.setResult(1);
        } catch (Throwable e) {
            opLog.setResult(0);
            throw e;
        } finally {
            opLog.setCostTime(System.currentTimeMillis() - startTime);
            opLog.setCreateTime(LocalDateTime.now());
            try {
                operationLogMapper.insert(opLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
        return result;
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "";
    }
}
