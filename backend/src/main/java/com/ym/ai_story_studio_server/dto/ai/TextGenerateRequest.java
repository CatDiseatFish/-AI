package com.ym.ai_story_studio_server.dto.ai;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 文本生成请求DTO
 *
 * <p>用于调用AI文本生成服务,支持自定义模型参数
 *
 * <p><strong>使用示例:</strong>
 * <pre>
 * TextGenerateRequest request = new TextGenerateRequest(
 *     "请帮我写一个关于科幻的故事大纲",
 *     0.8,
 *     0.9,
 *     1L
 * );
 * </pre>
 *
 * <p><strong>计费说明:</strong>
 * 文本生成采用<strong>固定扣费模式</strong>,每次生成扣除固定积分,不按token数计费。
 * maxTokens参数已改为系统内置(默认4096),无需前端传入。
 *
 * @param prompt 提示词(必填,1-10000字符)
 * @param temperature 温度参数(可选,0-1,默认使用配置值)
 * @param topP 采样参数(可选,0-1,默认使用配置值)
 * @param projectId 所属项目ID(必填)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record TextGenerateRequest(
        @NotBlank(message = "提示词不能为空")
        @Size(min = 1, max = 10000, message = "提示词长度必须在1-10000个字符之间")
        String prompt,

        @Min(value = 0, message = "温度参数不能小于0")
        @Max(value = 1, message = "温度参数不能大于1")
        Double temperature,

        @Min(value = 0, message = "采样参数不能小于0")
        @Max(value = 1, message = "采样参数不能大于1")
        Double topP,

        @NotNull(message = "项目ID不能为空")
        Long projectId
) {
}