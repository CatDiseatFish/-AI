package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.scene.CategoryVO;
import com.ym.ai_story_studio_server.dto.scene.CreateCategoryRequest;
import com.ym.ai_story_studio_server.dto.scene.UpdateCategoryRequest;

import java.util.List;

/**
 * 场景分类服务接口
 *
 * <p>提供场景分类的CRUD功能
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface SceneCategoryService {

    /**
     * 获取用户的场景分类列表(按排序值排序)
     *
     * @param userId 当前用户ID
     * @return 分类VO列表
     */
    List<CategoryVO> getCategoryList(Long userId);

    /**
     * 创建场景分类
     *
     * @param userId 当前用户ID
     * @param request 创建分类请求
     * @return 分类VO
     */
    CategoryVO createCategory(Long userId, CreateCategoryRequest request);

    /**
     * 更新场景分类(重命名)
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID
     * @param request 更新分类请求
     */
    void updateCategory(Long userId, Long categoryId, UpdateCategoryRequest request);

    /**
     * 删除场景分类
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID
     */
    void deleteCategory(Long userId, Long categoryId);
}
