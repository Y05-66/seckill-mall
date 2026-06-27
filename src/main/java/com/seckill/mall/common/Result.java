package com.seckill.mall.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应结果封装类
 * <p>
 * 所有Controller接口统一使用此类包装返回数据，确保前端接收格式一致。
 * 响应结构：{ "code": 200, "msg": "操作成功", "data": {...} }
 * </p>
 *
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> implements Serializable {

    /** 状态码，200表示成功，其他表示各类错误 */
    private int code;

    /** 提示信息，用于前端展示给用户 */
    private String msg;

    /** 响应数据，成功时返回业务数据，失败时为null */
    private T data;

    private Result() {}

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 返回成功结果（无数据）
     * 适用于删除、更新等不需要返回数据的操作
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    /**
     * 返回成功结果（带数据）
     * 适用于查询、新增等需要返回数据的操作
     *
     * @param data 响应数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    /**
     * 返回成功结果（自定义提示信息 + 数据）
     *
     * @param msg  自定义成功提示信息
     * @param data 响应数据
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 返回错误结果（使用预定义状态码枚举）
     *
     * @param resultCode 错误状态码枚举
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    /**
     * 返回错误结果（使用默认错误码 + 自定义消息）
     *
     * @param msg 错误提示信息
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(ResultCode.FAIL.getCode(), msg, null);
    }

    /**
     * 返回错误结果（自定义错误码 + 消息）
     *
     * @param code 错误状态码
     * @param msg  错误提示信息
     */
    public static <T> Result<T> error(int code, String msg) {
        return new Result<>(code, msg, null);
    }
}
