package com.ym.ai_story_studio_server.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片拼接工具类
 * 用于将多张图片横向拼接成一张画布
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Component
public class ImageMergeUtil {

    private static final int PADDING = 20; // 图片之间的间距
    private static final int MAX_HEIGHT = 1024; // 最大高度
    private static final Color BACKGROUND_COLOR = Color.WHITE; // 背景颜色
    private static final int COMPOSITE_CANVAS_WIDTH = 1280;
    private static final int COMPOSITE_CELL_HEIGHT = 360;
    private static final int COMPOSITE_LABEL_HEIGHT = 40;
    private static final Font COMPOSITE_LABEL_FONT = new Font("SansSerif", Font.BOLD, 24);

    public static class ImageItem {
        private final String label;
        private final String imageUrl;

        public ImageItem(String label, String imageUrl) {
            this.label = label;
            this.imageUrl = imageUrl;
        }

        public String getLabel() {
            return label;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }

    /**
     * 将多个图片URL横向拼接成一张图片
     *
     * @param imageUrls 图片URL列表
     * @return 拼接后的图片字节数组
     */
    public byte[] mergeImagesHorizontally(List<String> imageUrls) throws IOException {
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new IllegalArgumentException("图片URL列表不能为空");
        }

        log.info("开始拼接图片，共 {} 张", imageUrls.size());

        // 1. 下载所有图片
        List<BufferedImage> images = new ArrayList<>();
        for (String url : imageUrls) {
            try {
                BufferedImage image = downloadImage(url);
                if (image != null) {
                    images.add(image);
                    log.debug("成功加载图片: {}", url);
                }
            } catch (Exception e) {
                log.warn("加载图片失败，跳过: {}", url, e);
            }
        }

        if (images.isEmpty()) {
            throw new IOException("没有成功加载任何图片");
        }

        // 2. 计算目标尺寸
        int targetHeight = MAX_HEIGHT;
        int totalWidth = 0;

        // 按比例缩放所有图片到相同高度
        List<BufferedImage> resizedImages = new ArrayList<>();
        for (BufferedImage img : images) {
            int newWidth = (int) ((double) targetHeight / img.getHeight() * img.getWidth());
            BufferedImage resized = resizeImage(img, newWidth, targetHeight);
            resizedImages.add(resized);
            totalWidth += newWidth;
        }

        // 加上间距
        totalWidth += PADDING * (resizedImages.size() + 1);

        // 3. 创建画布
        BufferedImage canvas = new BufferedImage(totalWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvas.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 填充背景
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, totalWidth, targetHeight);

        // 4. 将图片绘制到画布上
        int currentX = PADDING;
        for (BufferedImage img : resizedImages) {
            g2d.drawImage(img, currentX, 0, null);
            currentX += img.getWidth() + PADDING;
        }

        g2d.dispose();

        // 5. 转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(canvas, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();

        log.info("图片拼接完成，总宽度: {}, 高度: {}, 大小: {} KB", 
                totalWidth, targetHeight, imageBytes.length / 1024);

        return imageBytes;
    }

    /**
     * 将多张图片按两列多行拼接，并在图片下方附带标签
     *
     * @param items 图片与标签信息
     * @return 拼接后的图片字节数组
     */
    public byte[] mergeImagesGridWithLabels(List<ImageItem> items) throws IOException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("图片列表不能为空");
        }

        log.info("开始拼接图片（网格），共 {} 张", items.size());

        List<ImageWithLabel> images = new ArrayList<>();
        for (ImageItem item : items) {
            if (item == null || item.getImageUrl() == null || item.getImageUrl().isBlank()) {
                continue;
            }
            try {
                BufferedImage image = downloadImage(item.getImageUrl());
                if (image != null) {
                    images.add(new ImageWithLabel(image, item.getLabel()));
                    log.debug("成功加载图片: {}", item.getImageUrl());
                }
            } catch (Exception e) {
                log.warn("加载图片失败，跳过: {}", item.getImageUrl(), e);
            }
        }

        if (images.isEmpty()) {
            throw new IOException("没有成功加载任何图片");
        }

        int cols = 2;
        int cellWidth = (COMPOSITE_CANVAS_WIDTH - PADDING * (cols + 1)) / cols;
        int rows = (int) Math.ceil(images.size() / (double) cols);
        int totalHeight = PADDING + rows * (COMPOSITE_CELL_HEIGHT + COMPOSITE_LABEL_HEIGHT + PADDING) + PADDING;

        BufferedImage canvas = new BufferedImage(COMPOSITE_CANVAS_WIDTH, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvas.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, COMPOSITE_CANVAS_WIDTH, totalHeight);

        g2d.setFont(COMPOSITE_LABEL_FONT);
        FontMetrics fontMetrics = g2d.getFontMetrics();

        for (int i = 0; i < images.size(); i++) {
            int row = i / cols;
            int col = i % cols;
            int left = PADDING + col * (cellWidth + PADDING);
            int top = PADDING + row * (COMPOSITE_CELL_HEIGHT + COMPOSITE_LABEL_HEIGHT + PADDING);

            BufferedImage image = images.get(i).image();
            int[] scaled = scaleToFit(image.getWidth(), image.getHeight(), cellWidth, COMPOSITE_CELL_HEIGHT);
            int drawWidth = scaled[0];
            int drawHeight = scaled[1];
            int imageX = left + (cellWidth - drawWidth) / 2;
            int imageY = top + (COMPOSITE_CELL_HEIGHT - drawHeight) / 2;
            g2d.drawImage(image, imageX, imageY, drawWidth, drawHeight, null);

            String label = images.get(i).label();
            if (label != null && !label.isBlank()) {
                int labelTop = top + COMPOSITE_CELL_HEIGHT;
                g2d.setColor(BACKGROUND_COLOR);
                g2d.fillRect(left, labelTop, cellWidth, COMPOSITE_LABEL_HEIGHT);
                g2d.setColor(Color.BLACK);

                int textWidth = fontMetrics.stringWidth(label);
                int textX = left + (cellWidth - textWidth) / 2;
                int textY = labelTop + (COMPOSITE_LABEL_HEIGHT - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();
                g2d.drawString(label, textX, textY);
            }
        }

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(canvas, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();

        log.info("图片拼接完成（网格），宽度: {}, 高度: {}, 大小: {} KB",
                COMPOSITE_CANVAS_WIDTH, totalHeight, imageBytes.length / 1024);

        return imageBytes;
    }

    private int[] scaleToFit(int srcWidth, int srcHeight, int maxWidth, int maxHeight) {
        double scale = Math.min((double) maxWidth / srcWidth, (double) maxHeight / srcHeight);
        int width = Math.max(1, (int) Math.round(srcWidth * scale));
        int height = Math.max(1, (int) Math.round(srcHeight * scale));
        return new int[] { width, height };
    }

    /**
     * 从URL下载图片
     */
    private BufferedImage downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream in = url.openStream()) {
            return ImageIO.read(in);
        }
    }

    /**
     * 等比例缩放图片
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    private static class ImageWithLabel {
        private final BufferedImage image;
        private final String label;

        private ImageWithLabel(BufferedImage image, String label) {
            this.image = image;
            this.label = label;
        }

        private BufferedImage image() {
            return image;
        }

        private String label() {
            return label;
        }
    }

    /**
     * 将字节数组转换为BufferedImage
     */
    public BufferedImage bytesToImage(byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            return ImageIO.read(bais);
        }
    }
}
