package com.ym.ai_story_studio_server.dto.ai;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * 批量生成请求DTO
 *
 * <p>用于批量生成分镜图、视频、角色画像、场景画像等AI内容
 *
 * <p><strong>支持的生成模式:</strong>
 * <ul>
 *   <li>ALL - 全部生成(对所有目标执行生成操作)</li>
 *   <li>MISSING - 仅缺失生成(仅对尚未生成的目标执行生成)</li>
 * </ul>
 *
 * <p><strong>使用场景:</strong>
 * <ul>
 *   <li>批量生成分镜图 - targetIds为分镜ID列表</li>
 *   <li>批量生成视频 - targetIds为分镜ID列表</li>
 *   <li>批量生成角色画像 - targetIds为角色ID列表</li>
 *   <li>批量生成场景画像 - targetIds为场景ID列表</li>
 * </ul>
 *
 * <p><strong>使用示例:</strong>
 * <pre>
 * // 批量生成分镜图(全部生成,每个生成1张)
 * BatchGenerateRequest request = new BatchGenerateRequest(
 *     Arrays.asList(1L, 2L, 3L),  // 分镜ID列表
 *     "ALL",                       // 全部生成
 *     1,                           // 每个分镜生成1张图
 *     "21:9",                      // 画幅比例
 *     "jimeng-4.5"                 // 使用即梦模型
 * );
 *
 * // 批量生成视频(仅缺失,每个生成1个)
 * BatchGenerateRequest request = new BatchGenerateRequest(
 *     Arrays.asList(1L, 2L, 3L),  // 分镜ID列表
 *     "MISSING",                   // 仅缺失生成
 *     1,                           // 每个分镜生成1个视频
 *     "16:9",                      // 视频画幅
 *     "sora-2"                     // 使用Sora模型
 * );
 * </pre>
 *
 * <p><strong>注意事项:</strong>
 * <ul>
 *   <li>批量操作会创建异步任务,不会阻塞HTTP请求</li>
 *   <li>每个目标会生成countPerItem个资产版本</li>
 *   <li>model和aspectRatio参数可选,默认使用配置文件中的值</li>
 *   <li>MISSING模式会自动跳过已有资产的目标,避免重复生成</li>
 * </ul>
 *
 * @param targetIds 目标ID列表(分镜ID/角色ID/场景ID),必填,至少1个
 * @param mode 生成模式(ALL-全部生成, MISSING-仅缺失生成),必填
 * @param countPerItem 每个目标生成的数量,可选,默认1,最大10
 * @param aspectRatio 画幅比例(可选,默认使用配置值)
 * @param model 模型名称(可选,默认使用配置值)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record BatchGenerateRequest(
        @NotEmpty(message = "目标ID列表不能为空")
        List<Long> targetIds,

        @NotNull(message = "生成模式不能为空")
        @Pattern(regexp = "ALL|MISSING", message = "生成模式必须为ALL或MISSING")
        String mode,

        @Min(value = 1, message = "每个目标至少生成1个")
        Integer countPerItem,

        @Pattern(regexp = "1:1|16:9|9:16|21:9", message = "不支持的画幅比例")
        String aspectRatio,

        String model
) {
    /**
     * 获取每个目标的生成数量(默认值处理)
     *
     * @return 生成数量,默认为1
     */
    public int getCountPerItemOrDefault() {
        return countPerItem != null ? countPerItem : 1;
    }
}
