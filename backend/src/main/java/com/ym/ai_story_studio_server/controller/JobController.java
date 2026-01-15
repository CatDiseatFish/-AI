package com.ym.ai_story_studio_server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.job.JobDetailVO;
import com.ym.ai_story_studio_server.dto.job.JobQueryRequest;
import com.ym.ai_story_studio_server.dto.job.JobVO;
import com.ym.ai_story_studio_server.service.JobService;
import com.ym.ai_story_studio_server.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 任务控制器
 *
 * <p>提供任务队列管理的HTTP接口,包括任务列表查询、任务详情查询和任务取消
 *
 * <p>所有接口均需要JWT认证,通过UserContext获取当前用户ID
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /**
     * 分页查询任务列表
     *
     * <p>支持按状态、类型、项目ID筛选,返回当前用户的所有任务
     *
     * <p>请求参数:
     * <ul>
     *     <li>page: 页码(必须,大于0)</li>
     *     <li>size: 每页大小(必须,1-100)</li>
     *     <li>status: 任务状态筛选(可选,PENDING/RUNNING/SUCCEEDED/FAILED/CANCELED)</li>
     *     <li>jobType: 任务类型筛选(可选)</li>
     *     <li>projectId: 项目ID筛选(可选)</li>
     * </ul>
     *
     * <p>响应数据:
     * <pre>
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "records": [ ... ],
     *     "total": 15,
     *     "size": 10,
     *     "current": 1,
     *     "pages": 2
     *   }
     * }
     * </pre>
     *
     * @param request 查询请求(分页+筛选条件)
     * @return 分页任务列表
     */
    @GetMapping
    public Result<Page<JobVO>> getJobPage(@Validated JobQueryRequest request) {
        Long userId = UserContext.getUserId();
        log.info("分页查询任务列表, userId: {}, request: {}", userId, request);

        Page<JobVO> page = jobService.getJobPage(userId, request);
        return Result.success(page);
    }

    /**
     * 获取任务详情
     *
     * <p>返回任务的完整信息,包括子任务列表
     *
     * <p>响应数据:
     * <pre>
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": {
     *     "id": 1,
     *     "projectId": 1,
     *     "projectName": "测试项目",
     *     "jobType": "GEN_CHAR_IMG",
     *     "status": "RUNNING",
     *     "progress": 50,
     *     "totalItems": 10,
     *     "doneItems": 5,
     *     "elapsedSeconds": 120,
     *     "items": [ ... ],
     *     ...
     *   }
     * }
     * </pre>
     *
     * @param jobId 任务ID
     * @return 任务详情VO
     */
    @GetMapping("/{id}")
    public Result<JobDetailVO> getJobDetail(@PathVariable("id") Long jobId) {
        Long userId = UserContext.getUserId();
        log.info("获取任务详情, userId: {}, jobId: {}", userId, jobId);

        JobDetailVO jobDetail = jobService.getJobDetail(userId, jobId);
        return Result.success(jobDetail);
    }

    /**
     * 取消任务
     *
     * <p>取消正在运行或等待中的任务
     *
     * <p>只能取消PENDING或RUNNING状态的任务,已完成/已失败/已取消的任务不能再次取消
     *
     * <p>响应数据:
     * <pre>
     * {
     *   "code": 200,
     *   "message": "任务取消成功"
     * }
     * </pre>
     *
     * @param jobId 任务ID
     * @return 成功响应
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelJob(@PathVariable("id") Long jobId) {
        Long userId = UserContext.getUserId();
        log.info("取消任务, userId: {}, jobId: {}", userId, jobId);

        jobService.cancelJob(userId, jobId);
        return Result.success();
    }
}
