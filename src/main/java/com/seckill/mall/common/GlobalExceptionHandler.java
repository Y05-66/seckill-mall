package com.seckill.mall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import javax.validation.ConstraintViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 使用 {@code @RestControllerAdvice} 统一拦截Controller层抛出的各种异常，
 * 将其转换为标准的 {@link Result} 响应格式返回给前端，避免前端直接收到裸异常信息。
 * </p>
 * <p>
 * 异常处理优先级（从高到低）：
 * 1. 参数校验异常（@Valid/@Validated触发）
 * 2. 认证异常（用户名密码错误）
 * 3. 授权异常（权限不足）
 * 4. 业务异常（自定义BusinessException）
 * 5. 系统异常（兜底，未知错误）
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理@Valid注解触发的参数校验异常（@RequestBody场景）
     * 提取所有字段错误信息，用逗号拼接返回
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 处理@Validated注解触发的绑定异常（表单参数/QueryString场景）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBind(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.error(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 处理Spring Security认证异常（用户名或密码错误）
     * 由AuthenticationManager.authenticate()抛出
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleBadCredentials(BadCredentialsException e) {
        return Result.error(ResultCode.PASSWORD_ERROR);
    }

    /**
     * 处理Spring Security禁用异常（账户被管理员禁用
     * 当UserDetails.isEnabled()返回false时，Spring Security认证链会抛出此异常
     * 禁用用户无法登录或访问需认证的接口
     */
    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleDisabled(DisabledException e){
        return Result.error(ResultCode.USER_DISABLED);
    }

    /**
     * 处理Spring Security授权异常（已认证但权限不足）
     * 由@PreAuthorize等权限注解触发
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDenied(AccessDeniedException e) {
        return Result.error(ResultCode.FORBIDDEN);
    }

    /**
     * 处理自定义业务异常
     * 业务层通过 throw new BusinessException(...) 抛出
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMsg());
        return Result.error(e.getCode(), e.getMsg());
    }

    /**
     * 处理参数校验异常（@RequestParam/@PathVariable 上的 @Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", msg);
        return Result.error(400, msg.isEmpty() ? "参数格式错误" : msg);
    }

    /**
     * 兜底处理所有未知异常
     * 记录完整错误日志，返回通用错误信息，避免泄露系统内部细节
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ResultCode.FAIL);
    }
}
