// {{CODE-Cycle-Integration:
//   Task_ID: [#T001]
//   Timestamp: [2025-12-26 15:40:00]
//   Phase: [D-Develop]
//   Context-Analysis: "创建存储服务接口，面向接口编程，支持未来多存储提供商实现（OSS/MinIO）。"
//   Principle_Applied: "SOLID原则-依赖倒置, 开闭原则-对扩展开放"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.service;

import java.io.InputStream;

/**
 * 存储服务接口
 *
 * <p>提供文件上传、下载、删除等基础存储操作的抽象接口
 *
 * <p>设计理念: 面向接口编程，支持多种存储提供商实现
 * <ul>
 *   <li>V1: 阿里云OSS实现 ({@code OssStorageServiceImpl})</li>
 *   <li>V2规划: MinIO实现 ({@code MinioStorageServiceImpl})</li>
 * </ul>
 *
 * <p>使用示例:
 * <pre>
 * &#64;Autowired
 * private StorageService storageService;
 *
 * public String uploadFile(MultipartFile file) {
 *     try (InputStream is = file.getInputStream()) {
 *         return storageService.upload(is, file.getOriginalFilename(), file.getContentType());
 *     }
 * }
 * </pre>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface StorageService {

    /**
     * 上传文件到存储服务
     *
     * <p>文件路径格式: {@code {year}/{month}/{day}/{uuid}_{originalFileName}}
     *
     * @param inputStream 文件输入流（调用者负责关闭）
     * @param fileName    原始文件名（用于生成存储路径和保留文件扩展名）
     * @param contentType 文件MIME类型（例如: "image/jpeg", "video/mp4"）
     * @return 文件的公共访问URL
     * @throws com.ym.ai_story_studio_server.exception.StorageException 上传失败时抛出
     */
    String upload(InputStream inputStream, String fileName, String contentType);

    /**
     * 上传字节数组到存储服务
     *
     * @param imageBytes  图片字节数组
     * @param fileName    文件名
     * @return 文件的公共访问URL
     * @throws com.ym.ai_story_studio_server.exception.StorageException 上传失败时抛出
     */
    String uploadImageBytes(byte[] imageBytes, String fileName);

    /**
     * 根据文件URL下载文件
     *
     * <p>注意: 返回的InputStream由调用者负责关闭
     *
     * @param fileUrl 文件的完整访问URL
     * @return 文件输入流（调用者负责关闭）
     * @throws com.ym.ai_story_studio_server.exception.StorageException 下载失败时抛出
     */
    InputStream download(String fileUrl);

    /**
     * 删除指定URL的文件
     *
     * @param fileUrl 文件的完整访问URL
     * @throws com.ym.ai_story_studio_server.exception.StorageException 删除失败时抛出
     */
    void delete(String fileUrl);

    /**
     * 生成文件的临时访问URL（预签名URL）
     *
     * <p>用于私有存储桶或需要临时授权访问的场景
     *
     * @param fileKey          文件的存储Key（不含域名的路径部分）
     * @param expirationMinutes 有效期（分钟）
     * @return 临时访问URL
     * @throws com.ym.ai_story_studio_server.exception.StorageException 生成失败时抛出
     */
    String generatePresignedUrl(String fileKey, int expirationMinutes);
}
// {{END_MODIFICATIONS}}
