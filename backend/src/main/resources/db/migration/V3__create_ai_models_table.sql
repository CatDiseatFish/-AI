SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- AI模型配置表
-- =========================
CREATE TABLE ai_models (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模型ID',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '模型代码，如 gemini-3-pro-preview',
    name VARCHAR(200) NOT NULL COMMENT '模型显示名称',
    type VARCHAR(50) NOT NULL COMMENT '模型类型: LANGUAGE, IMAGE, VIDEO',
    provider VARCHAR(100) NULL COMMENT '模型提供商，如 Google, OpenAI',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用: 1=启用, 0=禁用',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    description TEXT NULL COMMENT '模型描述',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    INDEX idx_type (type),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';

-- 初始化模型数据
INSERT INTO ai_models (code, name, type, provider, enabled, sort_order) VALUES
('gemini-3-pro-preview', 'Gemini 3 Pro Preview', 'LANGUAGE', 'Google', 1, 1),
('jimeng-4.5', '即梦 4.5', 'IMAGE', 'JiMeng', 1, 1),
('gemini-3-pro-image-preview', 'Gemini 3 Pro Image Preview', 'IMAGE', 'Google', 1, 2),
('sora-2', 'Sora 2', 'VIDEO', 'OpenAI', 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
