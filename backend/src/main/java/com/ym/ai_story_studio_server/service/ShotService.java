package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.shot.*;

import java.util.List;

/**
 * 分镜服务接口
 *
 * <p>提供分镜的CRUD、排序、绑定管理等功能
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface ShotService {

    /**
     * 获取项目的分镜列表(按shotNo升序)
     *
     * <p>返回指定项目下所有未删除的分镜,包含绑定的角色/场景信息和资产状态
     *
     * @param userId 当前用户ID,用于验证项目归属权限
     * @param projectId 项目ID
     * @return 分镜VO列表,按分镜序号升序排序
     */
    List<ShotVO> getShotList(Long userId, Long projectId);

    /**
     * 新增分镜
     *
     * <p>在指定项目下创建新分镜,自动分配分镜序号(最大shotNo+1)
     *
     * @param userId 当前用户ID,用于验证项目归属权限
     * @param projectId 项目ID
     * @param request 创建分镜请求,包含剧本文本
     * @return 新创建的分镜VO
     */
    ShotVO createShot(Long userId, Long projectId, CreateShotRequest request);

    /**
     * 更新分镜
     *
     * <p>更新指定分镜的剧本文本
     *
     * @param userId 当前用户ID,用于验证项目归属权限
     * @param projectId 项目ID
     * @param shotId 分镜ID
     * @param request 更新分镜请求,包含新的剧本文本
     */
    void updateShot(Long userId, Long projectId, Long shotId, UpdateShotRequest request);

    /**
     * 删除分镜(软删除)
     *
     * <p>软删除指定分镜(设置deleted_at字段),同时删除相关的绑定关系
     *
     * @param userId 当前用户ID,用于验证项目归属权限
     * @param projectId 项目ID
     * @param shotId 分镜ID
     */
    void deleteShot(Long userId, Long projectId, Long shotId);

    /**
     * 调整分镜顺序
     *
     * <p>批量调整分镜的shotNo,按照传入的shotIds顺序重新分配序号(从1开始)
     *
     * @param userId 当前用户ID,用于验证项目归属权限
     * @param projectId 项目ID
     * @param request 调整顺序请求,包含按新顺序排列的分镜ID列表
     */
    void reorderShots(Long userId, Long projectId, ReorderShotsRequest request);

    /**
     * 创建绑定关系(绑定角色或场景)
     *
     * <p>在分镜与角色/场景之间创建绑定关系,防止重复绑定
     *
     * @param userId 当前用户ID,用于验证项目归属权限
     * @param projectId 项目ID
     * @param shotId 分镜ID
     * @param request 创建绑定请求,包含绑定类型(PCHAR/PSCENE)和绑定对象ID
     */
    void createBinding(Long userId, Long projectId, Long shotId, CreateBindingRequest request);

    /**
     * 删除绑定关系
     *
     * <p>删除指定的分镜绑定关系(物理删除)
     *
     * @param userId 当前用户ID,用于验证项目归属权限
     * @param projectId 项目ID
     * @param shotId 分镜ID
     * @param bindingId 绑定记录ID
     */
    void deleteBinding(Long userId, Long projectId, Long shotId, Long bindingId);

    /**
     * AI解析剧本并批量创建分镜
     *
     * <p>将完整剧本文本通过AI拆分成多条分镜,并批量创建
     *
     * @param userId 当前用户ID,用于验证项目归属权限
     * @param projectId 项目ID
     * @param request 包含完整剧本的请求
     * @return 创建的分镜VO列表
     */
    List<ShotVO> parseScriptAndCreateShots(Long userId, Long projectId, ParseScriptRequest request);

    /**
     * 删除分镜图资产
     *
     * <p>删除指定分镜的分镜图资产，删除后状态变为“待生成”
     *
     * @param userId 当前用户ID,用于验证项目归属权限
     * @param projectId 项目ID
     * @param shotId 分镜ID
     */
    void deleteShotImageAsset(Long userId, Long projectId, Long shotId);

    /**
     * 删除视频资产
     *
     * <p>删除指定分镜的视频资产，删除后状态变为“待生成”
     *
     * @param userId 当前用户ID,用于验证项目归属权限
     * @param projectId 项目ID
     * @param shotId 分镜ID
     */
    void deleteVideoAsset(Long userId, Long projectId, Long shotId);
}
