package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.asset.AssetVersionVO;
import com.ym.ai_story_studio_server.dto.asset.SetCurrentVersionRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 资产服务接口
 *
 * <p>提供资产版本管理功能,包括:
 * <ul>
 *   <li>查询资产的版本历史列表</li>
 *   <li>上传本地图片创建新版本</li>
 *   <li>设置当前使用的版本(支持版本回退)</li>
 * </ul>
 *
 * <p>权限要求:所有方法都需要验证资产归属权限,确保用户只能操作自己项目的资产
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface AssetService {

    /**
     * 获取资产版本历史列表
     *
     * <p>返回指定资产的所有版本记录,按版本号降序排序(最新版本在前),并标记当前正在使用的版本。
     *
     * @param userId 当前用户ID(用于权限验证)
     * @param assetId 资产ID
     * @return 资产版本VO列表,按版本号降序排序
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 如果资产不存在或无权限访问
     */
    List<AssetVersionVO> getVersionHistory(Long userId, Long assetId);

    /**
     * 上传本地图片(创建新版本)
     *
     * <p>用户上传本地图片文件,系统自动生成新版本号(最大版本号+1),
     * 上传文件到对象存储(OSS/MinIO),创建新的AssetVersion记录。
     *
     * <p>版本来源标记为"UPLOAD",状态为"READY"。
     *
     * @param userId 当前用户ID(用于权限验证)
     * @param assetId 资产ID
     * @param file 上传的图片文件(支持jpg/jpeg/png/webp等格式)
     * @return 新创建的资产版本VO
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 如果资产不存在、无权限访问或文件上传失败
     */
    AssetVersionVO uploadLocalImage(Long userId, Long assetId, MultipartFile file);

    /**
     * 从URL上传图片(创建新版本)
     *
     * <p>从URL下载图片,然后上传到对象存储并创建新版本。
     * 解决前端直接访问OSS的跨域问题。
     *
     * <p>版本来源标记为"UPLOAD",状态为"READY"。
     *
     * @param userId 当前用户ID(用于权限验证)
     * @param assetId 资产ID
     * @param imageUrl 图片URL
     * @return 新创建的资产版本VO
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 如果资产不存在、无权限访问或下载/上传失败
     */
    AssetVersionVO uploadFromUrl(Long userId, Long assetId, String imageUrl);

    /**
     * 设置当前版本
     *
     * <p>设置资产的当前使用版本,支持版本回退(切换到历史版本)。
     * 更新asset_refs表中的current_version_id字段。
     *
     * @param userId 当前用户ID(用于权限验证)
     * @param assetId 资产ID
     * @param request 设置当前版本请求(包含目标版本ID)
     * @throws com.ym.ai_story_studio_server.exception.BusinessException 如果资产不存在、版本不存在或无权限访问
     */
    void setCurrentVersion(Long userId, Long assetId, SetCurrentVersionRequest request);
}
