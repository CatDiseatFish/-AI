package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.service.StorageService;
import com.ym.ai_story_studio_server.util.ImageMergeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片工具Controller
 * 提供图片拼接等工具接口
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/utils/images")
@RequiredArgsConstructor
public class ImageUtilController {

    private final ImageMergeUtil imageMergeUtil;
    private final StorageService storageService;

    /**
     * 拼接多张图片
     *
     * @param request 包含图片URL列表的请求
     * @return 拼接后的图片URL
     */
    @PostMapping("/merge")
    public Result<Map<String, String>> mergeImages(@RequestBody MergeImagesRequest request) {
        int urlCount = request.imageUrls() == null ? 0 : request.imageUrls().size();
        int itemCount = request.imageItems() == null ? 0 : request.imageItems().size();
        log.info("收到图片拼接请求，imageUrls: {}, imageItems: {}", urlCount, itemCount);

        try {
            // 1. 拼接图片
            byte[] mergedImageBytes;
            if (request.imageItems() != null && !request.imageItems().isEmpty()) {
                mergedImageBytes = imageMergeUtil.mergeImagesGridWithLabels(
                        request.imageItems().stream()
                                .map(item -> new ImageMergeUtil.ImageItem(item.label(), item.imageUrl()))
                                .collect(java.util.stream.Collectors.toList())
                );
            } else if (request.storyboardImageUrl() != null || request.sceneImageUrl() != null
                    || (request.characters() != null && !request.characters().isEmpty())) {
                mergedImageBytes = imageMergeUtil.mergeImagesGridWithLabels(
                        buildItemsFromStructuredRequest(request)
                );
            } else {
                mergedImageBytes = imageMergeUtil.mergeImagesHorizontally(request.imageUrls());
            }

            // 2. 上传到OSS
            String ossUrl = storageService.uploadImageBytes(
                    mergedImageBytes,
                    "merged_" + System.currentTimeMillis() + ".png"
            );

            log.info("图片拼接并上传成功: {}", ossUrl);

            Map<String, String> response = new HashMap<>();
            response.put("mergedImageUrl", ossUrl);

            return Result.success(response);

        } catch (Exception e) {
            log.error("图片拼接失败", e);
            return Result.error(500, "图片拼接失败: " + e.getMessage());
        }
    }

    /**
     * 图片拼接请求DTO
     */
    public record MergeImagesRequest(
            List<String> imageUrls,
            String storyboardImageUrl,
            String sceneImageUrl,
            List<CharacterItem> characters,
            List<ImageItem> imageItems
    ) {
    }

    public record CharacterItem(
            String name,
            String imageUrl
    ) {
    }

    public record ImageItem(
            String label,
            String imageUrl
    ) {
    }

    private List<ImageMergeUtil.ImageItem> buildItemsFromStructuredRequest(MergeImagesRequest request) {
        List<ImageMergeUtil.ImageItem> items = new java.util.ArrayList<>();
        if (request.storyboardImageUrl() != null && !request.storyboardImageUrl().isBlank()) {
            items.add(new ImageMergeUtil.ImageItem("镜头参考", request.storyboardImageUrl()));
        }
        if (request.sceneImageUrl() != null && !request.sceneImageUrl().isBlank()) {
            items.add(new ImageMergeUtil.ImageItem("场景参考", request.sceneImageUrl()));
        }
        if (request.characters() != null) {
            for (CharacterItem character : request.characters()) {
                if (character == null || character.imageUrl() == null || character.imageUrl().isBlank()) {
                    continue;
                }
                String label = character.name() != null && !character.name().isBlank()
                        ? character.name() + " 人物参考"
                        : "人物参考";
                items.add(new ImageMergeUtil.ImageItem(label, character.imageUrl()));
            }
        }
        return items;
    }
}
