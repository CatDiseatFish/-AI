package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.export.ExportRequest;
import com.ym.ai_story_studio_server.entity.*;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.mapper.*;
import com.ym.ai_story_studio_server.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 导出服务实现类
 *
 * <p>实现项目数据导出功能,按分类文件夹组织资产
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final ProjectMapper projectMapper;
    private final ProjectCharacterMapper projectCharacterMapper;
    private final ProjectSceneMapper projectSceneMapper;
    private final StoryboardShotMapper shotMapper;
    private final AssetVersionMapper assetVersionMapper;
    private final AssetRefMapper assetRefMapper;
    private final CharacterLibraryMapper characterLibraryMapper;
    private final SceneLibraryMapper sceneLibraryMapper;
    private final JobMapper jobMapper;

    /**
     * 临时文件存储路径
     */
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/ai-story-exports/";

    /**
     * 导出任务状态缓存(jobId -> status)
     */
    private final ConcurrentHashMap<Long, String> exportStatusMap = new ConcurrentHashMap<>();

    /**
     * 导出文件路径缓存(jobId -> filePath)
     */
    private final ConcurrentHashMap<Long, String> exportFileMap = new ConcurrentHashMap<>();

    @Override
    public Long submitExportTask(Long userId, Long projectId, ExportRequest request) {
        log.info("提交导出任务: userId={}, projectId={}, request={}", userId, projectId, request);

        // 1. 验证项目是否存在且属于当前用户
        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getDeletedAt() != null) {
            log.warn("项目不存在: projectId={}", projectId);
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        if (!project.getUserId().equals(userId)) {
            log.warn("无权限访问该项目: userId={}, projectId={}", userId, projectId);
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 2. 创建导出任务
        Job job = new Job();
        job.setUserId(userId);
        job.setProjectId(projectId);
        job.setJobType("EXPORT_ZIP");
        job.setStatus("PENDING");
        job.setProgress(0);
        job.setStartedAt(LocalDateTime.now());

        jobMapper.insert(job);

        log.info("导出任务创建成功: jobId={}", job.getId());

        // 3. 异步执行导出
        exportStatusMap.put(job.getId(), "RUNNING");
        executeExportAsync(job.getId(), projectId, request);

        return job.getId();
    }

    @Override
    public Resource getExportFile(Long userId, Long jobId) {
        log.info("下载导出文件: userId={}, jobId={}", userId, jobId);

        // 1. 验证任务是否存在且属于当前用户
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            log.warn("导出任务不存在: jobId={}", jobId);
            throw new BusinessException(ResultCode.JOB_NOT_FOUND);
        }

        if (!job.getUserId().equals(userId)) {
            log.warn("无权限访问该任务: userId={}, jobId={}", userId, jobId);
            throw new BusinessException(ResultCode.ACCESS_DENIED);
        }

        // 2. 检查任务状态
        if (!"SUCCEEDED".equals(job.getStatus())) {
            log.warn("导出任务未完成: jobId={}, status={}", jobId, job.getStatus());
            throw new BusinessException(ResultCode.PARAM_INVALID, "导出任务未完成,请稍后再试");
        }

        // 3. 获取文件路径
        String filePath = exportFileMap.get(jobId);
        if (filePath == null) {
            log.warn("导出文件路径不存在: jobId={}", jobId);
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "导出文件不存在");
        }

        File file = new File(filePath);
        if (!file.exists()) {
            log.warn("导出文件不存在: filePath={}", filePath);
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "导出文件不存在");
        }

        return new FileSystemResource(file);
    }

    @Override
    public String getExportFileName(Long jobId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            return "export.zip";
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "project_" + job.getProjectId() + "_export_" + timestamp + ".zip";
    }

    /**
     * 异步执行导出任务
     *
     * @param jobId 任务ID
     * @param projectId 项目ID
     * @param request 导出请求
     */
    @Async
    public void executeExportAsync(Long jobId, Long projectId, ExportRequest request) {
        log.info("开始执行导出任务: jobId={}, projectId={}", jobId, projectId);

        try {
            // 1. 更新任务状态为运行中
            Job job = jobMapper.selectById(jobId);
            job.setStatus("RUNNING");
            jobMapper.updateById(job);

            // 2. 创建临时目录
            Path tempDir = Paths.get(TEMP_DIR);
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            // 3. 创建ZIP文件
            String zipFileName = "project_" + projectId + "_export_" + jobId + ".zip";
            String zipFilePath = TEMP_DIR + zipFileName;
            FileOutputStream fos = new FileOutputStream(zipFilePath);
            ZipOutputStream zos = new ZipOutputStream(fos);

            // 4. 导出角色画像
            if (request.exportCharacters()) {
                log.debug("导出角色画像: jobId={}", jobId);
                exportCharacters(projectId, request.mode(), zos);
            }

            // 5. 导出场景画像
            if (request.exportScenes()) {
                log.debug("导出场景画像: jobId={}", jobId);
                exportScenes(projectId, request.mode(), zos);
            }

            // 6. 导出分镜图
            if (request.exportShotImages()) {
                log.debug("导出分镜图: jobId={}", jobId);
                exportShotImages(projectId, request.mode(), zos);
            }

            // 7. 导出视频
            if (request.exportVideos()) {
                log.debug("导出视频: jobId={}", jobId);
                exportVideos(projectId, request.mode(), zos);
            }

            // 8. 关闭ZIP流
            zos.close();
            fos.close();

            // 9. 更新任务状态为成功
            job = jobMapper.selectById(jobId);
            job.setStatus("SUCCEEDED");
            job.setProgress(100);
            job.setFinishedAt(LocalDateTime.now());
            jobMapper.updateById(job);

            exportStatusMap.put(jobId, "SUCCEEDED");
            exportFileMap.put(jobId, zipFilePath);

            log.info("导出任务执行成功: jobId={}, zipFile={}", jobId, zipFilePath);

        } catch (Exception e) {
            log.error("导出任务执行失败: jobId=" + jobId, e);

            // 更新任务状态为失败
            Job job = jobMapper.selectById(jobId);
            job.setStatus("FAILED");
            job.setFinishedAt(LocalDateTime.now());
            job.setErrorMessage(e.getMessage());
            jobMapper.updateById(job);

            exportStatusMap.put(jobId, "FAILED");
        }
    }

    /**
     * 导出角色画像
     *
     * <p>通过 ProjectCharacter -> CharacterLibrary -> AssetRef 查询当前版本
     *
     * @param projectId 项目ID
     * @param mode 导出模式(CURRENT/ALL)
     * @param zos ZIP输出流
     */
    private void exportCharacters(Long projectId, String mode, ZipOutputStream zos) throws Exception {
        List<ProjectCharacter> characters = projectCharacterMapper.selectList(
                new LambdaQueryWrapper<ProjectCharacter>()
                        .eq(ProjectCharacter::getProjectId, projectId)
        );

        int index = 1;
        for (ProjectCharacter character : characters) {
            if (character.getLibraryCharacterId() == null) {
                continue;
            }

            // 查询全局角色库以获取名称
            CharacterLibrary libChar = characterLibraryMapper.selectById(character.getLibraryCharacterId());
            if (libChar == null) {
                continue;
            }

            // 使用displayName或全局name
            String characterName = character.getDisplayName() != null ?
                    character.getDisplayName() : libChar.getName();
            String folderName = String.format("01-角色/%02d-%s/", index, characterName);

            if ("CURRENT".equals(mode)) {
                // 仅导出当前版本:查询AssetRef获取当前选用的版本
                AssetRef assetRef = assetRefMapper.selectOne(
                        new LambdaQueryWrapper<AssetRef>()
                                .eq(AssetRef::getProjectId, projectId)
                                .eq(AssetRef::getRefType, "LIB_CHAR_CURRENT")
                                .eq(AssetRef::getRefOwnerId, character.getLibraryCharacterId())
                );

                if (assetRef != null && assetRef.getAssetVersionId() != null) {
                    AssetVersion version = assetVersionMapper.selectById(assetRef.getAssetVersionId());
                    if (version != null && version.getUrl() != null) {
                        String fileName = folderName + "当前版本" + getFileExtension(version.getUrl());
                        downloadAndAddToZip(version.getUrl(), fileName, zos);
                    }
                }
            } else {
                // 导出所有历史版本:通过Asset查询所有版本
                // 注意:需要先通过AssetRef找到assetId
                AssetRef assetRef = assetRefMapper.selectOne(
                        new LambdaQueryWrapper<AssetRef>()
                                .eq(AssetRef::getProjectId, projectId)
                                .eq(AssetRef::getRefType, "LIB_CHAR_CURRENT")
                                .eq(AssetRef::getRefOwnerId, character.getLibraryCharacterId())
                );

                if (assetRef != null && assetRef.getAssetVersionId() != null) {
                    AssetVersion currentVersion = assetVersionMapper.selectById(assetRef.getAssetVersionId());
                    if (currentVersion != null) {
                        Long assetId = currentVersion.getAssetId();

                        List<AssetVersion> versions = assetVersionMapper.selectList(
                                new LambdaQueryWrapper<AssetVersion>()
                                        .eq(AssetVersion::getAssetId, assetId)
                                        .orderByDesc(AssetVersion::getVersionNo)
                        );

                        int versionIndex = 1;
                        for (AssetVersion version : versions) {
                            if (version.getUrl() != null) {
                                String fileName = folderName + String.format("版本%02d", versionIndex) + getFileExtension(version.getUrl());
                                downloadAndAddToZip(version.getUrl(), fileName, zos);
                                versionIndex++;
                            }
                        }
                    }
                }
            }

            index++;
        }
    }

    /**
     * 导出场景画像
     *
     * <p>通过 ProjectScene -> SceneLibrary -> AssetRef 查询当前版本
     *
     * @param projectId 项目ID
     * @param mode 导出模式(CURRENT/ALL)
     * @param zos ZIP输出流
     */
    private void exportScenes(Long projectId, String mode, ZipOutputStream zos) throws Exception {
        List<ProjectScene> scenes = projectSceneMapper.selectList(
                new LambdaQueryWrapper<ProjectScene>()
                        .eq(ProjectScene::getProjectId, projectId)
        );

        int index = 1;
        for (ProjectScene scene : scenes) {
            if (scene.getLibrarySceneId() == null) {
                continue;
            }

            // 查询全局场景库以获取名称
            SceneLibrary libScene = sceneLibraryMapper.selectById(scene.getLibrarySceneId());
            if (libScene == null) {
                continue;
            }

            // 使用displayName或全局name
            String sceneName = scene.getDisplayName() != null ?
                    scene.getDisplayName() : libScene.getName();
            String folderName = String.format("02-场景/%02d-%s/", index, sceneName);

            if ("CURRENT".equals(mode)) {
                // 仅导出当前版本
                AssetRef assetRef = assetRefMapper.selectOne(
                        new LambdaQueryWrapper<AssetRef>()
                                .eq(AssetRef::getProjectId, projectId)
                                .eq(AssetRef::getRefType, "SCENE_CURRENT")
                                .eq(AssetRef::getRefOwnerId, scene.getLibrarySceneId())
                );

                if (assetRef != null && assetRef.getAssetVersionId() != null) {
                    AssetVersion version = assetVersionMapper.selectById(assetRef.getAssetVersionId());
                    if (version != null && version.getUrl() != null) {
                        String fileName = folderName + "当前版本" + getFileExtension(version.getUrl());
                        downloadAndAddToZip(version.getUrl(), fileName, zos);
                    }
                }
            } else {
                // 导出所有历史版本
                AssetRef assetRef = assetRefMapper.selectOne(
                        new LambdaQueryWrapper<AssetRef>()
                                .eq(AssetRef::getProjectId, projectId)
                                .eq(AssetRef::getRefType, "SCENE_CURRENT")
                                .eq(AssetRef::getRefOwnerId, scene.getLibrarySceneId())
                );

                if (assetRef != null && assetRef.getAssetVersionId() != null) {
                    AssetVersion currentVersion = assetVersionMapper.selectById(assetRef.getAssetVersionId());
                    if (currentVersion != null) {
                        Long assetId = currentVersion.getAssetId();

                        List<AssetVersion> versions = assetVersionMapper.selectList(
                                new LambdaQueryWrapper<AssetVersion>()
                                        .eq(AssetVersion::getAssetId, assetId)
                                        .orderByDesc(AssetVersion::getVersionNo)
                        );

                        int versionIndex = 1;
                        for (AssetVersion version : versions) {
                            if (version.getUrl() != null) {
                                String fileName = folderName + String.format("版本%02d", versionIndex) + getFileExtension(version.getUrl());
                                downloadAndAddToZip(version.getUrl(), fileName, zos);
                                versionIndex++;
                            }
                        }
                    }
                }
            }

            index++;
        }
    }

    /**
     * 导出分镜图
     *
     * <p>通过 StoryboardShot -> AssetRef(SHOT_IMG_CURRENT) 查询当前版本
     *
     * @param projectId 项目ID
     * @param mode 导出模式(CURRENT/ALL)
     * @param zos ZIP输出流
     */
    private void exportShotImages(Long projectId, String mode, ZipOutputStream zos) throws Exception {
        List<StoryboardShot> shots = shotMapper.selectList(
                new LambdaQueryWrapper<StoryboardShot>()
                        .eq(StoryboardShot::getProjectId, projectId)
                        .isNull(StoryboardShot::getDeletedAt)
                        .orderByAsc(StoryboardShot::getShotNo)
        );

        for (StoryboardShot shot : shots) {
            String folderName = String.format("03-分镜/%03d/", shot.getShotNo());

            if ("CURRENT".equals(mode)) {
                // 仅导出当前版本
                AssetRef assetRef = assetRefMapper.selectOne(
                        new LambdaQueryWrapper<AssetRef>()
                                .eq(AssetRef::getProjectId, projectId)
                                .eq(AssetRef::getRefType, "SHOT_IMG_CURRENT")
                                .eq(AssetRef::getRefOwnerId, shot.getId())
                );

                if (assetRef != null && assetRef.getAssetVersionId() != null) {
                    AssetVersion version = assetVersionMapper.selectById(assetRef.getAssetVersionId());
                    if (version != null && version.getUrl() != null) {
                        String fileName = folderName + "当前版本" + getFileExtension(version.getUrl());
                        downloadAndAddToZip(version.getUrl(), fileName, zos);
                    }
                }
            } else {
                // 导出所有历史版本
                AssetRef assetRef = assetRefMapper.selectOne(
                        new LambdaQueryWrapper<AssetRef>()
                                .eq(AssetRef::getProjectId, projectId)
                                .eq(AssetRef::getRefType, "SHOT_IMG_CURRENT")
                                .eq(AssetRef::getRefOwnerId, shot.getId())
                );

                if (assetRef != null && assetRef.getAssetVersionId() != null) {
                    AssetVersion currentVersion = assetVersionMapper.selectById(assetRef.getAssetVersionId());
                    if (currentVersion != null) {
                        Long assetId = currentVersion.getAssetId();

                        List<AssetVersion> versions = assetVersionMapper.selectList(
                                new LambdaQueryWrapper<AssetVersion>()
                                        .eq(AssetVersion::getAssetId, assetId)
                                        .orderByDesc(AssetVersion::getVersionNo)
                        );

                        int versionIndex = 1;
                        for (AssetVersion version : versions) {
                            if (version.getUrl() != null) {
                                String fileName = folderName + String.format("版本%02d", versionIndex) + getFileExtension(version.getUrl());
                                downloadAndAddToZip(version.getUrl(), fileName, zos);
                                versionIndex++;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 导出视频
     *
     * <p>通过 StoryboardShot -> AssetRef(SHOT_VIDEO_CURRENT) 查询当前版本
     *
     * @param projectId 项目ID
     * @param mode 导出模式(CURRENT/ALL)
     * @param zos ZIP输出流
     */
    private void exportVideos(Long projectId, String mode, ZipOutputStream zos) throws Exception {
        List<StoryboardShot> shots = shotMapper.selectList(
                new LambdaQueryWrapper<StoryboardShot>()
                        .eq(StoryboardShot::getProjectId, projectId)
                        .isNull(StoryboardShot::getDeletedAt)
                        .orderByAsc(StoryboardShot::getShotNo)
        );

        for (StoryboardShot shot : shots) {
            String folderName = String.format("04-视频/%03d/", shot.getShotNo());

            if ("CURRENT".equals(mode)) {
                // 仅导出当前版本
                AssetRef assetRef = assetRefMapper.selectOne(
                        new LambdaQueryWrapper<AssetRef>()
                                .eq(AssetRef::getProjectId, projectId)
                                .eq(AssetRef::getRefType, "SHOT_VIDEO_CURRENT")
                                .eq(AssetRef::getRefOwnerId, shot.getId())
                );

                if (assetRef != null && assetRef.getAssetVersionId() != null) {
                    AssetVersion version = assetVersionMapper.selectById(assetRef.getAssetVersionId());
                    if (version != null && version.getUrl() != null) {
                        String fileName = folderName + "当前版本" + getFileExtension(version.getUrl());
                        downloadAndAddToZip(version.getUrl(), fileName, zos);
                    }
                }
            } else {
                // 导出所有历史版本
                AssetRef assetRef = assetRefMapper.selectOne(
                        new LambdaQueryWrapper<AssetRef>()
                                .eq(AssetRef::getProjectId, projectId)
                                .eq(AssetRef::getRefType, "SHOT_VIDEO_CURRENT")
                                .eq(AssetRef::getRefOwnerId, shot.getId())
                );

                if (assetRef != null && assetRef.getAssetVersionId() != null) {
                    AssetVersion currentVersion = assetVersionMapper.selectById(assetRef.getAssetVersionId());
                    if (currentVersion != null) {
                        Long assetId = currentVersion.getAssetId();

                        List<AssetVersion> versions = assetVersionMapper.selectList(
                                new LambdaQueryWrapper<AssetVersion>()
                                        .eq(AssetVersion::getAssetId, assetId)
                                        .orderByDesc(AssetVersion::getVersionNo)
                        );

                        int versionIndex = 1;
                        for (AssetVersion version : versions) {
                            if (version.getUrl() != null) {
                                String fileName = folderName + String.format("版本%02d", versionIndex) + getFileExtension(version.getUrl());
                                downloadAndAddToZip(version.getUrl(), fileName, zos);
                                versionIndex++;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 从URL下载文件并添加到ZIP
     *
     * @param fileUrl 文件URL
     * @param zipEntryName ZIP内的文件名
     * @param zos ZIP输出流
     */
    private void downloadAndAddToZip(String fileUrl, String zipEntryName, ZipOutputStream zos) throws Exception {
        log.debug("下载文件到ZIP: url={}, entry={}", fileUrl, zipEntryName);

        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);

        try (InputStream inputStream = connection.getInputStream()) {
            ZipEntry zipEntry = new ZipEntry(zipEntryName);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }

            zos.closeEntry();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param url 文件URL
     * @return 扩展名(含点号,如".png")
     */
    private String getFileExtension(String url) {
        int lastDotIndex = url.lastIndexOf('.');
        int lastSlashIndex = url.lastIndexOf('/');

        if (lastDotIndex > lastSlashIndex && lastDotIndex != -1) {
            return url.substring(lastDotIndex);
        }

        return "";
    }
}
