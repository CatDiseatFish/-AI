package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.export.ExportRequest;
import org.springframework.core.io.Resource;

/**
 * 导出服务接口
 *
 * <p>提供项目数据导出功能,支持:
 * <ul>
 *   <li>导出角色画像</li>
 *   <li>导出场景画像</li>
 *   <li>导出分镜图</li>
 *   <li>导出视频</li>
 *   <li>按分类文件夹组织(01-角色, 02-场景, 03-分镜, 04-视频)</li>
 *   <li>支持仅导出当前版本或包含所有历史版本</li>
 * </ul>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface ExportService {

    /**
     * 提交项目导出任务
     *
     * <p>异步执行导出任务,创建ZIP文件,包含选中的资产分类
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param request 导出请求
     * @return 导出任务ID
     */
    Long submitExportTask(Long userId, Long projectId, ExportRequest request);

    /**
     * 获取导出文件下载资源
     *
     * <p>根据任务ID获取导出的ZIP文件
     *
     * @param userId 用户ID
     * @param jobId 导出任务ID
     * @return 文件资源
     */
    Resource getExportFile(Long userId, Long jobId);

    /**
     * 获取导出文件名
     *
     * @param jobId 导出任务ID
     * @return 文件名
     */
    String getExportFileName(Long jobId);
}
