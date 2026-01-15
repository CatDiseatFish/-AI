// {{CODE-Cycle-Integration:
//   Task_ID: [#T-BACKEND-MODEL-API]
//   Timestamp: 2026-01-04T15:55:39+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Creating AiModelController following StylePresetController pattern. Uses @NoAuth for public access, @RequestParam for query parameter."
//   Principle_Applied: "KISS, DRY, SOLID"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.annotation.NoAuth;
import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.aimodel.ModelVO;
import com.ym.ai_story_studio_server.service.AiModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI模型配置控制器
 *
 * <p>处理AI模型查询接口,无需JWT认证(系统级配置数据)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@NoAuth
@RestController
@RequestMapping("/api/ai-models")
@RequiredArgsConstructor
public class AiModelController {

    private final AiModelService aiModelService;

    /**
     * 根据类型获取AI模型列表
     *
     * <p>无需JWT认证,返回指定类型的所有已启用模型
     *
     * @param type 模型类型: LANGUAGE, IMAGE, VIDEO
     * @return 模型列表
     */
    @GetMapping
    public Result<List<ModelVO>> getModelsByType(@RequestParam String type) {
        log.info("[AiModelController] 收到获取模型列表请求, type={}", type);
        List<ModelVO> models = aiModelService.getEnabledModelsByType(type);
        log.info("[AiModelController] 返回{}条模型", models.size());
        return Result.success(models);
    }
}
// {{END_MODIFICATIONS}}
