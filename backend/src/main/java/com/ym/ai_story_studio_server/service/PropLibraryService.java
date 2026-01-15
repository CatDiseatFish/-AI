package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.prop.CreatePropRequest;
import com.ym.ai_story_studio_server.dto.prop.PropVO;
import com.ym.ai_story_studio_server.dto.prop.UpdatePropRequest;

import java.util.List;

/**
 * 道具库服务接口
 *
 * <p>提供道具库的CRUD功能
 */
public interface PropLibraryService {

    /**
     * 获取道具库列表(支持搜索和筛选)
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID(可选)
     * @param keyword 搜索关键词(可选)
     * @return 道具VO列表
     */
    List<PropVO> getPropList(Long userId, Long categoryId, String keyword);

    /**
     * 创建道具
     *
     * @param userId 当前用户ID
     * @param request 创建道具请求
     * @return 道具VO
     */
    PropVO createProp(Long userId, CreatePropRequest request);

    /**
     * 更新道具
     *
     * @param userId 当前用户ID
     * @param propId 道具ID
     * @param request 更新道具请求
     */
    void updateProp(Long userId, Long propId, UpdatePropRequest request);

    /**
     * 删除道具(软删除)
     *
     * @param userId 当前用户ID
     * @param propId 道具ID
     */
    void deleteProp(Long userId, Long propId);
}
