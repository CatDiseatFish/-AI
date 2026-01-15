package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.prop.PropCategoryVO;
import com.ym.ai_story_studio_server.entity.PropCategory;
import com.ym.ai_story_studio_server.entity.PropLibrary;
import com.ym.ai_story_studio_server.mapper.PropCategoryMapper;
import com.ym.ai_story_studio_server.mapper.PropLibraryMapper;
import com.ym.ai_story_studio_server.service.PropCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 道具分类服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropCategoryServiceImpl implements PropCategoryService {

    private final PropCategoryMapper categoryMapper;
    private final PropLibraryMapper propLibraryMapper;

    @Override
    public List<PropCategoryVO> getCategoryList(Long userId) {
        log.info("获取道具分类列表, userId: {}", userId);

        LambdaQueryWrapper<PropCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PropCategory::getUserId, userId)
                .orderByAsc(PropCategory::getSortOrder);

        List<PropCategory> categories = categoryMapper.selectList(queryWrapper);

        return categories.stream().map(category -> {
            // 统计该分类下的道具数量
            LambdaQueryWrapper<PropLibrary> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(PropLibrary::getCategoryId, category.getId())
                    .isNull(PropLibrary::getDeletedAt);
            long propCount = propLibraryMapper.selectCount(countWrapper);

            return new PropCategoryVO(
                    category.getId(),
                    category.getName(),
                    category.getSortOrder(),
                    (int) propCount,
                    category.getCreatedAt(),
                    category.getUpdatedAt()
            );
        }).collect(Collectors.toList());
    }

    @Override
    public PropCategoryVO createCategory(Long userId, String name) {
        log.info("创建道具分类, userId: {}, name: {}", userId, name);

        // 检查同名分类是否存在
        LambdaQueryWrapper<PropCategory> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(PropCategory::getUserId, userId)
                .eq(PropCategory::getName, name);
        if (categoryMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_RESOURCE, "分类名称已存在");
        }

        // 获取最大排序值
        LambdaQueryWrapper<PropCategory> maxOrderWrapper = new LambdaQueryWrapper<>();
        maxOrderWrapper.eq(PropCategory::getUserId, userId)
                .orderByDesc(PropCategory::getSortOrder)
                .last("LIMIT 1");
        PropCategory maxOrderCategory = categoryMapper.selectOne(maxOrderWrapper);
        int nextOrder = maxOrderCategory != null ? maxOrderCategory.getSortOrder() + 1 : 0;

        PropCategory category = new PropCategory();
        category.setUserId(userId);
        category.setName(name);
        category.setSortOrder(nextOrder);

        categoryMapper.insert(category);

        log.info("道具分类创建成功, categoryId: {}", category.getId());

        return new PropCategoryVO(
                category.getId(),
                category.getName(),
                category.getSortOrder(),
                0,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    @Override
    public void updateCategory(Long userId, Long categoryId, String name) {
        log.info("更新道具分类, userId: {}, categoryId: {}, name: {}", userId, categoryId, name);

        PropCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "分类不存在");
        }

        if (!category.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 检查同名分类是否存在
        LambdaQueryWrapper<PropCategory> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(PropCategory::getUserId, userId)
                .eq(PropCategory::getName, name)
                .ne(PropCategory::getId, categoryId);
        if (categoryMapper.selectCount(checkWrapper) > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_RESOURCE, "分类名称已存在");
        }

        category.setName(name);
        categoryMapper.updateById(category);

        log.info("道具分类更新成功, categoryId: {}", categoryId);
    }

    @Override
    public void deleteCategory(Long userId, Long categoryId) {
        log.info("删除道具分类, userId: {}, categoryId: {}", userId, categoryId);

        PropCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "分类不存在");
        }

        if (!category.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 将该分类下的道具移到"未分类"
        LambdaQueryWrapper<PropLibrary> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(PropLibrary::getCategoryId, categoryId);
        List<PropLibrary> props = propLibraryMapper.selectList(updateWrapper);
        for (PropLibrary prop : props) {
            prop.setCategoryId(null);
            propLibraryMapper.updateById(prop);
        }

        categoryMapper.deleteById(categoryId);

        log.info("道具分类删除成功, categoryId: {}", categoryId);
    }
}
