// {{CODE-Cycle-Integration:
//   Task_ID: [#T-BACKEND-MODEL-API]
//   Timestamp: 2026-01-04T15:55:39+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Implementing AiModelService with MyBatis-Plus QueryWrapper for filtering enabled models by type."
//   Principle_Applied: "KISS, DRY, SOLID"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ym.ai_story_studio_server.dto.aimodel.ModelVO;
import com.ym.ai_story_studio_server.entity.AiModel;
import com.ym.ai_story_studio_server.mapper.AiModelMapper;
import com.ym.ai_story_studio_server.service.AiModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI模型服务实现
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelServiceImpl implements AiModelService {

    private final AiModelMapper aiModelMapper;

    @Override
    public List<ModelVO> getEnabledModelsByType(String type) {
        log.info("[AiModelService] 查询模型类型: {}", type);

        QueryWrapper<AiModel> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", type)
                    .eq("enabled", 1)
                    .orderByAsc("sort_order");

        List<AiModel> models = aiModelMapper.selectList(queryWrapper);
        log.info("[AiModelService] 查询到{}条{}类型的模型", models.size(), type);

        return models.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换Entity为VO
     */
    private ModelVO convertToVO(AiModel model) {
        return ModelVO.builder()
                .id(model.getId())
                .code(model.getCode())
                .name(model.getName())
                .type(model.getType())
                .provider(model.getProvider())
                .enabled(model.getEnabled())
                .build();
    }
}
// {{END_MODIFICATIONS}}
