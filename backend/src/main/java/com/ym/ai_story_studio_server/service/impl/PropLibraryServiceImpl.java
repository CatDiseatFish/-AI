package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.prop.CreatePropRequest;
import com.ym.ai_story_studio_server.dto.prop.PropVO;
import com.ym.ai_story_studio_server.dto.prop.UpdatePropRequest;
import com.ym.ai_story_studio_server.entity.PropCategory;
import com.ym.ai_story_studio_server.entity.PropLibrary;
import com.ym.ai_story_studio_server.entity.ProjectProp;
import com.ym.ai_story_studio_server.mapper.PropCategoryMapper;
import com.ym.ai_story_studio_server.mapper.PropLibraryMapper;
import com.ym.ai_story_studio_server.mapper.ProjectPropMapper;
import com.ym.ai_story_studio_server.service.PropLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 道具库服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropLibraryServiceImpl implements PropLibraryService {

    private final PropLibraryMapper propLibraryMapper;
    private final PropCategoryMapper categoryMapper;
    private final ProjectPropMapper projectPropMapper;

    @Override
    public List<PropVO> getPropList(Long userId, Long categoryId, String keyword) {
        log.info("获取道具库列表, userId: {}, categoryId: {}, keyword: {}", userId, categoryId, keyword);

        LambdaQueryWrapper<PropLibrary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PropLibrary::getUserId, userId)
                .isNull(PropLibrary::getDeletedAt);

        if (categoryId != null) {
            queryWrapper.eq(PropLibrary::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(PropLibrary::getName, keyword);
        }

        queryWrapper.orderByDesc(PropLibrary::getUpdatedAt);

        List<PropLibrary> props = propLibraryMapper.selectList(queryWrapper);

        return props.stream().map(prop -> {
            String categoryName = null;
            if (prop.getCategoryId() != null) {
                PropCategory category = categoryMapper.selectById(prop.getCategoryId());
                if (category != null) {
                    categoryName = category.getName();
                }
            }

            LambdaQueryWrapper<ProjectProp> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(ProjectProp::getLibraryPropId, prop.getId());
            long referenceCount = projectPropMapper.selectCount(countWrapper);

            return new PropVO(
                    prop.getId(),
                    prop.getCategoryId(),
                    categoryName,
                    prop.getName(),
                    prop.getDescription(),
                    prop.getThumbnailUrl(),
                    (int) referenceCount,
                    prop.getCreatedAt(),
                    prop.getUpdatedAt()
            );
        }).collect(Collectors.toList());
    }

    @Override
    public PropVO createProp(Long userId, CreatePropRequest request) {
        log.info("创建道具, userId: {}, name: {}", userId, request.name());

        if (request.categoryId() != null) {
            PropCategory category = categoryMapper.selectById(request.categoryId());
            if (category == null) {
                throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "道具分类不存在");
            }
            if (!category.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.ACCESS_DENIED);
            }
        }

        PropLibrary prop = new PropLibrary();
        prop.setUserId(userId);
        prop.setCategoryId(request.categoryId());
        prop.setName(request.name());
        prop.setDescription(request.description());
        prop.setThumbnailUrl(request.thumbnailUrl());

        propLibraryMapper.insert(prop);

        log.info("道具创建成功, propId: {}", prop.getId());

        String categoryName = null;
        if (prop.getCategoryId() != null) {
            PropCategory category = categoryMapper.selectById(prop.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        return new PropVO(
                prop.getId(),
                prop.getCategoryId(),
                categoryName,
                prop.getName(),
                prop.getDescription(),
                prop.getThumbnailUrl(),
                0,
                prop.getCreatedAt(),
                prop.getUpdatedAt()
        );
    }

    @Override
    public void updateProp(Long userId, Long propId, UpdatePropRequest request) {
        log.info("更新道具, userId: {}, propId: {}", userId, propId);

        PropLibrary prop = propLibraryMapper.selectById(propId);
        if (prop == null || prop.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "道具不存在");
        }

        if (!prop.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        if (request.categoryId() != null) {
            PropCategory category = categoryMapper.selectById(request.categoryId());
            if (category == null) {
                throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "道具分类不存在");
            }
            if (!category.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.ACCESS_DENIED);
            }
        }

        if (request.name() != null) {
            prop.setName(request.name());
        }
        if (request.categoryId() != null) {
            prop.setCategoryId(request.categoryId());
        }
        if (request.description() != null) {
            prop.setDescription(request.description());
        }
        if (request.thumbnailUrl() != null) {
            prop.setThumbnailUrl(request.thumbnailUrl());
        }

        propLibraryMapper.updateById(prop);

        log.info("道具更新成功, propId: {}", propId);
    }

    @Override
    public void deleteProp(Long userId, Long propId) {
        log.info("删除道具, userId: {}, propId: {}", userId, propId);

        PropLibrary prop = propLibraryMapper.selectById(propId);
        if (prop == null || prop.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "道具不存在");
        }

        if (!prop.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        LambdaQueryWrapper<ProjectProp> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(ProjectProp::getLibraryPropId, propId);
        long referenceCount = projectPropMapper.selectCount(checkWrapper);
        if (referenceCount > 0) {
            log.warn("道具被{}个项目引用,删除后可能影响这些项目, propId: {}", referenceCount, propId);
        }

        prop.setDeletedAt(LocalDateTime.now());
        propLibraryMapper.updateById(prop);

        log.info("道具删除成功, propId: {}", propId);
    }
}
