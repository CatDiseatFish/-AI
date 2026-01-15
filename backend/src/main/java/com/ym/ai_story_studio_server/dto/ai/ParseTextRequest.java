package com.ym.ai_story_studio_server.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 文本解析请求DTO
 *
 * <p>用于将用户输入的小说/剧本文本解析为结构化的分镜脚本
 *
 * <p><strong>核心功能:</strong>
 * <ul>
 *   <li>调用AI模型分析文本内容</li>
 *   <li>自动提取场景、角色、对话等信息</li>
 *   <li>生成结构化的分镜列表</li>
 *   <li>自动创建项目角色和场景</li>
 * </ul>
 *
 * <p><strong>文本格式要求:</strong>
 * <ul>
 *   <li>支持小说格式(段落分隔)</li>
 *   <li>支持剧本格式(场景+对话)</li>
 *   <li>支持纯文本描述</li>
 *   <li>长度限制:10-50000个字符</li>
 * </ul>
 *
 * <p><strong>使用示例:</strong>
 * <pre>
 * ParseTextRequest request = new ParseTextRequest("""
 *     第一幕:未来城市
 *
 *     主角小明站在摩天大楼的楼顶,俯瞰着霓虹闪烁的城市。
 *     夜风吹拂着他的长发,眼神中透露出坚定。
 *
 *     小明:(自言自语) 今晚,一切都会改变。
 *
 *     第二幕:地下基地
 *
 *     小红在昏暗的实验室中操作着全息界面,屏幕上显示着复杂的数据。
 *     ...
 *     """);
 * </pre>
 *
 * <p><strong>解析结果:</strong>
 * <ul>
 *   <li>自动创建分镜记录(每个场景/段落对应一个分镜)</li>
 *   <li>提取角色列表并创建项目角色</li>
 *   <li>提取场景列表并创建项目场景</li>
 *   <li>自动绑定分镜与角色、场景的关联关系</li>
 * </ul>
 *
 * <p><strong>注意事项:</strong>
 * <ul>
 *   <li>文本解析是异步操作,会创建任务并立即返回任务ID</li>
 *   <li>解析过程可能需要较长时间,取决于文本长度</li>
 *   <li>解析会消耗积分,按文本生成的固定费用计费</li>
 *   <li>建议文本长度控制在10000字符以内以获得最佳效果</li>
 * </ul>
 *
 * @param rawText 原始文本内容(必填,10-50000字符)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ParseTextRequest(
        @NotBlank(message = "文本内容不能为空")
        @Size(min = 10, max = 50000, message = "文本长度必须在10-50000个字符之间")
        String rawText
) {
}
