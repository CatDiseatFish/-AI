package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.character.CategoryVO;
import com.ym.ai_story_studio_server.dto.character.CreateCategoryRequest;
import com.ym.ai_story_studio_server.dto.character.UpdateCategoryRequest;
import com.ym.ai_story_studio_server.service.CharacterCategoryService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色分类控制器
 *
 * <p>处理角色分类相关接口,需要JWT认证
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/character-categories")
@RequiredArgsConstructor
public class CharacterCategoryController {

    private final CharacterCategoryService categoryService;

    /**
     * 获取角色分类列表
     *
     * @return 分类列表响应
     */
    @GetMapping
    public Result<List<CategoryVO>> getCategoryList() {
        Long userId = UserContext.getUserId();
        log.info("获取角色分类列表, userId: {}", userId);
        List<CategoryVO> categories = categoryService.getCategoryList(userId);
        return Result.success(categories);
    }

    /**
     * 创建角色分类
     *
     * @param request 创建分类请求
     * @return 分类VO响应
     */
    @PostMapping
    public Result<CategoryVO> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        Long userId = UserContext.getUserId();
        log.info("创建角色分类, userId: {}, name: {}", userId, request.name());
        CategoryVO category = categoryService.createCategory(userId, request);
        return Result.success(category);
    }

    /**
     * 更新角色分类
     *
     * @param categoryId 分类ID
     * @param request 更新分类请求
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateCategory(
            @PathVariable("id") Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        Long userId = UserContext.getUserId();
        log.info("更新角色分类, userId: {}, categoryId: {}", userId, categoryId);
        categoryService.updateCategory(userId, categoryId, request);
        return Result.success();
    }

    /**
     * 删除角色分类
     *
     * @param categoryId 分类ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable("id") Long categoryId) {
        Long userId = UserContext.getUserId();
        log.info("删除角色分类, userId: {}, categoryId: {}", userId, categoryId);
        categoryService.deleteCategory(userId, categoryId);
        return Result.success();
    }
}
