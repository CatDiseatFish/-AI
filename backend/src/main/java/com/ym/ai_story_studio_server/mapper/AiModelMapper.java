// {{CODE-Cycle-Integration:
//   Task_ID: [#T-BACKEND-MODEL-API]
//   Timestamp: 2026-01-04T15:55:39+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Creating AiModelMapper following StylePresetMapper pattern. Extends MyBatis-Plus BaseMapper."
//   Principle_Applied: "KISS, DRY, SOLID"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ym.ai_story_studio_server.entity.AiModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI模型配置表 Mapper
 */
@Mapper
public interface AiModelMapper extends BaseMapper<AiModel> {
}
// {{END_MODIFICATIONS}}
