package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.scene.CategoryVO;
import com.ym.ai_story_studio_server.dto.scene.CreateSceneRequest;
import com.ym.ai_story_studio_server.dto.scene.SceneVO;
import com.ym.ai_story_studio_server.dto.scene.UpdateSceneRequest;
import com.ym.ai_story_studio_server.service.SceneCategoryService;
import com.ym.ai_story_studio_server.service.SceneLibraryService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 场景库控制器
 *
 * <p>处理场景库相关接口,需要JWT认证
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/library/scenes")
@RequiredArgsConstructor
public class SceneLibraryController {

    private final SceneLibraryService sceneLibraryService;
    private final SceneCategoryService categoryService;

    /**
     * 获取场景分类列表
     *
     * @return 分类列表响应
     */
    @GetMapping("/categories")
    public Result<List<CategoryVO>> getCategories() {
        Long userId = UserContext.getUserId();
        log.info("获取场景分类列表, userId: {}", userId);
        List<CategoryVO> categories = categoryService.getCategoryList(userId);
        return Result.success(categories);
    }

    /**
     * 获取场景库列表
     *
     * @param categoryId 分类ID(可选)
     * @param keyword 搜索关键词(可选)
     * @return 场景列表响应
     */
    @GetMapping
    public Result<List<SceneVO>> getSceneList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Long userId = UserContext.getUserId();
        log.info("获取场景库列表, userId: {}, categoryId: {}, keyword: {}", userId, categoryId, keyword);
        List<SceneVO> scenes = sceneLibraryService.getSceneList(userId, categoryId, keyword);
        return Result.success(scenes);
    }

    /**
     * 创建场景
     *
     * @param request 创建场景请求
     * @return 场景VO响应
     */
    @PostMapping
    public Result<SceneVO> createScene(@Valid @RequestBody CreateSceneRequest request) {
        Long userId = UserContext.getUserId();
        log.info("创建场景, userId: {}, name: {}", userId, request.name());
        SceneVO scene = sceneLibraryService.createScene(userId, request);
        return Result.success(scene);
    }

    /**
     * 更新场景
     *
     * @param sceneId 场景ID
     * @param request 更新场景请求
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateScene(
            @PathVariable("id") Long sceneId,
            @Valid @RequestBody UpdateSceneRequest request) {
        Long userId = UserContext.getUserId();
        log.info("更新场景, userId: {}, sceneId: {}", userId, sceneId);
        sceneLibraryService.updateScene(userId, sceneId, request);
        return Result.success();
    }

    /**
     * 删除场景
     *
     * @param sceneId 场景ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteScene(@PathVariable("id") Long sceneId) {
        Long userId = UserContext.getUserId();
        log.info("删除场景, userId: {}, sceneId: {}", userId, sceneId);
        sceneLibraryService.deleteScene(userId, sceneId);
        return Result.success();
    }
}
