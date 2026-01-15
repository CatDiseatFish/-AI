// {{CODE-Cycle-Integration:
//   Task_ID: [#T011]
//   Timestamp: 2025-12-27T10:00:00+08:00
//   Phase: D-Develop
//   Context-Analysis: "Analyzed UserService/FolderService patterns. Creating StylePresetService interface for style preset operations."
//   Principle_Applied: "Verification-Mindset-Loop, KISS, SOLID"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.stylepreset.StylePresetVO;

import java.util.List;

/**
 * 风格预设服务接口
 *
 * <p>提供风格预设查询功能,风格预设是系统级配置数据
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface StylePresetService {

    /**
     * 获取所有已启用的风格预设列表(按排序值升序)
     *
     * <p>风格预设用于创建项目时选择图片/视频生成风格
     *
     * @return 风格预设VO列表
     */
    List<StylePresetVO> getEnabledStylePresets();
}
// {{END_MODIFICATIONS}}
