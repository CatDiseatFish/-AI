package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.prop.PropCategoryVO;

import java.util.List;

/**
 * 道具分类服务接口
 */
public interface PropCategoryService {

    /**
     * 获取分类列表
     *
     * @param userId 当前用户ID
     * @return 分类VO列表
     */
    List<PropCategoryVO> getCategoryList(Long userId);

    /**
     * 创建分类
     *
     * @param userId 当前用户ID
     * @param name 分类名称
     * @return 分类VO
     */
    PropCategoryVO createCategory(Long userId, String name);

    /**
     * 更新分类
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID
     * @param name 新名称
     */
    void updateCategory(Long userId, Long categoryId, String name);

    /**
     * 删除分类
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID
     */
    void deleteCategory(Long userId, Long categoryId);
}
