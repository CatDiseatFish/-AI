SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 添加 GPT-4o 语言模型
-- =========================

-- 插入 GPT-4o 语言模型（用于文本生成）
INSERT INTO ai_models (code, name, type, provider, enabled, sort_order, description) 
VALUES (
    'gpt-4o',
    'GPT-4o',
    'LANGUAGE',
    'OpenAI',
    1,
    2,
    'OpenAI GPT-4o 多模态大语言模型，支持文本理解和生成'
);

SET FOREIGN_KEY_CHECKS = 1;
