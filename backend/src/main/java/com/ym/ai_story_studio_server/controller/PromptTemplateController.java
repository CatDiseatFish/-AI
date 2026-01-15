package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.prompttemplate.CreatePromptTemplateRequest;
import com.ym.ai_story_studio_server.dto.prompttemplate.PromptTemplateVO;
import com.ym.ai_story_studio_server.dto.prompttemplate.UpdatePromptTemplateRequest;
import com.ym.ai_story_studio_server.service.PromptTemplateService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 指令库/提示词模板控制器
 *
 * <p>提供指令模板的查询、创建、更新和删除接口
 *
 * <p>接口清单:
 * <ul>
 *   <li>GET /api/prompt-templates - 获取指令库列表</li>
 *   <li>POST /api/prompt-templates - 创建指令模板</li>
 *   <li>PUT /api/prompt-templates/{id} - 更新指令模板</li>
 *   <li>DELETE /api/prompt-templates/{id} - 删除指令模板</li>
 * </ul>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/prompt-templates")
@RequiredArgsConstructor
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;

    /**
     * 获取指令库列表
     *
     * <p>返回系统预设模板和用户自定义模板,支持按类别筛选
     *
     * @param category 模板类别(可选,为null时返回所有类别)
     * @return 指令模板列表
     */
    @GetMapping
    public Result<List<PromptTemplateVO>> getTemplateList(
            @RequestParam(required = false) String category) {
        Long userId = UserContext.getUserId();
        List<PromptTemplateVO> templates = promptTemplateService.getTemplateList(userId, category);
        return Result.success(templates);
    }

    /**
     * 创建指令模板
     *
     * <p>创建用户自定义模板
     *
     * @param request 创建请求
     * @return 创建的模板VO
     */
    @PostMapping
    public Result<PromptTemplateVO> createTemplate(@Valid @RequestBody CreatePromptTemplateRequest request) {
        Long userId = UserContext.getUserId();
        PromptTemplateVO template = promptTemplateService.createTemplate(userId, request);
        return Result.success("指令模板创建成功", template);
    }

    /**
     * 更新指令模板
     *
     * <p>仅允许用户更新自己创建的模板,不允许修改系统预设模板
     *
     * @param id 模板ID
     * @param request 更新请求
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateTemplate(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdatePromptTemplateRequest request) {
        Long userId = UserContext.getUserId();
        promptTemplateService.updateTemplate(userId, id, request);
        return Result.success();
    }

    /**
     * 删除指令模板
     *
     * <p>仅允许用户删除自己创建的模板,不允许删除系统预设模板
     *
     * @param id 模板ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        promptTemplateService.deleteTemplate(userId, id);
        return Result.success();
    }
}
