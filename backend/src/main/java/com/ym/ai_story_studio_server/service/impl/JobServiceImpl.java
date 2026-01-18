// {{CODE-Cycle-Integration:
//   Task_ID: [#T016]
//   Timestamp: [2025-12-28 18:00:00]
//   Phase: [D-Develop]
//   Context-Analysis: "创建任务队列服务实现类,实现任务列表分页查询、任务详情查询和任务取消功能。权限验证基于job->user链路。"
//   Principle_Applied: "SOLID原则-单一职责, DRY原则, 权限验证模式"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.job.JobDetailVO;
import com.ym.ai_story_studio_server.dto.job.JobItemVO;
import com.ym.ai_story_studio_server.dto.job.JobQueryRequest;
import com.ym.ai_story_studio_server.dto.job.JobVO;
import com.ym.ai_story_studio_server.entity.Job;
import com.ym.ai_story_studio_server.entity.JobItem;
import com.ym.ai_story_studio_server.entity.Project;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.mapper.JobItemMapper;
import com.ym.ai_story_studio_server.mapper.JobMapper;
import com.ym.ai_story_studio_server.mapper.ProjectMapper;
import com.ym.ai_story_studio_server.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务服务实现类
 *
 * <p>提供任务队列管理功能实现,包括任务列表分页查询、任务详情查询和任务取消
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobMapper jobMapper;
    private final JobItemMapper jobItemMapper;
    private final ProjectMapper projectMapper;
    private final ObjectMapper objectMapper;

    /**
     * 分页查询任务列表(支持搜索和筛选)
     *
     * @param userId 当前用户ID(用于数据隔离)
     * @param request 查询请求(分页+筛选条件)
     * @return 分页任务列表
     */
    @Override
    public Page<JobVO> getJobPage(Long userId, JobQueryRequest request) {
        log.info("分页查询任务列表, userId: {}, request: {}", userId, request);

        // 1. 构建分页对象
        Page<Job> page = new Page<>(request.page(), request.size());

        // 2. 构建查询条件
        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Job::getUserId, userId) // 必须条件:用户隔离
                .eq(request.projectId() != null, Job::getProjectId, request.projectId()) // 可选:按项目筛选
                .eq(StringUtils.hasText(request.status()), Job::getStatus, request.status()) // 可选:按状态筛选
                .eq(StringUtils.hasText(request.jobType()), Job::getJobType, request.jobType()) // 可选:按类型筛选
                .orderByDesc(Job::getCreatedAt); // 按创建时间降序排序(最新的任务在前)

        // 3. 执行分页查询
        page = jobMapper.selectPage(page, queryWrapper);

        log.info("查询到{}条任务记录", page.getTotal());

        // 4. 转换为VO
        return (Page<JobVO>) page.convert(job -> {
            // 查询项目名称
            String projectName = null;
            if (job.getProjectId() != null) {
                Project project = projectMapper.selectById(job.getProjectId());
                if (project != null) {
                    projectName = project.getName();
                }
            }

            // 计算任务耗时(仅运行中的任务)
            Long elapsedSeconds = null;
            if ("RUNNING".equals(job.getStatus()) && job.getStartedAt() != null) {
                elapsedSeconds = Duration.between(job.getStartedAt(), LocalDateTime.now()).getSeconds();
            }

            // 解析meta_json提取allImageUrls
            List<String> allImageUrls = extractAllImageUrls(job.getMetaJson());

            return new JobVO(
                    job.getId(),
                    job.getProjectId(),
                    projectName,
                    job.getJobType(),
                    job.getStatus(),
                    job.getProgress(),
                    job.getTotalItems(),
                    job.getDoneItems(),
                    elapsedSeconds,
                    job.getStartedAt(),
                    job.getFinishedAt(),
                    job.getErrorMessage(),
                    job.getResultUrl(),
                    allImageUrls,
                    job.getCostPoints(),
                    job.getMetaJson(),
                    job.getCreatedAt()
            );
        });
    }

    /**
     * 获取任务详情(包含子任务列表)
     *
     * @param userId 当前用户ID(用于权限验证)
     * @param jobId 任务ID
     * @return 任务详情VO
     * @throws BusinessException 如果任务不存在或无权限访问
     */
    @Override
    public JobDetailVO getJobDetail(Long userId, Long jobId) {
        log.info("获取任务详情, userId: {}, jobId: {}", userId, jobId);

        // 1. 查询任务是否存在
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(ResultCode.JOB_NOT_FOUND);
        }

        // 2. 验证权限(任务是否属于当前用户)
        if (!job.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 3. 查询项目名称
        String projectName = null;
        if (job.getProjectId() != null) {
            Project project = projectMapper.selectById(job.getProjectId());
            if (project != null) {
                projectName = project.getName();
            }
        }

        // 4. 查询子任务列表
        LambdaQueryWrapper<JobItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(JobItem::getJobId, jobId)
                .orderByAsc(JobItem::getId); // 按ID升序排序
        List<JobItem> jobItems = jobItemMapper.selectList(itemWrapper);

        log.info("查询到{}条子任务记录", jobItems.size());

        // 5. 转换子任务为VO
        List<JobItemVO> itemVOs = jobItems.stream()
                .map(item -> new JobItemVO(
                        item.getId(),
                        item.getTargetType(),
                        item.getTargetId(),
                        item.getStatus(),
                        item.getOutputAssetVersionId(),
                        item.getErrorMessage(),
                        item.getStartedAt(),
                        item.getFinishedAt(),
                        item.getCreatedAt()
                )).collect(Collectors.toList());

        // 6. 计算任务耗时
        Long elapsedSeconds = null;
        if ("RUNNING".equals(job.getStatus()) && job.getStartedAt() != null) {
            elapsedSeconds = Duration.between(job.getStartedAt(), LocalDateTime.now()).getSeconds();
        } else if (job.getFinishedAt() != null && job.getStartedAt() != null) {
            // 已完成的任务:计算总耗时
            elapsedSeconds = Duration.between(job.getStartedAt(), job.getFinishedAt()).getSeconds();
        }

        // 7. 解析meta_json提取allImageUrls
        List<String> allImageUrls = extractAllImageUrls(job.getMetaJson());

        return new JobDetailVO(
                job.getId(),
                job.getProjectId(),
                projectName,
                job.getJobType(),
                job.getStatus(),
                job.getProgress(),
                job.getTotalItems(),
                job.getDoneItems(),
                elapsedSeconds,
                job.getStartedAt(),
                job.getFinishedAt(),
                job.getErrorMessage(),
                job.getResultUrl(),
                allImageUrls,
                job.getCostPoints(),
                job.getMetaJson(),
                itemVOs,
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }

    /**
     * 取消任务
     *
     * @param userId 当前用户ID(用于权限验证)
     * @param jobId 任务ID
     * @throws BusinessException 如果任务不存在、无权限访问或任务已完成
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelJob(Long userId, Long jobId) {
        log.info("取消任务, userId: {}, jobId: {}", userId, jobId);

        // 1. 查询任务是否存在
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException(ResultCode.JOB_NOT_FOUND);
        }

        // 2. 验证权限(任务是否属于当前用户)
        if (!job.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 3. 检查任务状态(只能取消PENDING或RUNNING状态的任务)
        if ("SUCCEEDED".equals(job.getStatus())) {
            throw new BusinessException(ResultCode.JOB_ALREADY_COMPLETED, "任务已完成,无法取消");
        }
        if ("FAILED".equals(job.getStatus())) {
            throw new BusinessException(ResultCode.JOB_ALREADY_COMPLETED, "任务已失败,无法取消");
        }
        if ("CANCELED".equals(job.getStatus())) {
            throw new BusinessException(ResultCode.JOB_ALREADY_CANCELLED, "任务已取消");
        }

        // 4. 更新任务状态为CANCELED
        job.setStatus("CANCELED");
        job.setFinishedAt(LocalDateTime.now());
        jobMapper.updateById(job);

        log.info("任务取消成功, jobId: {}", jobId);
    }

    /**
     * 从meta_json中提取allImageUrls列表
     *
     * @param metaJson JSON字符串
     * @return 图片URL列表，如果不存在则返回null
     */
    private List<String> extractAllImageUrls(String metaJson) {
        if (metaJson == null || metaJson.isBlank()) {
            return null;
        }

        try {
            Map<String, Object> metaData = objectMapper.readValue(metaJson, new TypeReference<Map<String, Object>>() {});
            Object allImageUrlsObj = metaData.get("allImageUrls");
            
            if (allImageUrlsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> urls = (List<String>) allImageUrlsObj;
                return urls.isEmpty() ? null : urls;
            }
        } catch (Exception e) {
            log.warn("解析meta_json失败: {}", metaJson, e);
        }

        return null;
    }
}
// {{END_MODIFICATIONS}}
