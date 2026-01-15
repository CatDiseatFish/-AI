package com.ym.ai_story_studio_server.dto.project;

/**
 * 项目查询请求
 *
 * @param folderId 文件夹ID（可选，为null表示查询所有）
 * @param keyword 搜索关键词（可选，搜索项目名称）
 * @param aspectRatio 画幅比例筛选（可选）
 * @param styleCode 风格筛选（可选）
 * @param status 状态筛选（可选）
 * @param page 页码（从1开始）
 * @param size 每页大小
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public record ProjectQueryRequest(
        /**
         * 文件夹ID（可选，为null表示查询所有）
         */
        Long folderId,

        /**
         * 搜索关键词（可选，搜索项目名称）
         */
        String keyword,

        /**
         * 画幅比例筛选（可选）
         */
        String aspectRatio,

        /**
         * 风格筛选（可选）
         */
        String styleCode,

        /**
         * 状态筛选（可选）
         */
        String status,

        /**
         * 页码（从1开始）
         */
        Integer page,

        /**
         * 每页大小
         */
        Integer size
) {
    /**
     * 获取偏移量
     */
    public long getOffset() {
        return (long) (page - 1) * size;
    }
}
