package com.ym.ai_story_studio_server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ym.ai_story_studio_server.dto.job.JobDetailVO;
import com.ym.ai_story_studio_server.dto.job.JobQueryRequest;
import com.ym.ai_story_studio_server.dto.job.JobVO;
import com.ym.ai_story_studio_server.exception.BusinessException;

/**
 * 任务服务接口
 *
 * <p>提供任务队列管理功能,包括任务列表查询、任务详情查询和任务取消
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface JobService {

    /**
     * 分页查询任务列表(支持搜索和筛选)
     *
     * <p>查询当前用户的所有任务,支持按状态、类型、项目ID筛选
     *
     * @param userId 当前用户ID(用于数据隔离)
     * @param request 查询请求(分页+筛选条件)
     * @return 分页任务列表
     */
    Page<JobVO> getJobPage(Long userId, JobQueryRequest request);

    /**
     * 获取任务详情(包含子任务列表)
     *
     * <p>返回任务的完整信息,包括子任务列表
     *
     * @param userId 当前用户ID(用于权限验证)
     * @param jobId 任务ID
     * @return 任务详情VO
     * @throws BusinessException 如果任务不存在或无权限访问
     */
    JobDetailVO getJobDetail(Long userId, Long jobId);

    /**
     * 取消任务
     *
     * <p>取消正在运行或等待中的任务,已完成/已失败/已取消的任务不能再次取消
     *
     * @param userId 当前用户ID(用于权限验证)
     * @param jobId 任务ID
     * @throws BusinessException 如果任务不存在、无权限访问或任务已完成
     */
    void cancelJob(Long userId, Long jobId);
}
