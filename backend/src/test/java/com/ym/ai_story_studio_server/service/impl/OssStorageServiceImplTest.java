// {{CODE-Cycle-Integration:
//   Task_ID: [#T001]
//   Timestamp: [2025-12-26 16:30:00]
//   Phase: [D-Develop - Bug Fix]
//   Context-Analysis: "修复ossClient注入问题，使用反射手动注入mock对象，避免NPE"
//   Principle_Applied: "Verification-Mindset-Loop, 测试最佳实践, 反射注入模式"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.ym.ai_story_studio_server.config.StorageProperties;
import com.ym.ai_story_studio_server.exception.StorageException;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * OssStorageServiceImpl 单元测试
 *
 * <p>使用反射注入Mock对象，解决手动创建ossClient导致的注入失败问题
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OssStorageServiceImpl 单元测试")
class OssStorageServiceImplTest {

    @Mock
    private OSS ossClient;

    @Mock
    private StorageProperties storageProperties;

    @Mock
    private StorageProperties.OssConfig ossConfig;

    private OssStorageServiceImpl storageService;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String ENDPOINT = "oss-cn-hangzhou.aliyuncs.com";
    private static final String ACCESS_KEY_ID = "test-access-key-id";
    private static final String ACCESS_KEY_SECRET = "test-access-key-secret";
    private static final String REGION = "cn-hangzhou";
    private static final String URL_PREFIX = "https://test-bucket.oss-cn-hangzhou.aliyuncs.com";

    @BeforeEach
    void setUp() throws Exception {
        // 创建服务实例（不通过@InjectMocks，手动创建）
        storageService = new OssStorageServiceImpl(storageProperties);

        // 配置属性Mock
        lenient().when(storageProperties.getOss()).thenReturn(ossConfig);
        lenient().when(ossConfig.getEndpoint()).thenReturn(ENDPOINT);
        lenient().when(ossConfig.getBucket()).thenReturn(BUCKET_NAME);
        lenient().when(ossConfig.getAccessKeyId()).thenReturn(ACCESS_KEY_ID);
        lenient().when(ossConfig.getAccessKeySecret()).thenReturn(ACCESS_KEY_SECRET);
        lenient().when(ossConfig.getRegion()).thenReturn(REGION);
        lenient().when(ossConfig.getUrlPrefix()).thenReturn(URL_PREFIX);

        // 【关键】使用反射注入mock的ossClient（绕过init()方法中的手动创建）
        injectMockOssClient();

        // 设置存储桶存在
        lenient().when(ossClient.doesBucketExist(anyString())).thenReturn(true);
    }

