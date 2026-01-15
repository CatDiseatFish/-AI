package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.prompttemplate.CreatePromptTemplateRequest;
import com.ym.ai_story_studio_server.dto.prompttemplate.PromptTemplateVO;
import com.ym.ai_story_studio_server.dto.prompttemplate.UpdatePromptTemplateRequest;
import com.ym.ai_story_studio_server.entity.PromptTemplate;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.mapper.PromptTemplateMapper;
import com.ym.ai_story_studio_server.service.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 指令库/提示词模板服务实现类
 *
 * <p>实现指令模板的CRUD功能,包括系统预设模板和用户自定义模板的管理
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private final PromptTemplateMapper templateMapper;

    @Override
    public List<PromptTemplateVO> getTemplateList(Long userId, String category) {
        log.debug("查询指令模板列表: userId={}, category={}", userId, category);

        // 1. 构建查询条件: (系统预设模板 OR 用户自定义模板)
        LambdaQueryWrapper<PromptTemplate> queryWrapper = new LambdaQueryWrapper<>();

        // 系统预设模板(isSystem=1)或用户自定义模板(userId=当前用户)
        queryWrapper.and(wrapper -> wrapper
                .eq(PromptTemplate::getIsSystem, 1)
                .or()
                .eq(PromptTemplate::getUserId, userId)
        );

        // 按类别筛选(可选)
        if (category != null && !category.isBlank()) {
            queryWrapper.eq(PromptTemplate::getCategory, category);
        }

        // 按category和sortOrder排序
        queryWrapper.orderByAsc(PromptTemplate::getCategory)
                .orderByAsc(PromptTemplate::getSortOrder);

        List<PromptTemplate> templates = templateMapper.selectList(queryWrapper);

        // 2. 转换为VO
        return templates.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PromptTemplateVO createTemplate(Long userId, CreatePromptTemplateRequest request) {
        log.info("创建指令模板: userId={}, category={}, name={}", userId, request.category(), request.name());

        // 1. 检查名称是否重复(同一用户下同一类别内唯一)
        Long count = templateMapper.selectCount(
                new LambdaQueryWrapper<PromptTemplate>()
                        .eq(PromptTemplate::getUserId, userId)
                        .eq(PromptTemplate::getCategory, request.category())
                        .eq(PromptTemplate::getName, request.name())
        );

        if (count > 0) {
            log.warn("模板名称在该类别下已存在: userId={}, category={}, name={}",
                    userId, request.category(), request.name());
            throw new BusinessException(ResultCode.TEMPLATE_NAME_DUPLICATE);
        }

        // 2. 获取当前用户在该类别下的最大排序值
        LambdaQueryWrapper<PromptTemplate> maxWrapper = new LambdaQueryWrapper<>();
        maxWrapper.eq(PromptTemplate::getUserId, userId)
                .eq(PromptTemplate::getCategory, request.category())
                .orderByDesc(PromptTemplate::getSortOrder)
                .last("LIMIT 1");

        PromptTemplate lastTemplate = templateMapper.selectOne(maxWrapper);
        int nextSortOrder = (lastTemplate != null) ? lastTemplate.getSortOrder() + 1 : 0;

        // 3. 创建模板实体
        PromptTemplate template = new PromptTemplate();
        template.setUserId(userId);
        template.setCategory(request.category());
        template.setName(request.name());
        template.setContent(request.content());
        template.setIsSystem(0); // 用户自定义模板
        template.setSortOrder(nextSortOrder);

        // 4. 保存到数据库
        templateMapper.insert(template);

        log.info("指令模板创建成功: id={}, name={}", template.getId(), template.getName());

        return convertToVO(template);
    }

    @Override
    @Transactional
    public void updateTemplate(Long userId, Long templateId, UpdatePromptTemplateRequest request) {
        log.info("更新指令模板: userId={}, templateId={}", userId, templateId);

        // 1. 查询模板是否存在
        PromptTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            log.warn("模板不存在: templateId={}", templateId);
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        // 2. 检查是否为系统预设模板(系统模板不允许修改)
        if (template.getIsSystem() == 1) {
            log.warn("系统预设模板不允许修改: templateId={}", templateId);
            throw new BusinessException(ResultCode.ACCESS_DENIED, "系统预设模板不允许修改");
        }

        // 3. 验证是否属于当前用户
        if (!template.getUserId().equals(userId)) {
            log.warn("无权限访问该模板: userId={}, templateId={}", userId, templateId);
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 4. 检查新名称是否与其他模板重复(如果修改了名称或类别)
        String newCategory = request.category() != null ? request.category() : template.getCategory();
        String newName = request.name() != null ? request.name() : template.getName();

        if (!template.getName().equals(newName) || !template.getCategory().equals(newCategory)) {
            Long count = templateMapper.selectCount(
                    new LambdaQueryWrapper<PromptTemplate>()
                            .eq(PromptTemplate::getUserId, userId)
                            .eq(PromptTemplate::getCategory, newCategory)
                            .eq(PromptTemplate::getName, newName)
                            .ne(PromptTemplate::getId, templateId)
            );

            if (count > 0) {
                log.warn("模板名称在该类别下已存在: userId={}, category={}, name={}",
                        userId, newCategory, newName);
                throw new BusinessException(ResultCode.TEMPLATE_NAME_DUPLICATE);
            }
        }

        // 5. 更新字段(仅更新提供的字段)
        if (request.category() != null) {
            template.setCategory(request.category());
        }
        if (request.name() != null) {
            template.setName(request.name());
        }
        if (request.content() != null) {
            template.setContent(request.content());
        }

        templateMapper.updateById(template);

        log.info("指令模板更新成功: templateId={}", templateId);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long userId, Long templateId) {
        log.info("删除指令模板: userId={}, templateId={}", userId, templateId);

        // 1. 查询模板是否存在
        PromptTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            log.warn("模板不存在: templateId={}", templateId);
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        // 2. 检查是否为系统预设模板(系统模板不允许删除)
        if (template.getIsSystem() == 1) {
            log.warn("系统预设模板不允许删除: templateId={}", templateId);
            throw new BusinessException(ResultCode.ACCESS_DENIED, "系统预设模板不允许删除");
        }

        // 3. 验证是否属于当前用户
        if (!template.getUserId().equals(userId)) {
            log.warn("无权限访问该模板: userId={}, templateId={}", userId, templateId);
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 4. 物理删除(指令模板不需要软删除)
        templateMapper.deleteById(templateId);

        log.info("指令模板删除成功: templateId={}", templateId);
    }

    /**
     * 将实体转换为VO
     *
     * @param template 模板实体
     * @return 模板VO
     */
    private PromptTemplateVO convertToVO(PromptTemplate template) {
        return new PromptTemplateVO(
                template.getId(),
                template.getCategory(),
                template.getName(),
                template.getContent(),
                template.getIsSystem(),
                template.getSortOrder(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}
