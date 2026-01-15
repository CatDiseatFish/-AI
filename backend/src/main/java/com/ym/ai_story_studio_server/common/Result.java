package com.ym.ai_story_studio_server.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 *
 * 响应格式：
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... }
 * }
 *
 * @param <T> 响应数据类型
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 状态码
     */
    private int code;

    /**
     * 状态描述信息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳（毫秒）
     */
    private long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public Result(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // ==================== 成功响应 ====================

    /**
     * 成功响应（无数据）
     */
    public static Result<Void> success() {
        return new Result<>(ResultCode.SUCCESS, null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS, data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>(ResultCode.SUCCESS, data);
        result.setMessage(message);
        return result;
    }

    // ==================== 失败响应 ====================

    /**
     * 失败响应（使用ResultCode）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode, null);
    }

    /**
     * 失败响应（使用ResultCode + 自定义消息）
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        Result<T> result = new Result<>(resultCode, null);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应（使用ResultCode + 数据）
     */
    public static <T> Result<T> error(ResultCode resultCode, T data) {
        return new Result<>(resultCode, data);
    }

    /**
     * 失败响应（自定义code和message）
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 系统错误快捷方法
     */
    public static <T> Result<T> systemError() {
        return new Result<>(ResultCode.SYSTEM_ERROR, null);
    }

    /**
     * 系统错误快捷方法（自定义消息）
     */
    public static <T> Result<T> systemError(String message) {
        Result<T> result = new Result<>(ResultCode.SYSTEM_ERROR, null);
        result.setMessage(message);
        return result;
    }

    // ==================== 判断方法 ====================

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}
