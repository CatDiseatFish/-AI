package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.scene.CreateSceneRequest;
import com.ym.ai_story_studio_server.dto.scene.SceneVO;
import com.ym.ai_story_studio_server.dto.scene.UpdateSceneRequest;
import com.ym.ai_story_studio_server.entity.SceneCategory;
import com.ym.ai_story_studio_server.entity.SceneLibrary;
import com.ym.ai_story_studio_server.entity.ProjectScene;
import com.ym.ai_story_studio_server.mapper.SceneCategoryMapper;
import com.ym.ai_story_studio_server.mapper.SceneLibraryMapper;
import com.ym.ai_story_studio_server.mapper.ProjectSceneMapper;
import com.ym.ai_story_studio_server.service.SceneLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 场景库服务实现类
 *
 * <p>提供场景库的CRUD功能实现
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneLibraryServiceImpl implements SceneLibraryService {

    private final SceneLibraryMapper sceneLibraryMapper;
    private final SceneCategoryMapper categoryMapper;
    private final ProjectSceneMapper projectSceneMapper;

    /**
     * 获取场景库列表(支持搜索和筛选)
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID(可选)
     * @param keyword 搜索关键词(可选)
     * @return 场景VO列表
     */
    @Override
    public List<SceneVO> getSceneList(Long userId, Long categoryId, String keyword) {
        log.info("获取场景库列表, userId: {}, categoryId: {}, keyword: {}", userId, categoryId, keyword);

        // 构建查询条件
        LambdaQueryWrapper<SceneLibrary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SceneLibrary::getUserId, userId)
                .isNull(SceneLibrary::getDeletedAt);

        // 动态条件
        if (categoryId != null) {
            queryWrapper.eq(SceneLibrary::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(SceneLibrary::getName, keyword);
        }

        queryWrapper.orderByDesc(SceneLibrary::getUpdatedAt);

        // 执行查询
        List<SceneLibrary> scenes = sceneLibraryMapper.selectList(queryWrapper);

        // 转换为VO
        return scenes.stream().map(scene -> {
            // 查询分类名称
            String categoryName = null;
            if (scene.getCategoryId() != null) {
                SceneCategory category = categoryMapper.selectById(scene.getCategoryId());
                if (category != null) {
                    categoryName = category.getName();
                }
            }

            // 统计引用次数
            LambdaQueryWrapper<ProjectScene> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(ProjectScene::getLibrarySceneId, scene.getId());
            long referenceCount = projectSceneMapper.selectCount(countWrapper);

            return new SceneVO(
                    scene.getId(),
                    scene.getCategoryId(),
                    categoryName,
                    scene.getName(),
                    scene.getDescription(),
                    scene.getThumbnailUrl(),
                    (int) referenceCount,
                    scene.getCreatedAt(),
                    scene.getUpdatedAt()
            );
        }).collect(Collectors.toList());
    }

    /**
     * 创建场景
     *
     * @param userId 当前用户ID
     * @param request 创建场景请求
     * @return 场景VO
     */
    @Override
    public SceneVO createScene(Long userId, CreateSceneRequest request) {
        log.info("创建场景, userId: {}, name: {}", userId, request.name());

        // 如果指定了分类,验证分类是否存在且属于当前用户
        if (request.categoryId() != null) {
            SceneCategory category = categoryMapper.selectById(request.categoryId());
            if (category == null) {
                throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "场景分类不存在");
            }
            if (!category.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.ACCESS_DENIED);
            }
        }

        // 创建场景实体
        SceneLibrary scene = new SceneLibrary();
        scene.setUserId(userId);
        scene.setCategoryId(request.categoryId());
        scene.setName(request.name());
        scene.setDescription(request.description());

        // 保存到数据库
        sceneLibraryMapper.insert(scene);

        log.info("场景创建成功, sceneId: {}", scene.getId());

        // 查询分类名称
        String categoryName = null;
        if (scene.getCategoryId() != null) {
            SceneCategory category = categoryMapper.selectById(scene.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        return new SceneVO(
                scene.getId(),
                scene.getCategoryId(),
                categoryName,
                scene.getName(),
                scene.getDescription(),
                scene.getThumbnailUrl(),
                0, // 新创建的场景引用次数为0
                scene.getCreatedAt(),
                scene.getUpdatedAt()
        );
    }

    /**
     * 更新场景
     *
     * @param userId 当前用户ID
     * @param sceneId 场景ID
     * @param request 更新场景请求
     */
    @Override
    public void updateScene(Long userId, Long sceneId, UpdateSceneRequest request) {
        log.info("更新场景, userId: {}, sceneId: {}", userId, sceneId);

        // 查询场景是否存在且未删除
        SceneLibrary scene = sceneLibraryMapper.selectById(sceneId);
        if (scene == null || scene.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "场景不存在");
        }

        // 验证是否属于当前用户
        if (!scene.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 如果指定了新分类,验证分类是否存在且属于当前用户
        if (request.categoryId() != null) {
            SceneCategory category = categoryMapper.selectById(request.categoryId());
            if (category == null) {
                throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "场景分类不存在");
            }
            if (!category.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.ACCESS_DENIED);
            }
        }

        // 更新字段(所有字段可选)
        if (request.name() != null) {
            scene.setName(request.name());
        }
        if (request.categoryId() != null) {
            scene.setCategoryId(request.categoryId());
        }
        if (request.description() != null) {
            scene.setDescription(request.description());
        }
        if (request.thumbnailUrl() != null) {
            scene.setThumbnailUrl(request.thumbnailUrl());
        }

        // 保存到数据库
        sceneLibraryMapper.updateById(scene);

        log.info("场景更新成功, sceneId: {}", sceneId);
    }

    /**
     * 删除场景(软删除)
     *
     * @param userId 当前用户ID
     * @param sceneId 场景ID
     */
    @Override
    public void deleteScene(Long userId, Long sceneId) {
        log.info("删除场景, userId: {}, sceneId: {}", userId, sceneId);

        // 查询场景是否存在且未删除
        SceneLibrary scene = sceneLibraryMapper.selectById(sceneId);
        if (scene == null || scene.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "场景不存在");
        }

        // 验证是否属于当前用户
        if (!scene.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 检查是否被项目引用(允许删除,但记录日志)
        LambdaQueryWrapper<ProjectScene> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(ProjectScene::getLibrarySceneId, sceneId);
        long referenceCount = projectSceneMapper.selectCount(checkWrapper);
        if (referenceCount > 0) {
            log.warn("场景被{}个项目引用,删除后可能影响这些项目, sceneId: {}", referenceCount, sceneId);
        }

        // 软删除(设置deleted_at)
        scene.setDeletedAt(LocalDateTime.now());
        sceneLibraryMapper.updateById(scene);

        log.info("场景删除成功, sceneId: {}", sceneId);
    }
}
