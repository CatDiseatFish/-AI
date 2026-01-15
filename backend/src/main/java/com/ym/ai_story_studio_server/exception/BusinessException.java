package com.ym.ai_story_studio_server.exception;

import com.ym.ai_story_studio_server.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常类
 *
 * 用于在业务逻辑中主动抛出，会被全局异常处理器捕获并返回统一响应格式
 *
 * 使用示例：
 * throw new BusinessException(ResultCode.USER_NOT_FOUND);
 * throw new BusinessException(ResultCode.PARAM_INVALID, "用户名长度必须在3-20个字符之间");
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误码枚举（用于追踪）
     */
    private final ResultCode resultCode;

    /**
     * 附加数据（可选）
     */
    private final Object data;

    /**
     * 使用ResultCode构造
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.resultCode = resultCode;
        this.data = null;
    }

    /**
     * 使用ResultCode + 自定义消息构造
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.resultCode = resultCode;
        this.data = null;
    }

    /**
     * 使用ResultCode + 自定义消息 + 原因构造
     */
    public BusinessException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.code = resultCode.getCode();
        this.resultCode = resultCode;
        this.data = null;
    }

    /**
     * 使用ResultCode + 附加数据构造
     */
    public BusinessException(ResultCode resultCode, Object data) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.resultCode = resultCode;
        this.data = data;
    }

    /**
     * 使用自定义code和message构造
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.resultCode = null;
        this.data = null;
    }

    // ==================== 快捷静态方法 ====================

    /**
     * 资源不存在异常
     */
    public static BusinessException notFound(String resourceName) {
        return new BusinessException(ResultCode.RESOURCE_NOT_FOUND, resourceName + "不存在");
    }

    /**
     * 参数无效异常
     */
    public static BusinessException invalidParam(String message) {
        return new BusinessException(ResultCode.PARAM_INVALID, message);
    }

    /**
     * 无权限异常
     */
    public static BusinessException accessDenied() {
        return new BusinessException(ResultCode.ACCESS_DENIED);
    }

    /**
     * 余额不足异常
     */
    public static BusinessException insufficientBalance() {
        return new BusinessException(ResultCode.INSUFFICIENT_BALANCE);
    }
}
