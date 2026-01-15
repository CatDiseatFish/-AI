// {{CODE-Cycle-Integration:
//   Task_ID: [#T-BACKEND-MODEL-API]
//   Timestamp: 2026-01-04T15:55:39+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Creating AiModelService interface following StylePresetService pattern."
//   Principle_Applied: "KISS, DRY, SOLID"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.aimodel.ModelVO;

import java.util.List;

/**
 * AI模型服务接口
 *
 * <p>提供AI模型查询功能,模型配置是系统级数据
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface AiModelService {

    /**
     * 根据模型类型获取已启用的模型列表(按排序值升序)
     *
     * <p>模型类型: LANGUAGE(语言模型), IMAGE(图像生成), VIDEO(视频生成)
     *
     * @param type 模型类型
     * @return 模型VO列表
     */
    List<ModelVO> getEnabledModelsByType(String type);
}
// {{END_MODIFICATIONS}}
