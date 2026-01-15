package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.character.CategoryVO;
import com.ym.ai_story_studio_server.dto.character.CreateCategoryRequest;
import com.ym.ai_story_studio_server.dto.character.UpdateCategoryRequest;

import java.util.List;

/**
 * 角色分类服务接口
 *
 * <p>提供角色分类的CRUD功能（账号级）
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface CharacterCategoryService {

    /**
     * 获取用户的角色分类列表(按排序值排序)
     *
     * @param userId 当前用户ID
     * @return 分类VO列表
     */
    List<CategoryVO> getCategoryList(Long userId);

    /**
     * 创建角色分类
     *
     * @param userId 当前用户ID
     * @param request 创建分类请求
     * @return 分类VO
     */
    CategoryVO createCategory(Long userId, CreateCategoryRequest request);

    /**
     * 更新角色分类(重命名)
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID
     * @param request 更新分类请求
     */
    void updateCategory(Long userId, Long categoryId, UpdateCategoryRequest request);

    /**
     * 删除角色分类
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID
     */
    void deleteCategory(Long userId, Long categoryId);
}
