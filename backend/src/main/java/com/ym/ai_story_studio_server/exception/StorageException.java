// {{CODE-Cycle-Integration:
//   Task_ID: [#T001]
//   Timestamp: [2025-12-26 15:35:00]
//   Phase: [D-Develop]
//   Context-Analysis: "创建存储服务专用异常类，用于统一处理OSS操作中的错误。"
//   Principle_Applied: "异常处理最佳实践, 单一职责原则"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.exception;

/**
 * 存储服务异常
 *
 * <p>用于封装文件上传、下载、删除等存储操作中产生的异常
 *
 * <p>异常类型包括但不限于:
 * <ul>
 *   <li>网络连接超时</li>
 *   <li>认证失败</li>
 *   <li>存储桶不存在</li>
 *   <li>文件上传失败</li>
 *   <li>文件类型不支持</li>
 *   <li>文件大小超限</li>
 * </ul>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public class StorageException extends RuntimeException {

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 构造存储异常
     *
     * @param message 异常消息
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * 构造存储异常（带原因）
     *
     * @param message 异常消息
     * @param cause   原始异常
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造存储异常（带错误代码）
     *
     * @param errorCode 错误代码
     * @param message   异常消息
     */
    public StorageException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造存储异常（完整参数）
     *
     * @param errorCode 错误代码
     * @param message   异常消息
     * @param cause     原始异常
     */
    public StorageException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误代码
     *
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
}
// {{END_MODIFICATIONS}}
