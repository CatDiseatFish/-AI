SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 添加 GPT-4o-image-vip 模型
-- =========================

-- 插入 GPT-4o-image-vip 图像生成模型
INSERT INTO ai_models (code, name, type, provider, enabled, sort_order, description) 
VALUES (
    'gpt-4o-image-vip',
    'GPT-4o Image VIP',
    'IMAGE',
    'OpenAI',
    1,
    3,
    'GPT-4o 高级图像处理模型，支持图像理解和多模态对话'
);

SET FOREIGN_KEY_CHECKS = 1;
