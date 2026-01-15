package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.character.CharacterVO;
import com.ym.ai_story_studio_server.dto.character.CategoryVO;
import com.ym.ai_story_studio_server.dto.character.CreateCharacterRequest;
import com.ym.ai_story_studio_server.dto.character.UpdateCharacterRequest;
import com.ym.ai_story_studio_server.service.CharacterCategoryService;
import com.ym.ai_story_studio_server.service.CharacterLibraryService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 角色库控制器
 *
 * <p>处理角色库相关接口,需要JWT认证
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/library/characters")
@RequiredArgsConstructor
public class CharacterLibraryController {

    private final CharacterLibraryService characterLibraryService;
    private final CharacterCategoryService categoryService;

    /**
     * 获取角色分类列表
     *
     * @return 分类列表响应
     */
    @GetMapping("/categories")
    public Result<List<CategoryVO>> getCategories() {
        Long userId = UserContext.getUserId();
        log.info("获取角色分类列表, userId: {}", userId);
        List<CategoryVO> categories = categoryService.getCategoryList(userId);
        return Result.success(categories);
    }

    /**
     * 获取角色库列表
     *
     * @param categoryId 分类ID(可选)
     * @param keyword 搜索关键词(可选)
     * @return 角色列表响应
     */
    @GetMapping
    public Result<List<CharacterVO>> getCharacterList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        Long userId = UserContext.getUserId();
        log.info("获取角色库列表, userId: {}, categoryId: {}, keyword: {}", userId, categoryId, keyword);
        List<CharacterVO> characters = characterLibraryService.getCharacterList(userId, categoryId, keyword);
        return Result.success(characters);
    }

    /**
     * 创建角色
     *
     * @param request 创建角色请求
     * @return 角色VO响应
     */
    @PostMapping
    public Result<CharacterVO> createCharacter(@Valid @RequestBody CreateCharacterRequest request) {
        Long userId = UserContext.getUserId();
        log.info("创建角色, userId: {}, name: {}", userId, request.name());
        CharacterVO character = characterLibraryService.createCharacter(userId, request);
        return Result.success(character);
    }

    /**
     * 更新角色
     *
     * @param characterId 角色ID
     * @param request 更新角色请求
     * @return 成功响应
     */
    @PutMapping("/{id}")
    public Result<Void> updateCharacter(
            @PathVariable("id") Long characterId,
            @Valid @RequestBody UpdateCharacterRequest request) {
        Long userId = UserContext.getUserId();
        log.info("更新角色, userId: {}, characterId: {}", userId, characterId);
        characterLibraryService.updateCharacter(userId, characterId, request);
        return Result.success();
    }

    /**
     * 删除角色
     *
     * @param characterId 角色ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCharacter(@PathVariable("id") Long characterId) {
        Long userId = UserContext.getUserId();
        log.info("删除角色, userId: {}, characterId: {}", userId, characterId);
        characterLibraryService.deleteCharacter(userId, characterId);
        return Result.success();
    }

    /**
     * 上传角色缩略图
     *
     * @param characterId 角色ID
     * @param file 图片文件
     * @return 上传后的URL
     */
    @PostMapping("/{id}/thumbnail")
    public Result<Map<String, String>> uploadThumbnail(
            @PathVariable("id") Long characterId,
            @RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getUserId();
        log.info("上传角色缩略图, userId: {}, characterId: {}, fileName: {}", 
                userId, characterId, file.getOriginalFilename());
        String url = characterLibraryService.uploadThumbnail(userId, characterId, file);
        return Result.success(Map.of("url", url));
    }
}
