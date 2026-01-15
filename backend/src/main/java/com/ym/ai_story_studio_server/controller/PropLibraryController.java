package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.prop.CreatePropRequest;
import com.ym.ai_story_studio_server.dto.prop.PropCategoryVO;
import com.ym.ai_story_studio_server.dto.prop.PropVO;
import com.ym.ai_story_studio_server.dto.prop.UpdatePropRequest;
import com.ym.ai_story_studio_server.service.PropCategoryService;
import com.ym.ai_story_studio_server.service.PropLibraryService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 道具库控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/library/props")
@RequiredArgsConstructor
public class PropLibraryController {

    private final PropLibraryService propLibraryService;
    private final PropCategoryService categoryService;

    /**
     * 获取道具分类列表
     */
    @GetMapping("/categories")
    public Result<List<PropCategoryVO>> getCategories() {
        Long userId = UserContext.getUserId();
        log.info("获取道具分类列表, userId: {}", userId);
        List<PropCategoryVO> categories = categoryService.getCategoryList(userId);
        return Result.success(categories);
    }

    /**
     * 创建道具分类
     */
    @PostMapping("/categories")
    public Result<PropCategoryVO> createCategory(@RequestBody Map<String, String> request) {
        Long userId = UserContext.getUserId();
        String name = request.get("name");
        log.info("创建道具分类, userId: {}, name: {}", userId, name);
        PropCategoryVO category = categoryService.createCategory(userId, name);
        return Result.success(category);
    }

    /**
     * 更新道具分类
     */
    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(
            @PathVariable("id") Long categoryId,
            @RequestBody Map<String, String> request) {
        Long userId = UserContext.getUserId();
        String name = request.get("name");
        log.info("更新道具分类, userId: {}, categoryId: {}, name: {}", userId, categoryId, name);
        categoryService.updateCategory(userId, categoryId, name);
        return Result.success();
    }

    /**
     * 删除道具分类
     */
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable("id") Long categoryId) {
        Long userId = UserContext.getUserId();
        log.info("删除道具分类, userId: {}, categoryId: {}", userId, categoryId);
        categoryService.deleteCategory(userId, categoryId);
        return Result.success();
    }

    /**
     * 获取道具库列表
     */
    @GetMapping
    public Result<List<PropVO>> getPropList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Long userId = UserContext.getUserId();
        log.info("获取道具库列表, userId: {}, categoryId: {}, keyword: {}", userId, categoryId, keyword);
        List<PropVO> props = propLibraryService.getPropList(userId, categoryId, keyword);
        return Result.success(props);
    }

    /**
     * 创建道具
     */
    @PostMapping
    public Result<PropVO> createProp(@Valid @RequestBody CreatePropRequest request) {
        Long userId = UserContext.getUserId();
        log.info("创建道具, userId: {}, name: {}", userId, request.name());
        PropVO prop = propLibraryService.createProp(userId, request);
        return Result.success(prop);
    }

    /**
     * 更新道具
     */
    @PutMapping("/{id}")
    public Result<Void> updateProp(
            @PathVariable("id") Long propId,
            @Valid @RequestBody UpdatePropRequest request) {
        Long userId = UserContext.getUserId();
        log.info("更新道具, userId: {}, propId: {}", userId, propId);
        propLibraryService.updateProp(userId, propId, request);
        return Result.success();
    }

    /**
     * 删除道具
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProp(@PathVariable("id") Long propId) {
        Long userId = UserContext.getUserId();
        log.info("删除道具, userId: {}, propId: {}", userId, propId);
        propLibraryService.deleteProp(userId, propId);
        return Result.success();
    }
}
