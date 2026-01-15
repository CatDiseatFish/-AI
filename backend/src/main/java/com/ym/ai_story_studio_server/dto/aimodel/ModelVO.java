// {{CODE-Cycle-Integration:
//   Task_ID: [#T-BACKEND-MODEL-API]
//   Timestamp: 2026-01-04T15:55:39+08:00
//   Phase: [D-Develop]
//   Context-Analysis: "Creating ModelVO for API response, containing minimal fields needed by frontend."
//   Principle_Applied: "KISS, DRY, YAGNI"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.dto.aimodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI模型VO
 * 前端展示用的模型信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelVO {

    /**
     * 模型ID
     */
    private Long id;

    /**
     * 模型代码（用于存储和识别）
     */
    private String code;

    /**
     * 模型显示名称
     */
    private String name;

    /**
     * 模型类型: LANGUAGE, IMAGE, VIDEO
     */
    private String type;

    /**
     * 模型提供商
     */
    private String provider;

    /**
     * 是否启用
     */
    private Integer enabled;
}
// {{END_MODIFICATIONS}}
