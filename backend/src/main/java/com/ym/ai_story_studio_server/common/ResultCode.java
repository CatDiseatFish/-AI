package com.ym.ai_story_studio_server.common;

import lombok.Getter;

/**
 * 统一响应状态码枚举
 *
 * 编码规则：
 * - 200: 成功
 * - 400xx: 客户端错误（参数、权限等）
 * - 500xx: 服务端错误（系统、第三方服务等）
 *
 * 模块划分：
 * - 4001x: 认证相关
 * - 4002x: 用户相关
 * - 4003x: 项目相关
 * - 4004x: 角色相关
 * - 4005x: 场景相关
 * - 4006x: 分镜相关
 * - 4007x: 资产相关
 * - 4008x: 任务相关
 * - 4009x: 钱包/支付相关
 * - 4010x: 邀请相关
 * - 5001x: 系统错误
 * - 5002x: 第三方服务错误
 */
@Getter
public enum ResultCode {

    // ==================== 成功 ====================
    SUCCESS(200, "操作成功"),

    // ==================== 通用客户端错误 4000x ====================
    BAD_REQUEST(40000, "请求参数错误"),
    PARAM_MISSING(40001, "缺少必要参数"),
    PARAM_INVALID(40002, "参数格式不正确"),
    DUPLICATE_RESOURCE(40003, "资源已存在"),
    RESOURCE_NOT_FOUND(40004, "资源不存在"),
    METHOD_NOT_ALLOWED(40005, "请求方法不支持"),
    REQUEST_TOO_FREQUENT(40006, "请求过于频繁，请稍后再试"),

    // ==================== 认证授权 4001x ====================
    UNAUTHORIZED(40100, "未登录或登录已过期"),
    TOKEN_INVALID(40101, "Token无效"),
    TOKEN_EXPIRED(40102, "Token已过期"),
    ACCESS_DENIED(40103, "无权限访问"),
    ACCOUNT_DISABLED(40104, "账号已被禁用"),
    LOGIN_FAILED(40105, "登录失败，用户名或密码错误"),
    CAPTCHA_ERROR(40106, "验证码错误"),
    OAUTH_FAILED(40107, "第三方登录失败"),

    // ==================== 用户相关 4002x ====================
    USER_NOT_FOUND(40200, "用户不存在"),
    USER_ALREADY_EXISTS(40201, "用户已存在"),
    PHONE_ALREADY_BOUND(40202, "手机号已被绑定"),
    EMAIL_ALREADY_BOUND(40203, "邮箱已被绑定"),
    PASSWORD_INCORRECT(40204, "密码错误"),
    SMS_SEND_FAILED(40205, "短信发送失败"),
    SMS_CODE_INVALID(40206, "短信验证码错误或已过期"),

    // ==================== 项目相关 4003x ====================
    PROJECT_NOT_FOUND(40300, "项目不存在"),
    PROJECT_NAME_DUPLICATE(40301, "项目名称已存在"),
    PROJECT_LIMIT_EXCEEDED(40302, "项目数量已达上限"),
    FOLDER_NOT_FOUND(40303, "文件夹不存在"),
    FOLDER_NAME_DUPLICATE(40304, "文件夹名称已存在"),

    // ==================== 角色相关 4004x ====================
    CHARACTER_NOT_FOUND(40400, "角色不存在"),
    CHARACTER_NAME_DUPLICATE(40401, "角色名称已存在"),
    CHARACTER_IN_USE(40402, "角色正在使用中，无法删除"),
    CHARACTER_LIMIT_EXCEEDED(40403, "角色数量已达上限"),

    // ==================== 场景相关 4005x ====================
    SCENE_NOT_FOUND(40500, "场景不存在"),
    SCENE_NAME_DUPLICATE(40501, "场景名称已存在"),
    SCENE_IN_USE(40502, "场景正在使用中，无法删除"),

    // ==================== 分镜相关 4006x ====================
    SHOT_NOT_FOUND(40600, "分镜不存在"),
    SHOT_SEQUENCE_CONFLICT(40601, "分镜序号冲突"),
    STORYBOARD_PARSE_FAILED(40602, "剧本解析失败"),

    // ==================== 资产相关 4007x ====================
    ASSET_NOT_FOUND(40700, "资产不存在"),
    ASSET_VERSION_NOT_FOUND(40701, "资产版本不存在"),
    ASSET_UPLOAD_FAILED(40702, "资产上传失败"),
    ASSET_TYPE_UNSUPPORTED(40703, "不支持的资产类型"),
    ASSET_SIZE_EXCEEDED(40704, "资产大小超出限制"),

    // ==================== 任务相关 4008x ====================
    JOB_NOT_FOUND(40800, "任务不存在"),
    JOB_ALREADY_COMPLETED(40801, "任务已完成"),
    JOB_ALREADY_CANCELLED(40802, "任务已取消"),
    JOB_QUEUE_FULL(40803, "任务队列已满，请稍后再试"),
    JOB_GENERATION_FAILED(40804, "生成任务执行失败"),

    // ==================== 钱包/支付相关 4009x ====================
    WALLET_NOT_FOUND(40900, "钱包不存在"),
    INSUFFICIENT_BALANCE(40901, "积分余额不足"),
    PAYMENT_FAILED(40902, "支付失败"),
    PAYMENT_TIMEOUT(40903, "支付超时"),
    ORDER_NOT_FOUND(40904, "订单不存在"),
    ORDER_ALREADY_PAID(40905, "订单已支付"),
    ORDER_EXPIRED(40906, "订单已过期"),
    REFUND_FAILED(40907, "退款失败"),

    // ==================== 邀请相关 4010x ====================
    INVITE_CODE_NOT_FOUND(41000, "邀请码不存在"),
    INVITE_CODE_EXPIRED(41001, "邀请码已过期"),
    INVITE_CODE_USED_UP(41002, "邀请码使用次数已达上限"),
    INVITE_CODE_DISABLED(41003, "邀请码已禁用"),
    ALREADY_INVITED(41004, "您已经被邀请过了"),
    CANNOT_INVITE_SELF(41005, "不能邀请自己"),

    // ==================== 风格/模板相关 4011x ====================
    STYLE_NOT_FOUND(41100, "风格预设不存在"),
    TEMPLATE_NOT_FOUND(41101, "模板不存在"),
    TEMPLATE_NAME_DUPLICATE(41102, "模板名称已存在"),

    // ==================== 系统错误 5001x ====================
    SYSTEM_ERROR(50000, "系统内部错误"),
    DATABASE_ERROR(50001, "数据库操作异常"),
    CACHE_ERROR(50002, "缓存操作异常"),
    FILE_OPERATION_ERROR(50003, "文件操作异常"),
    CONFIG_ERROR(50004, "配置错误"),

    // ==================== 第三方服务错误 5002x ====================
    THIRD_PARTY_ERROR(50200, "第三方服务异常"),
    AI_SERVICE_ERROR(50201, "AI服务调用失败"),
    AI_SERVICE_TIMEOUT(50202, "AI服务响应超时"),
    AI_SERVICE_QUOTA_EXCEEDED(50203, "AI服务配额已用尽"),
    OSS_ERROR(50204, "OSS存储服务异常"),
    WECHAT_PAY_ERROR(50205, "微信支付服务异常"),
    SMS_SERVICE_ERROR(50206, "短信服务异常"),
    FILE_SIZE_EXCEEDED(50007, "文件大小超出限制"),
    ONLY_IMAGE_FILES_ALLOWED(50008, "只允许上传图片文件"),
    AVATAR_UPLOAD_FAILED(50009, "头像上传失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
