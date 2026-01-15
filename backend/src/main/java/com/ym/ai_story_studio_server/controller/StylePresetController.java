// {{CODE-Cycle-Integration:
//   Task_ID: [#T011]
//   Timestamp: 2025-12-27T10:00:00+08:00
//   Phase: D-Develop
//   Context-Analysis: "Analyzed UserController/FolderController patterns. Creating StylePresetController for style preset endpoints. No JWT required as this is system-level data."
//   Principle_Applied: "Verification-Mindset-Loop, KISS, SOLID"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.annotation.NoAuth;
import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.stylepreset.StylePresetVO;
import com.ym.ai_story_studio_server.service.StylePresetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 风格预设控制器
 *
 * <p>处理风格预设相关接口,无需JWT认证(系统级配置数据)
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@NoAuth
@RestController
@RequestMapping("/api/style-presets")
@RequiredArgsConstructor
public class StylePresetController {

    private final StylePresetService stylePresetService;

    /**
     * 获取风格预设列表
     *
     * <p>无需JWT认证,返回所有已启用的风格预设
     *
     * @return 风格预设列表
     */
    @GetMapping
    public Result<List<StylePresetVO>> getStylePresets() {
        log.info("收到获取风格预设列表请求");
        List<StylePresetVO> presets = stylePresetService.getEnabledStylePresets();
        log.info("返回{}条风格预设", presets.size());
        return Result.success(presets);
    }
}
// {{END_MODIFICATIONS}}