    /**
     * 使用反射将mock的ossClient注入到service实例中
     * 这是必须的，因为OssStorageServiceImpl在@PostConstruct中手动创建了ossClient
     */
    private void injectMockOssClient() throws Exception {
        Field ossClientField = OssStorageServiceImpl.class.getDeclaredField("ossClient");
        ossClientField.setAccessible(true);
        ossClientField.set(storageService, ossClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        // 清理：将ossClient设为null
        if (storageService != null) {
            Field ossClientField = OssStorageServiceImpl.class.getDeclaredField("ossClient");
            ossClientField.setAccessible(true);
            ossClientField.set(storageService, null);
        }
    }

    @Nested
    @DisplayName("upload() 方法测试")
    class UploadTests {

        @Test
        @DisplayName("成功上传图片文件")
        void upload_Success_ImageFile() {
            // Arrange
            String fileName = "test-avatar.png";
            String contentType = "image/png";
            String fileContent = "test file content";
            InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

            PutObjectResult mockResult = mock(PutObjectResult.class);
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(mockResult);

            // Act
            String resultUrl = storageService.upload(inputStream, fileName, contentType);

            // Assert
            assertThat(resultUrl).isNotNull();
            assertThat(resultUrl).startsWith(URL_PREFIX);

            // 验证调用参数
            ArgumentCaptor<String> bucketCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
            ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);

            verify(ossClient).putObject(
                    bucketCaptor.capture(),
                    keyCaptor.capture(),
                    streamCaptor.capture(),
                    metadataCaptor.capture()
            );

            assertThat(bucketCaptor.getValue()).isEqualTo(BUCKET_NAME);
            // 文件Key格式: {uuid}_{originalFileName}，原始文件名中的点号保留
            assertThat(keyCaptor.getValue()).contains(fileName);
            assertThat(metadataCaptor.getValue().getContentType()).isEqualTo(contentType);
        }

        @Test
        @DisplayName("成功上传视频文件")
        void upload_Success_VideoFile() {
            // Arrange
            String fileName = "test-video.mp4";
            String contentType = "video/mp4";
            String fileContent = "fake video content";
            InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

            PutObjectResult mockResult = mock(PutObjectResult.class);
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(mockResult);

            // Act
            String resultUrl = storageService.upload(inputStream, fileName, contentType);

            // Assert
            assertThat(resultUrl).isNotNull();
            assertThat(resultUrl).contains(fileName);
        }

        @Test
        @DisplayName("拒绝不支持的文件类型")
        void upload_Fails_UnsupportedFileType() {
            // Arrange
            String fileName = "test.exe";
            String contentType = "application/x-msdownload";
            InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

            // Act & Assert
            assertThatThrownBy(() -> storageService.upload(inputStream, fileName, contentType))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("不支持的文件类型");

            verify(ossClient, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        }

        @Test
        @DisplayName("拒绝空内容类型")
        void upload_Fails_NullContentType() {
            // Arrange
            String fileName = "test.txt";
            String contentType = null;
            InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

            // Act & Assert
            assertThatThrownBy(() -> storageService.upload(inputStream, fileName, contentType))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("文件类型不能为空");
        }

        @Test
        @DisplayName("处理带路径的文件名")
        void upload_Success_FileNameWithPath() {
            // Arrange
            String fileName = "path/to/subdir/test-file.png";
            String contentType = "image/png";
            InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

            PutObjectResult mockResult = mock(PutObjectResult.class);
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(mockResult);

            // Act
            String resultUrl = storageService.upload(inputStream, fileName, contentType);

            // Assert
            assertThat(resultUrl).isNotNull();
            assertThat(resultUrl).contains("test-file.png");
        }

        @Test
        @DisplayName("处理空文件名")
        void upload_Success_EmptyFileName() {
            // Arrange
            String fileName = "";
            String contentType = "image/jpeg";
            InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

            PutObjectResult mockResult = mock(PutObjectResult.class);
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(mockResult);

            // Act
            String resultUrl = storageService.upload(inputStream, fileName, contentType);

            // Assert
            assertThat(resultUrl).isNotNull();
            assertThat(resultUrl).contains("unnamed_file");
        }

        @Test
        @DisplayName("处理带特殊字符的文件名")
        void upload_Success_SpecialCharactersInFileName() {
            // Arrange
            String fileName = "test file@#$%.png";
            String contentType = "image/png";
            InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

            PutObjectResult mockResult = mock(PutObjectResult.class);
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(mockResult);

            // Act
            String resultUrl = storageService.upload(inputStream, fileName, contentType);

            // Assert
            assertThat(resultUrl).isNotNull();
            // 特殊字符应被下划线替换
            assertThat(resultUrl).doesNotContain("@");
            assertThat(resultUrl).doesNotContain("#");
        }
    }

    @Nested
    @DisplayName("download() 方法测试")
    class DownloadTests {

        @Test
        @DisplayName("成功下载文件")
        void download_Success() {
            // Arrange
            String fileUrl = URL_PREFIX + "/2025/12/26/test-file.png";
            String fileContent = "test file content";

            OSSObject mockObject = mock(OSSObject.class);
            when(mockObject.getObjectContent())
                    .thenReturn(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)));

            when(ossClient.getObject(eq(BUCKET_NAME), anyString())).thenReturn(mockObject);

            // Act
            InputStream result = storageService.download(fileUrl);

            // Assert
            assertThat(result).isNotNull();
            verify(ossClient).getObject(eq(BUCKET_NAME), eq("2025/12/26/test-file.png"));
        }

