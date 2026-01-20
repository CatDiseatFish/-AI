package com.ym.ai_story_studio_server.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文本解析任务消息
 *
 * <p>用于MQ传递文本解析任务信息
 *
 * @author AI Story Studio
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextParsingMessage implements Serializable {
    
    /**
     * 任务ID
     */
    private Long jobId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 原始文本内容
     */
    private String rawText;

    /**
     * 临时API密钥(可选)
     */
    private String apiKey;
}
