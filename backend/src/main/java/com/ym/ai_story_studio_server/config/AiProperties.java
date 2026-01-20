// {{CODE-Cycle-Integration:
//   Task_ID: [#AI-SERVICE-001]
//   Timestamp: [2025-12-28 22:00:00]
//   Phase: [D-Develop]
//   Context-Analysis: "创建AI服务配置属性类,用于从application.yml读取向量引擎API配置、文本/图片/视频模型配置。"
//   Principle_Applied: "SOLID原则-单一职责, Spring Boot配置绑定最佳实践"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务配置属性类
 *
 * <p>从application.yml读取AI服务相关配置,包括向量引擎API配置、文本/图片/视频模型配置
 *
 * <p><strong>application.yml配置示例:</strong>
 * <pre>
 * # AI服务配置
 * ai:
 *   # 向量引擎中转站配置
 *   vectorengine:
 *     # API基础URL
 *     base-url: https://api.vectorengine.ai
 *     # API密钥（生产环境强烈建议使用环境变量）
 *     api-key: ${AI_API_KEY:sk-a9VY9DmeSn1YSqpxNtOpyZQ6CKZoSnSB5X22kMk8X0gKUOOy}
 *     # 请求超时时间（毫秒）
 *     timeout: 60000
 *     # 连接超时时间（毫秒）
 *     connect-timeout: 10000
 *     # 读取超时时间（毫秒）
 *     read-timeout: 60000
 *
 *   # 文本生成模型配置
 *   text:
 *     # 默认模型
 *     model: gemini-3-pro-preview
 *     # 最大token数
 *     max-tokens: 4096
 *     # 温度参数（0-1，越高越随机）
 *     temperature: 0.7
 *     # 采样参数
 *     top-p: 0.9
 *
 *   # 图片生成模型配置
 *   image:
 *     # 默认模型
 *     default-model: gemini-3-pro-image-preview
 *     # 是否启用即梦反代
 *     jimeng-proxy-enabled: true
 *     # 即梦模型
 *     jimeng-model: jimeng-4.5
 *     # 默认画幅比例
 *     default-aspect-ratio: "21:9"
 *     # 默认生成数量
 *     default-count: 1
 *
 *   # 视频生成模型配置
 *   video:
 *     # 默认模型
 *     model: sora-2
 *     # 默认画幅比例
 *     default-aspect-ratio: "16:9"
 *     # 默认时长（秒）
 *     default-duration: 5
 *     # 最大时长（秒）
 *     max-duration: 10
 *     # 轮询间隔（毫秒）
 *     poll-interval: 5000
 *     # 最大轮询次数
 *     max-poll-count: 120
 * </pre>
 *
 * <p><strong>使用示例:</strong>
 * <pre>
 * &#64;Service
 * public class AiTextService {
 *     &#64;Autowired
 *     private AiProperties aiProperties;
 *
 *     public String generateText(String prompt) {
 *         String apiKey = aiProperties.getVectorengine().getApiKey();
 *         String model = aiProperties.getText().getModel();
 *         // ... 调用API
 *     }
 * }
 * </pre>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * 向量引擎中转站配置
     */
    private VectorEngine vectorengine = new VectorEngine();

    /**
     * 文本生成模型配置
     */
    private Text text = new Text();

    /**
     * 图片生成模型配置
     */
    private Image image = new Image();

    /**
     * 视频生成模型配置
     */
    private Video video = new Video();

    /**
     * 向量引擎中转站配置类
     */
    @Data
    public static class VectorEngine {
        /**
         * API基础URL
         */
        private String baseUrl = "https://api.apilingguang.top";

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 请求超时时间（毫秒）
         */
        private Long timeout = 60000L;

        /**
         * 连接超时时间（毫秒）
         */
        private Long connectTimeout = 10000L;

        /**
         * 读取超时时间（毫秒）
         */
        private Long readTimeout = 60000L;
    }

    /**
     * 文本生成模型配置类
     */
    @Data
    public static class Text {
        /**
         * 默认模型
         */
        private String model = "gemini-3-pro-preview";

        /**
         * 最大token数
         */
        private Integer maxTokens = 4096;

        /**
         * 温度参数（0-1，越高越随机）
         */
        private Double temperature = 0.7;

        /**
         * 采样参数
         */
        private Double topP = 0.9;
    }

    /**
     * 图片生成模型配置类
     */
    @Data
    public static class Image {
        /**
         * 默认模型
         */
        private String defaultModel = "gemini-3-pro-image-preview";

        /**
         * 是否启用即梦反代
         */
        private Boolean jimengProxyEnabled = true;

        /**
         * 即梦模型
         */
        private String jimengModel = "jimeng-4.5";

        /**
         * 默认画幅比例
         */
        private String defaultAspectRatio = "21:9";

        /**
         * 默认生成数量
         */
        private Integer defaultCount = 1;

        /**
         * 即梦反代配置
         */
        private JimengProxy jimengProxy = new JimengProxy();
    }

    /**
     * 即梦反代配置类
     */
    @Data
    public static class JimengProxy {
        /**
         * 反代服务器基础URL
         */
        private String baseUrl = "http://47.83.129.72:5100";

        /**
         * 会话ID（用于认证）
         */
        private String sessionid;

        /**
         * 是否启用即梦反代
         */
        private Boolean enabled = true;

        /**
         * 文生图API端点路径（默认 /v1/images/generations）
         */
        private String textToImageEndpoint = "/v1/images/generations";

        /**
         * 图生图API端点路径（默认 /v1/images/compositions）
         */
        private String imageToImageEndpoint = "/v1/images/compositions";

        /**
         * 连接超时时间（毫秒）
         */
        private Long connectTimeout = 30000L;

        /**
         * 读取超时时间（毫秒）
         */
        private Long readTimeout = 60000L;
    }

    /**
     * 视频生成模型配置类
     */
    @Data
    public static class Video {
        /**
         * 默认模型
         */
        private String model = "sora-2-all";

        /**
         * 默认画幅比例
         */
        private String defaultAspectRatio = "16:9";

        /**
         * 默认时长（秒）
         */
        private Integer defaultDuration = 5;

        /**
         * 最大时长（秒）
         */
        private Integer maxDuration = 15;

        /**
         * 轮询间隔（毫秒）
         */
        private Long pollInterval = 5000L;

        /**
         * 最大轮询次数
         */
        private Integer maxPollCount = 120;
    }
}
// {{END_MODIFICATIONS}}
