package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.prompttemplate.CreatePromptTemplateRequest;
import com.ym.ai_story_studio_server.dto.prompttemplate.PromptTemplateVO;
import com.ym.ai_story_studio_server.dto.prompttemplate.UpdatePromptTemplateRequest;

import java.util.List;

/**
 * 指令库/提示词模板服务接口
 *
 * <p>提供指令模板的查询、创建、更新和删除功能
 *
 * <p>业务规则:
 * <ul>
 *   <li>系统预设模板(isSystem=1, userId=NULL)只读,用户不可修改删除</li>
 *   <li>用户自定义模板(isSystem=0, userId=当前用户)可增删改</li>
 *   <li>模板名称在同一用户下同一类别内唯一</li>
 *   <li>支持按category筛选(CHARACTER/SCENE/SHOT/VIDEO/CUSTOM)</li>
 * </ul>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface PromptTemplateService {

    /**
     * 获取指令库列表
     *
     * <p>返回系统预设模板和用户自定义模板,按类别分组,按sortOrder升序排序
     *
     * @param userId 用户ID
     * @param category 模板类别(可选,为null时返回所有类别)
     * @return 指令模板列表
     */
    List<PromptTemplateVO> getTemplateList(Long userId, String category);

    /**
     * 创建指令模板
     *
     * <p>创建用户自定义模板,自动设置isSystem=0和userId
     *
     * @param userId 用户ID
     * @param request 创建请求
     * @return 创建的模板VO
     */
    PromptTemplateVO createTemplate(Long userId, CreatePromptTemplateRequest request);

    /**
     * 更新指令模板
     *
     * <p>仅允许用户更新自己创建的模板,不允许修改系统预设模板
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     * @param request 更新请求
     */
    void updateTemplate(Long userId, Long templateId, UpdatePromptTemplateRequest request);

    /**
     * 删除指令模板
     *
     * <p>仅允许用户删除自己创建的模板,不允许删除系统预设模板
     *
     * @param userId 用户ID
     * @param templateId 模板ID
     */
    void deleteTemplate(Long userId, Long templateId);
}
