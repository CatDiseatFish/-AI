package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.scene.CategoryVO;
import com.ym.ai_story_studio_server.dto.scene.CreateCategoryRequest;
import com.ym.ai_story_studio_server.dto.scene.UpdateCategoryRequest;
import com.ym.ai_story_studio_server.entity.SceneCategory;
import com.ym.ai_story_studio_server.mapper.SceneCategoryMapper;
import com.ym.ai_story_studio_server.mapper.SceneLibraryMapper;
import com.ym.ai_story_studio_server.service.SceneCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 场景分类服务实现类
 *
 * <p>提供场景分类的CRUD功能实现
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneCategoryServiceImpl implements SceneCategoryService {

    private final SceneCategoryMapper categoryMapper;
    private final SceneLibraryMapper sceneLibraryMapper;

    /**
     * 获取用户的场景分类列表(按排序值排序)
     *
     * @param userId 当前用户ID
     * @return 分类VO列表
     */
    @Override
    public List<CategoryVO> getCategoryList(Long userId) {
        log.info("获取用户场景分类列表, userId: {}", userId);

        // 查询用户的所有分类(按排序值升序)
        LambdaQueryWrapper<SceneCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SceneCategory::getUserId, userId)
                .orderByAsc(SceneCategory::getSortOrder);
        List<SceneCategory> categories = categoryMapper.selectList(queryWrapper);

        // 转换为VO并统计场景数量
        return categories.stream().map(category -> {
            // 统计该分类下的场景数量
            LambdaQueryWrapper<com.ym.ai_story_studio_server.entity.SceneLibrary> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(com.ym.ai_story_studio_server.entity.SceneLibrary::getCategoryId, category.getId())
                    .isNull(com.ym.ai_story_studio_server.entity.SceneLibrary::getDeletedAt);
            long sceneCount = sceneLibraryMapper.selectCount(countWrapper);

            return new CategoryVO(
                    category.getId(),
                    category.getName(),
                    category.getSortOrder(),
                    (int) sceneCount,
                    category.getCreatedAt(),
                    category.getUpdatedAt()
            );
        }).collect(Collectors.toList());
    }

    /**
     * 创建场景分类
     *
     * @param userId 当前用户ID
     * @param request 创建分类请求
     * @return 分类VO
     */
    @Override
    public CategoryVO createCategory(Long userId, CreateCategoryRequest request) {
        log.info("创建场景分类, userId: {}, name: {}", userId, request.name());

        // 检查名称是否重复
        LambdaQueryWrapper<SceneCategory> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SceneCategory::getUserId, userId)
                .eq(SceneCategory::getName, request.name());
        long count = categoryMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "分类名称已存在");
        }

        // 获取最大排序值
        LambdaQueryWrapper<SceneCategory> maxWrapper = new LambdaQueryWrapper<>();
        maxWrapper.eq(SceneCategory::getUserId, userId)
                .orderByDesc(SceneCategory::getSortOrder)
                .last("LIMIT 1");
        SceneCategory maxCategory = categoryMapper.selectOne(maxWrapper);
        int nextSortOrder = (maxCategory == null) ? 0 : maxCategory.getSortOrder() + 1;

        // 创建分类实体
        SceneCategory category = new SceneCategory();
        category.setUserId(userId);
        category.setName(request.name());
        category.setSortOrder(nextSortOrder);

        // 保存到数据库
        categoryMapper.insert(category);

        log.info("场景分类创建成功, categoryId: {}", category.getId());

        return new CategoryVO(
                category.getId(),
                category.getName(),
                category.getSortOrder(),
                0, // 新创建的分类场景数量为0
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    /**
     * 更新场景分类(重命名)
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID
     * @param request 更新分类请求
     */
    @Override
    public void updateCategory(Long userId, Long categoryId, UpdateCategoryRequest request) {
        log.info("更新场景分类, userId: {}, categoryId: {}, newName: {}", userId, categoryId, request.name());

        // 查询分类是否存在
        SceneCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "场景分类不存在");
        }

        // 验证是否属于当前用户
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 检查新名称是否与其他分类重复
        LambdaQueryWrapper<SceneCategory> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(SceneCategory::getUserId, userId)
                .eq(SceneCategory::getName, request.name())
                .ne(SceneCategory::getId, categoryId);
        long count = categoryMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "分类名称已存在");
        }

        // 更新分类名称
        category.setName(request.name());
        categoryMapper.updateById(category);

        log.info("场景分类更新成功, categoryId: {}", categoryId);
    }

    /**
     * 删除场景分类
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID
     */
    @Override
    public void deleteCategory(Long userId, Long categoryId) {
        log.info("删除场景分类, userId: {}, categoryId: {}", userId, categoryId);

        // 查询分类是否存在
        SceneCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "场景分类不存在");
        }

        // 验证是否属于当前用户
        if (!category.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 检查分类下是否有场景
        LambdaQueryWrapper<com.ym.ai_story_studio_server.entity.SceneLibrary> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(com.ym.ai_story_studio_server.entity.SceneLibrary::getCategoryId, categoryId)
                .isNull(com.ym.ai_story_studio_server.entity.SceneLibrary::getDeletedAt);
        long sceneCount = sceneLibraryMapper.selectCount(checkWrapper);
        if (sceneCount > 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "分类下还有场景,请先移动或删除场景");
        }

        // 物理删除分类
        categoryMapper.deleteById(categoryId);

        log.info("场景分类删除成功, categoryId: {}", categoryId);
    }
}
