-- V10: 添加道具相关表（道具分类、道具库、项目道具）
-- 结构与场景表保持一致

SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 道具分类表（账号级）
-- =========================
CREATE TABLE prop_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '道具分类ID',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    UNIQUE KEY uk_user_name (user_id, name),
    KEY idx_user (user_id),
    CONSTRAINT fk_prop_cat_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='道具分类（账号级）';

-- =========================
-- 道具库表（全局，跨项目复用）
-- =========================
CREATE TABLE prop_library (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '全局道具ID（账号级）',
    user_id BIGINT NOT NULL COMMENT '所属用户ID',
    category_id BIGINT NULL COMMENT '所属分类ID（可空）',
    name VARCHAR(64) NOT NULL COMMENT '道具名称',
    description TEXT NULL COMMENT '道具描述/提示词（主档）',
    thumbnail_url VARCHAR(512) NULL COMMENT '道具缩略图URL',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    deleted_at DATETIME(3) NULL COMMENT '软删除时间',
    KEY idx_user (user_id),
    KEY idx_category (category_id),
    CONSTRAINT fk_prop_lib_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_prop_lib_cat FOREIGN KEY (category_id) REFERENCES prop_categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局道具库（跨项目复用的道具主档）';

-- =========================
-- 项目道具表（引用全局道具库）
-- =========================
CREATE TABLE project_props (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目道具引用ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    library_prop_id BIGINT NOT NULL COMMENT '引用的全局道具ID',
    display_name VARCHAR(64) NULL COMMENT '项目内显示名（可覆盖全局名称）',
    override_description TEXT NULL COMMENT '项目内覆盖描述/提示词（可为空表示使用全局）',
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    UNIQUE KEY uk_project_lib_prop (project_id, library_prop_id),
    KEY idx_project (project_id),
    CONSTRAINT fk_proj_prop_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_proj_prop_lib FOREIGN KEY (library_prop_id) REFERENCES prop_library(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目道具（引用全局道具库，支持项目内覆盖）';

SET FOREIGN_KEY_CHECKS = 1;