        @Test
        @DisplayName("下载失败 - 文件不存在")
        void download_Fails_FileNotFound() {
            // Arrange
            String fileUrl = URL_PREFIX + "/2025/12/26/non-existent.png";

            when(ossClient.getObject(eq(BUCKET_NAME), anyString()))
                    .thenThrow(new com.aliyun.oss.OSSException("File not found"));

            // Act & Assert
            assertThatThrownBy(() -> storageService.download(fileUrl))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("文件下载失败");
        }

        @Test
        @DisplayName("下载失败 - 空URL")
        void download_Fails_EmptyUrl() {
            // Act & Assert
            assertThatThrownBy(() -> storageService.download(""))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("文件URL不能为空");
        }

        @Test
        @DisplayName("下载失败 - 无效URL格式")
        void download_Fails_InvalidUrl() {
            // Arrange
            String invalidUrl = "not-a-valid-url";

            // Act & Assert
            assertThatThrownBy(() -> storageService.download(invalidUrl))
                    .isInstanceOf(StorageException.class);
        }

        @Test
        @DisplayName("成功从CDN URL下载文件")
        void download_Success_CdnUrl() {
            // Arrange
            String cdnUrl = "https://cdn.example.com/2025/12/26/test-file.png";
            lenient().when(ossConfig.getUrlPrefix()).thenReturn("https://cdn.example.com");

            String fileContent = "test content";
            OSSObject mockObject = mock(OSSObject.class);
            when(mockObject.getObjectContent())
                    .thenReturn(new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8)));

            when(ossClient.getObject(eq(BUCKET_NAME), anyString())).thenReturn(mockObject);

            // Act
            InputStream result = storageService.download(cdnUrl);

            // Assert
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("delete() 方法测试")
    class DeleteTests {

        @Test
        @DisplayName("成功删除文件")
        void delete_Success() {
            // Arrange
            String fileUrl = URL_PREFIX + "/2025/12/26/test-file.png";

            // Act
            storageService.delete(fileUrl);

            // Assert
            verify(ossClient).deleteObject(eq(BUCKET_NAME), eq("2025/12/26/test-file.png"));
        }

        @Test
        @DisplayName("删除不存在的文件不抛异常")
        void delete_NonExistentFile_NoException() {
            // Arrange
            String fileUrl = URL_PREFIX + "/2025/12/26/non-existent.png";

            // Act
            storageService.delete(fileUrl);

            // Assert
            verify(ossClient).deleteObject(eq(BUCKET_NAME), eq("2025/12/26/non-existent.png"));
        }

        @Test
        @DisplayName("删除失败 - 空URL")
        void delete_Fails_EmptyUrl() {
            // Act & Assert
            assertThatThrownBy(() -> storageService.delete(""))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("文件URL不能为空");
        }

        @Test
        @DisplayName("删除失败 - OSS异常")
        void delete_Fails_OssException() {
            // Arrange
            String fileUrl = URL_PREFIX + "/2025/12/26/test-file.png";

            doThrow(new com.aliyun.oss.OSSException("Access denied"))
                    .when(ossClient).deleteObject(anyString(), anyString());

            // Act & Assert
            assertThatThrownBy(() -> storageService.delete(fileUrl))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("文件删除失败");
        }
    }

    @Nested
    @DisplayName("generatePresignedUrl() 方法测试")
    class GeneratePresignedUrlTests {

        @Test
        @DisplayName("成功生成预签名URL")
        void generatePresignedUrl_Success() throws Exception {
            // Arrange
            String fileKey = "2025/12/26/test-file.png";
            int expirationMinutes = 30;

            java.net.URL mockUrl = new java.net.URL("https://test-bucket.oss-cn-hangzhou.aliyuncs.com/2025/12/26/test-file.png?expires=1234567890");
            when(ossClient.generatePresignedUrl(eq(BUCKET_NAME), eq(fileKey), any(java.util.Date.class)))
                    .thenReturn(mockUrl);

            // Act
            String result = storageService.generatePresignedUrl(fileKey, expirationMinutes);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).contains(fileKey);

            // 验证过期时间
            ArgumentCaptor<java.util.Date> expirationCaptor = ArgumentCaptor.forClass(java.util.Date.class);
            verify(ossClient).generatePresignedUrl(eq(BUCKET_NAME), eq(fileKey), expirationCaptor.capture());

            long expectedExpiration = System.currentTimeMillis() + expirationMinutes * 60 * 1000L;
            long actualExpiration = expirationCaptor.getValue().getTime();
            assertThat(actualExpiration).isCloseTo(expectedExpiration, Percentage.withPercentage(5000L)); // 5秒误差
        }

        @Test
        @DisplayName("生成1小时有效期的预签名URL")
        void generatePresignedUrl_Success_OneHourExpiration() throws Exception {
            // Arrange
            String fileKey = "2025/12/26/test-file.png";
            int expirationMinutes = 60;

            java.net.URL mockUrl = new java.net.URL("https://test-bucket.oss-cn-hangzhou.aliyuncs.com/2025/12/26/test-file.png?expires=1234567890");
            when(ossClient.generatePresignedUrl(eq(BUCKET_NAME), eq(fileKey), any(java.util.Date.class)))
                    .thenReturn(mockUrl);

            // Act
            String result = storageService.generatePresignedUrl(fileKey, expirationMinutes);

            // Assert
            assertThat(result).isNotNull();

            ArgumentCaptor<java.util.Date> expirationCaptor = ArgumentCaptor.forClass(java.util.Date.class);
            verify(ossClient).generatePresignedUrl(eq(BUCKET_NAME), eq(fileKey), expirationCaptor.capture());

            long expectedExpiration = System.currentTimeMillis() + 60 * 60 * 1000L;
            long actualExpiration = expirationCaptor.getValue().getTime();
            assertThat(actualExpiration).isCloseTo(expectedExpiration, Percentage.withPercentage(5000L));
        }

        @Test
        @DisplayName("生成失败 - OSS异常")
        void generatePresignedUrl_Fails_OssException() {
            // Arrange
            String fileKey = "2025/12/26/test-file.png";

            when(ossClient.generatePresignedUrl(eq(BUCKET_NAME), eq(fileKey), any(java.util.Date.class)))
                    .thenThrow(new com.aliyun.oss.OSSException("Access denied"));

            // Act & Assert
            assertThatThrownBy(() -> storageService.generatePresignedUrl(fileKey, 30))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("预签名URL生成失败");
        }
    }

    @Nested
    @DisplayName("配置验证测试")
    class ConfigurationTests {

        @Test
        @DisplayName("初始化时缺少endpoint配置")
        void init_Fails_MissingEndpoint() {
            // Arrange
            lenient().when(ossConfig.getEndpoint()).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> storageService.init())
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("endpoint不能为空");
        }

        @Test
        @DisplayName("初始化时缺少bucket配置")
        void init_Fails_MissingBucket() {
            // Arrange
            lenient().when(ossConfig.getEndpoint()).thenReturn(ENDPOINT);
            lenient().when(ossConfig.getBucket()).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> storageService.init())
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("bucket不能为空");
        }

        @Test
        @DisplayName("初始化时缺少accessKeyId配置")
        void init_Fails_MissingAccessKeyId() {
            // Arrange
            lenient().when(ossConfig.getEndpoint()).thenReturn(ENDPOINT);
            lenient().when(ossConfig.getBucket()).thenReturn(BUCKET_NAME);
            lenient().when(ossConfig.getAccessKeyId()).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> storageService.init())
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("accessKeyId不能为空");
        }

        @Test
        @DisplayName("初始化时缺少accessKeySecret配置")
        void init_Fails_MissingAccessKeySecret() {
            // Arrange
            lenient().when(ossConfig.getEndpoint()).thenReturn(ENDPOINT);
            lenient().when(ossConfig.getBucket()).thenReturn(BUCKET_NAME);
            lenient().when(ossConfig.getAccessKeyId()).thenReturn(ACCESS_KEY_ID);
            lenient().when(ossConfig.getAccessKeySecret()).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> storageService.init())
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("accessKeySecret不能为空");
        }

        @Test
        @DisplayName("初始化时存储桶不存在")
        void init_Fails_BucketNotExist() {
            // Arrange - 清除setUp中的通用stubbing，设置存储桶不存在
            reset(ossClient);
            when(ossClient.doesBucketExist(BUCKET_NAME)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> storageService.init())
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("存储桶不存在");
        }
    }

    @Nested
    @DisplayName("URL处理测试")
    class UrlHandlingTests {

        @Test
        @DisplayName("确保HTTP endpoint被转换为HTTPS")
        void upload_ConvertsHttpToHttps() {
            // Arrange
            lenient().when(ossConfig.getEndpoint()).thenReturn("http://oss-cn-hangzhou.aliyuncs.com");

            String fileName = "test.png";
            String contentType = "image/png";
            InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

            PutObjectResult mockResult = mock(PutObjectResult.class);
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(mockResult);

            // Act
            String resultUrl = storageService.upload(inputStream, fileName, contentType);

            // Assert
            assertThat(resultUrl).isNotNull();
            assertThat(resultUrl).startsWith("https://");
        }

        @Test
        @DisplayName("处理无协议的endpoint")
        void upload_AddsHttpsToEndpointWithoutProtocol() {
            // Arrange
            lenient().when(ossConfig.getEndpoint()).thenReturn("oss-cn-hangzhou.aliyuncs.com");

            String fileName = "test.png";
            String contentType = "image/png";
            InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

            PutObjectResult mockResult = mock(PutObjectResult.class);
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(mockResult);

            // Act
            String resultUrl = storageService.upload(inputStream, fileName, contentType);

            // Assert
            assertThat(resultUrl).isNotNull();
            assertThat(resultUrl).startsWith("https://");
        }

        @Test
        @DisplayName("使用自定义URL前缀")
        void upload_UsesCustomUrlPrefix() {
            // Arrange
            String customPrefix = "https://cdn.example.com";
            lenient().when(ossConfig.getUrlPrefix()).thenReturn(customPrefix);

            String fileName = "test.png";
            String contentType = "image/png";
            InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

            PutObjectResult mockResult = mock(PutObjectResult.class);
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(mockResult);

            // Act
            String resultUrl = storageService.upload(inputStream, fileName, contentType);

            // Assert
            assertThat(resultUrl).isNotNull();
            assertThat(resultUrl).startsWith(customPrefix);
        }
    }

    @Nested
    @DisplayName("支持的文件类型测试")
    class SupportedContentTypeTests {

        @Test
        @DisplayName("支持所有图片格式")
        void upload_Accepts_AllImageFormats() {
            // Arrange
            PutObjectResult mockResult = mock(PutObjectResult.class);
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(mockResult);

            String[] imageTypes = {
                    "image/jpeg",
                    "image/jpg",
                    "image/png",
                    "image/gif",
                    "image/webp",
                    "image/bmp",
                    "image/svg+xml"
            };

            // Act & Assert
            for (String contentType : imageTypes) {
                InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
                String resultUrl = storageService.upload(inputStream, "test." + contentType.split("/")[1], contentType);
                assertThat(resultUrl).isNotNull();
            }
        }

        @Test
        @DisplayName("支持所有视频格式")
        void upload_Accepts_AllVideoFormats() {
            // Arrange
            PutObjectResult mockResult = mock(PutObjectResult.class);
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(mockResult);

            String[] videoTypes = {
                    "video/mp4",
                    "video/webm",
                    "video/ogg",
                    "video/avi",
                    "video/quicktime",
                    "video/x-msvideo"
            };

            // Act & Assert
            for (String contentType : videoTypes) {
                InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
                String resultUrl = storageService.upload(inputStream, "test." + contentType.split("/")[1], contentType);
                assertThat(resultUrl).isNotNull();
            }
        }

        @Test
        @DisplayName("处理带参数的内容类型")
        void upload_Handles_ContentTypeWithParameters() {
            // Arrange
            String contentType = "image/jpeg; charset=utf-8";
            InputStream inputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

            PutObjectResult mockResult = mock(PutObjectResult.class);
            when(ossClient.putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                    .thenReturn(mockResult);

            // Act
            String resultUrl = storageService.upload(inputStream, "test.jpg", contentType);

            // Assert
            assertThat(resultUrl).isNotNull();
        }
    }
}
// {{END_MODIFICATIONS}}
