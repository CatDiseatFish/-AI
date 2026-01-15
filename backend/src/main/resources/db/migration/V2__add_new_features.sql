SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 1) 风格预设表
-- =========================
CREATE TABLE style_presets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '风格预设ID',
    code VARCHAR(64) NOT NULL UNIQUE COMMENT '风格代码：celluloid/urban_korean/pixar/disney3d/ghibli/anime',
    name VARCHAR(64) NOT NULL COMMENT '风格名称：赛璐璐/都市韩漫/皮克斯/迪士尼3D/宫崎骏/日漫',
    thumbnail_url VARCHAR(512) NULL COMMENT '预览缩略图URL',
    prompt_prefix TEXT NULL COMMENT '风格提示词前缀（生图时自动拼接）',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风格预设表';

-- 初始化风格数据
INSERT INTO style_presets (code, name, sort_order, enabled) VALUES
('celluloid', '赛璐璐', 1, 1),
('urban_korean', '都市韩漫', 2, 1),
('pixar', '皮克斯', 3, 1),
('disney3d', '迪士尼3D', 4, 1),
('ghibli', '宫崎骏', 5, 1),
('anime', '日漫', 6, 1);

-- =========================
-- 2) 指令库（提示词模板）
-- =========================
CREATE TABLE prompt_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    user_id BIGINT NULL COMMENT '所属用户ID（NULL表示系统预设）',
    category VARCHAR(32) NOT NULL COMMENT '类别：CHARACTER/SCENE/SHOT/VIDEO/CUSTOM',
    name VARCHAR(128) NOT NULL COMMENT '模板名称',
    content TEXT NOT NULL COMMENT '模板内容',
    is_system TINYINT NOT NULL DEFAULT 0 COMMENT '是否系统预设',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    KEY idx_user (user_id),
    KEY idx_category (category),
    CONSTRAINT fk_prompt_template_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指令库/提示词模板';

-- =========================
-- 3) 邀请码表
-- =========================
CREATE TABLE invite_codes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '邀请码ID',
    user_id BIGINT NOT NULL COMMENT '邀请人用户ID',
    code VARCHAR(32) NOT NULL UNIQUE COMMENT '邀请码（唯一）',
    reward_points INT NOT NULL DEFAULT 100 COMMENT '被邀请人获得积分',
    inviter_reward_points INT NOT NULL DEFAULT 50 COMMENT '邀请人获得积分',
    max_uses INT NULL COMMENT '最大使用次数（NULL不限）',
    used_count INT NOT NULL DEFAULT 0 COMMENT '已使用次数',
    expire_at DATETIME(3) NULL COMMENT '过期时间（NULL永不过期）',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    KEY idx_user (user_id),
    CONSTRAINT fk_invite_code_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请码表';

-- =========================
-- 4) 邀请记录表
-- =========================
CREATE TABLE invite_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '邀请记录ID',
    invite_code_id BIGINT NOT NULL COMMENT '邀请码ID',
    inviter_user_id BIGINT NOT NULL COMMENT '邀请人用户ID',
    invitee_user_id BIGINT NOT NULL COMMENT '被邀请人用户ID',
    inviter_reward INT NOT NULL COMMENT '邀请人实际获得积分',
    invitee_reward INT NOT NULL COMMENT '被邀请人实际获得积分',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_invitee (invitee_user_id) COMMENT '每人只能被邀请一次',
    KEY idx_inviter (inviter_user_id),
    CONSTRAINT fk_invite_record_code FOREIGN KEY (invite_code_id) REFERENCES invite_codes(id),
    CONSTRAINT fk_invite_record_inviter FOREIGN KEY (inviter_user_id) REFERENCES users(id),
    CONSTRAINT fk_invite_record_invitee FOREIGN KEY (invitee_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请记录表';

-- =========================
-- 5) 修改 projects 表：添加时代设置
-- =========================
ALTER TABLE projects
ADD COLUMN era_setting VARCHAR(64) NULL COMMENT '时代设置（如：现代/古代/未来）' AFTER style_code;

SET FOREIGN_KEY_CHECKS = 1;
