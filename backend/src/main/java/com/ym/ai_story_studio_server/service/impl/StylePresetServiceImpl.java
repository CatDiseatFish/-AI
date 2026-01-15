// {{CODE-Cycle-Integration:
//   Task_ID: [#T011]
//   Timestamp: 2025-12-27T10:00:00+08:00
//   Phase: D-Develop
//   Context-Analysis: "Analyzed FolderServiceImpl/UserServiceImpl patterns. Implementing StylePresetService with LambdaQueryWrapper and Stream conversion."
//   Principle_Applied: "Verification-Mindset-Loop, KISS, SOLID"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ym.ai_story_studio_server.dto.stylepreset.StylePresetVO;
import com.ym.ai_story_studio_server.entity.StylePreset;
import com.ym.ai_story_studio_server.mapper.StylePresetMapper;
import com.ym.ai_story_studio_server.service.StylePresetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 风格预设服务实现类
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StylePresetServiceImpl implements StylePresetService {

    private final StylePresetMapper stylePresetMapper;

    @Override
    public List<StylePresetVO> getEnabledStylePresets() {
        log.info("查询已启用的风格预设列表");

        // 1. 构建查询条件: enabled = 1, 按sort_order升序排序
        LambdaQueryWrapper<StylePreset> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StylePreset::getEnabled, 1)
                .orderByAsc(StylePreset::getSortOrder);

        // 2. 执行查询
        List<StylePreset> stylePresets = stylePresetMapper.selectList(queryWrapper);

        log.info("查询到{}条已启用的风格预设", stylePresets.size());

        // 3. 转换为VO列表
        List<StylePresetVO> voList = stylePresets.stream()
                .map(entity -> new StylePresetVO(
                        entity.getId(),
                        entity.getCode(),
                        entity.getName(),
                        entity.getThumbnailUrl(),
                        entity.getPromptPrefix(),
                        entity.getSortOrder(),
                        entity.getEnabled(),
                        entity.getCreatedAt()
                ))
                .collect(Collectors.toList());

        log.info("风格预设列表转换完成,返回{}条记录", voList.size());
        return voList;
    }
}
// {{END_MODIFICATIONS}}
