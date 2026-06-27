package com.seckill.mall.common;

import lombok.Getter;

/**
 * 自定义业务异常类
 * <p>
 * 用于在Service层抛出业务逻辑异常，由 {@link GlobalExceptionHandler} 统一捕获并转换为
 * 标准的 {@link Result} 响应返回给前端。
 * </p>
 * <p>
 * 使用方式：{@code throw new BusinessException(ResultCode.SECKILL_REPEAT);}
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务状态码 */
    private final int code;

    /** 业务提示信息 */
    private final String msg;

    /**
     * 通过预定义的状态码枚举构造异常
     *
     * @param resultCode 状态码枚举，自动提取code和msg
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    /**
     * 通过自定义code和msg构造异常
     *
     * @param code 自定义状态码
     * @param msg  自定义提示信息
     */
    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    /**
     * 通过自定义msg构造异常（使用默认FAIL状态码500）
     *
     * @param msg 自定义提示信息
     */
    public BusinessException(String msg) {
        super(msg);
        this.code = ResultCode.FAIL.getCode();
        this.msg = msg;
    }
}
