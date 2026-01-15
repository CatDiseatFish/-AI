package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.scene.CreateSceneRequest;
import com.ym.ai_story_studio_server.dto.scene.SceneVO;
import com.ym.ai_story_studio_server.dto.scene.UpdateSceneRequest;

import java.util.List;

/**
 * 场景库服务接口
 *
 * <p>提供场景库的CRUD功能
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface SceneLibraryService {

    /**
     * 获取场景库列表(支持搜索和筛选)
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID(可选)
     * @param keyword 搜索关键词(可选)
     * @return 场景VO列表
     */
    List<SceneVO> getSceneList(Long userId, Long categoryId, String keyword);

    /**
     * 创建场景
     *
     * @param userId 当前用户ID
     * @param request 创建场景请求
     * @return 场景VO
     */
    SceneVO createScene(Long userId, CreateSceneRequest request);

    /**
     * 更新场景
     *
     * @param userId 当前用户ID
     * @param sceneId 场景ID
     * @param request 更新场景请求
     */
    void updateScene(Long userId, Long sceneId, UpdateSceneRequest request);

    /**
     * 删除场景(软删除)
     *
     * @param userId 当前用户ID
     * @param sceneId 场景ID
     */
    void deleteScene(Long userId, Long sceneId);
}
