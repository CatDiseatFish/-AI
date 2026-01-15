SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 1) 用户与登录
-- =========================

DROP TABLE IF EXISTS usage_charges;
DROP TABLE IF EXISTS pricing_rules;
DROP TABLE IF EXISTS job_items;
DROP TABLE IF EXISTS jobs;
DROP TABLE IF EXISTS asset_refs;
DROP TABLE IF EXISTS asset_versions;
DROP TABLE IF EXISTS assets;
DROP TABLE IF EXISTS shot_bindings;
DROP TABLE IF EXISTS storyboard_shots;
DROP TABLE IF EXISTS project_scenes;
DROP TABLE IF EXISTS scene_library;
DROP TABLE IF EXISTS scene_categories;
DROP TABLE IF EXISTS project_characters;
DROP TABLE IF EXISTS character_library;
DROP TABLE IF EXISTS character_categories;
DROP TABLE IF EXISTS wallet_transactions;
DROP TABLE IF EXISTS wallets;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS project_folders;
DROP TABLE IF EXISTS user_auth;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                       nickname VARCHAR(64) NULL COMMENT '昵称',
                       avatar_url VARCHAR(512) NULL COMMENT '头像URL',
                       status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
                       created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                       updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE user_auth (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '认证记录ID',
                           user_id BIGINT NOT NULL COMMENT '用户ID',
                           auth_type VARCHAR(32) NOT NULL COMMENT '认证类型：PHONE/EMAIL/WECHAT',
                           identifier VARCHAR(128) NOT NULL COMMENT '唯一标识：手机号/邮箱/微信openid等',
                           credential VARCHAR(255) NULL COMMENT '凭据：密码哈希（短信/微信可为空）',
                           verified TINYINT NOT NULL DEFAULT 0 COMMENT '是否已验证：1是，0否',
                           created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                           UNIQUE KEY uk_type_identifier (auth_type, identifier),
                           KEY idx_user_id (user_id),
                           CONSTRAINT fk_user_auth_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证表（支持多登录方式）';

-- =========================
-- 2) 钱包与积分流水
-- =========================

CREATE TABLE wallets (
                         user_id BIGINT PRIMARY KEY COMMENT '用户ID（同 users.id）',
                         balance INT NOT NULL DEFAULT 0 COMMENT '积分余额',
                         updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                         CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钱包（积分余额）';

CREATE TABLE wallet_transactions (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流水ID',
                                     user_id BIGINT NOT NULL COMMENT '用户ID',
                                     type VARCHAR(16) NOT NULL COMMENT '流水类型：RECHARGE/CONSUME/REFUND/ADJUST',
                                     amount INT NOT NULL COMMENT '变动积分：推荐消费为负数，充值为正数',
                                     balance_after INT NOT NULL COMMENT '变动后的余额',
                                     biz_type VARCHAR(32) NULL COMMENT '业务类型：JOB/ORDER等',
                                     biz_id BIGINT NULL COMMENT '业务ID：如 job_id 或 order_id',
                                     meta_json JSON NULL COMMENT '扩展信息（JSON）：如模型、数量、备注等',
                                     created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                     KEY idx_user (user_id),
                                     KEY idx_biz (biz_type, biz_id),
                                     CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水明细（可审计/可追溯）';

-- =========================
-- 3) 项目文件夹与项目
-- =========================

CREATE TABLE project_folders (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件夹ID',
                                 user_id BIGINT NOT NULL COMMENT '所属用户ID',
                                 name VARCHAR(128) NOT NULL COMMENT '文件夹名称',
                                 sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值（越小越靠前）',
                                 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                 deleted_at DATETIME(3) NULL COMMENT '软删除时间（非空表示已删除）',
                                 KEY idx_user (user_id),
                                 CONSTRAINT fk_folder_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目文件夹（用于首页分类）';

CREATE TABLE projects (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
                          user_id BIGINT NOT NULL COMMENT '所属用户ID',
                          folder_id BIGINT NULL COMMENT '所属文件夹ID（可为空表示未归类）',
                          name VARCHAR(128) NOT NULL COMMENT '项目名称',
                          aspect_ratio VARCHAR(16) NOT NULL DEFAULT '16:9' COMMENT '画幅比例：如16:9/9:16/21:9等',
                          style_code VARCHAR(64) NULL COMMENT '风格标识：如cinematic/anime等（自定义）',
                          raw_text MEDIUMTEXT NULL COMMENT '输入的小说/剧本文本原文',
                          status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '项目状态：DRAFT/PROCESSING/READY等',
                          model_config_json JSON NULL COMMENT '模型配置（JSON）：语言模型/生图模型/生视频模型等',
                          created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                          updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                          deleted_at DATETIME(3) NULL COMMENT '软删除时间',
                          KEY idx_user (user_id),
                          KEY idx_folder (folder_id),
                          KEY idx_user_name (user_id, name),
                          CONSTRAINT fk_project_user FOREIGN KEY (user_id) REFERENCES users(id),
                          CONSTRAINT fk_project_folder FOREIGN KEY (folder_id) REFERENCES project_folders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表（作品）';

-- =========================
-- 4) 全局角色库（跨项目复用）与分类
-- =========================

CREATE TABLE character_categories (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色分类ID',
                                      user_id BIGINT NOT NULL COMMENT '所属用户ID',
                                      name VARCHAR(64) NOT NULL COMMENT '分类名称',
                                      sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
                                      created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                      updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                      UNIQUE KEY uk_user_name (user_id, name),
                                      KEY idx_user (user_id),
                                      CONSTRAINT fk_char_cat_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色分类（账号级）';

CREATE TABLE character_library (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '全局角色ID（账号级）',
                                   user_id BIGINT NOT NULL COMMENT '所属用户ID',
                                   category_id BIGINT NULL COMMENT '所属分类ID（可空）',
                                   name VARCHAR(64) NOT NULL COMMENT '角色名称',
                                   description TEXT NULL COMMENT '角色描述/提示词（主档）',
                                   created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                   updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                   deleted_at DATETIME(3) NULL COMMENT '软删除时间',
                                   KEY idx_user (user_id),
                                   KEY idx_category (category_id),
                                   CONSTRAINT fk_char_lib_user FOREIGN KEY (user_id) REFERENCES users(id),
                                   CONSTRAINT fk_char_lib_cat FOREIGN KEY (category_id) REFERENCES character_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局角色库（跨项目复用的角色主档）';

CREATE TABLE project_characters (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目角色引用ID',
                                    project_id BIGINT NOT NULL COMMENT '项目ID',
                                    library_character_id BIGINT NOT NULL COMMENT '引用的全局角色ID',
                                    display_name VARCHAR(64) NULL COMMENT '项目内显示名（可覆盖全局名称）',
                                    override_description TEXT NULL COMMENT '项目内覆盖描述/提示词（可为空表示使用全局）',
                                    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                    UNIQUE KEY uk_project_lib_char (project_id, library_character_id),
                                    KEY idx_project (project_id),
                                    CONSTRAINT fk_proj_char_project FOREIGN KEY (project_id) REFERENCES projects(id),
                                    CONSTRAINT fk_proj_char_lib FOREIGN KEY (library_character_id) REFERENCES character_library(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目角色（引用全局角色库，支持项目内覆盖）';

-- =========================
-- 5) 全局场景库（跨项目复用）与分类
-- =========================

CREATE TABLE scene_categories (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '场景分类ID',
                                  user_id BIGINT NOT NULL COMMENT '所属用户ID',
                                  name VARCHAR(64) NOT NULL COMMENT '分类名称',
                                  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
                                  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                  UNIQUE KEY uk_user_name (user_id, name),
                                  KEY idx_user (user_id),
                                  CONSTRAINT fk_scene_cat_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场景分类（账号级）';

CREATE TABLE scene_library (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '全局场景ID（账号级）',
                               user_id BIGINT NOT NULL COMMENT '所属用户ID',
                               category_id BIGINT NULL COMMENT '所属分类ID（可空）',
                               name VARCHAR(64) NOT NULL COMMENT '场景名称',
                               description TEXT NULL COMMENT '场景描述/提示词（主档）',
                               created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                               updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                               deleted_at DATETIME(3) NULL COMMENT '软删除时间',
                               KEY idx_user (user_id),
                               KEY idx_category (category_id),
                               CONSTRAINT fk_scene_lib_user FOREIGN KEY (user_id) REFERENCES users(id),
                               CONSTRAINT fk_scene_lib_cat FOREIGN KEY (category_id) REFERENCES scene_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局场景库（跨项目复用的场景主档）';

CREATE TABLE project_scenes (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目场景引用ID',
                                project_id BIGINT NOT NULL COMMENT '项目ID',
                                library_scene_id BIGINT NOT NULL COMMENT '引用的全局场景ID',
                                display_name VARCHAR(64) NULL COMMENT '项目内显示名（可覆盖全局名称）',
                                override_description TEXT NULL COMMENT '项目内覆盖描述/提示词（可为空表示使用全局）',
                                created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                UNIQUE KEY uk_project_lib_scene (project_id, library_scene_id),
                                KEY idx_project (project_id),
                                CONSTRAINT fk_proj_scene_project FOREIGN KEY (project_id) REFERENCES projects(id),
                                CONSTRAINT fk_proj_scene_lib FOREIGN KEY (library_scene_id) REFERENCES scene_library(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目场景（引用全局场景库，支持项目内覆盖）';


-- =========================
-- 6) 分镜与绑定关系
-- =========================

CREATE TABLE storyboard_shots (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分镜ID',
                                  project_id BIGINT NOT NULL COMMENT '所属项目ID',
                                  shot_no INT NOT NULL COMMENT '分镜序号/排序（同一项目内唯一）',
                                  script_text TEXT NOT NULL COMMENT '分镜剧本文本（可编辑）',
                                  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                                  deleted_at DATETIME(3) NULL COMMENT '软删除时间',
                                  UNIQUE KEY uk_project_shotno (project_id, shot_no),
                                  KEY idx_project (project_id),
                                  CONSTRAINT fk_shot_project FOREIGN KEY (project_id) REFERENCES projects(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分镜表（Storyboard Shot）';

CREATE TABLE shot_bindings (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分镜绑定ID',
                               shot_id BIGINT NOT NULL COMMENT '分镜ID',
                               bind_type VARCHAR(16) NOT NULL COMMENT '绑定类型：PCHAR(项目角色)/PSCENE(场景)',
                               bind_id BIGINT NOT NULL COMMENT '绑定对象ID：project_characters.id 或 project_scenes.id',
                               created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                               UNIQUE KEY uk_shot_bind (shot_id, bind_type, bind_id),
                               KEY idx_shot (shot_id),
                               CONSTRAINT fk_binding_shot FOREIGN KEY (shot_id) REFERENCES storyboard_shots(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分镜与角色/场景的绑定关系（多对多）';

-- =========================
-- 7) 统一资产系统：assets + versions + refs
-- =========================

CREATE TABLE assets (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资产ID（逻辑资产）',
                        project_id BIGINT NOT NULL COMMENT '所属项目ID',
                        asset_type VARCHAR(24) NOT NULL COMMENT '资产类型：CHAR_IMG/SCENE_IMG/SHOT_IMG/VIDEO/REF_IMG/COMPOSITE_IMG等',
                        owner_type VARCHAR(16) NOT NULL COMMENT '归属对象类型：LIB_CHAR/PCHAR/LIB_SCENE/SHOT/PROJECT',
                        owner_id BIGINT NOT NULL COMMENT '归属对象ID（随 owner_type 变化）',
                        created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                        KEY idx_project (project_id),
                        KEY idx_owner (owner_type, owner_id),
                        CONSTRAINT fk_asset_project FOREIGN KEY (project_id) REFERENCES projects(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产主表（一个资产对应多个版本）';

CREATE TABLE asset_versions (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资产版本ID（历史记录）',
                                asset_id BIGINT NOT NULL COMMENT '资产ID',
                                version_no INT NOT NULL COMMENT '版本号（从1开始，同一asset内唯一）',
                                source VARCHAR(16) NOT NULL COMMENT '来源：AI/UPLOAD/IMPORT',
                                provider VARCHAR(16) NULL COMMENT '存储提供方：OSS/MINIO/REMOTE等',
                                url VARCHAR(1024) NOT NULL COMMENT '可访问URL（前端展示/下载）',
                                object_key VARCHAR(512) NULL COMMENT '对象存储Key（OSS/MinIO等，可选）',
                                prompt TEXT NULL COMMENT '生成提示词（可空）',
                                params_json JSON NULL COMMENT '生成参数（JSON）：比例、模型、seed、参考图等',
                                status VARCHAR(16) NOT NULL DEFAULT 'READY' COMMENT '状态：READY/FAILED等',
                                created_by BIGINT NULL COMMENT '创建人用户ID（可空）',
                                created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                UNIQUE KEY uk_asset_version (asset_id, version_no),
                                KEY idx_asset (asset_id),
                                CONSTRAINT fk_version_asset FOREIGN KEY (asset_id) REFERENCES assets(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产版本表（用于多版本预览/对比/复用）';

CREATE TABLE asset_refs (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资产引用ID（当前选用）',
                            project_id BIGINT NOT NULL COMMENT '所属项目ID',
                            ref_type VARCHAR(24) NOT NULL COMMENT '引用类型：LIB_CHAR_CURRENT/PCHAR_CURRENT/LIB_SCENE_CURRENT/PSCENE_CURRENT/SHOT_IMG_CURRENT/SHOT_VIDEO_CURRENT等',
                            ref_owner_id BIGINT NOT NULL COMMENT '引用对象ID（随 ref_type 变化）',
                            asset_version_id BIGINT NOT NULL COMMENT '当前选用的资产版本ID',
                            updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                            UNIQUE KEY uk_ref (ref_type, ref_owner_id),
                            KEY idx_project (project_id),
                            CONSTRAINT fk_ref_version FOREIGN KEY (asset_version_id) REFERENCES asset_versions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='当前选用版本引用（用于“当前展示/导出/生成视频拼接”）';

-- =========================
-- 8) 任务队列（解析/生图/生视频/导出ZIP）
-- =========================

CREATE TABLE jobs (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
                      user_id BIGINT NOT NULL COMMENT '发起用户ID',
                      project_id BIGINT NOT NULL COMMENT '所属项目ID',
                      job_type VARCHAR(32) NOT NULL COMMENT '任务类型：PARSE_TEXT/GEN_CHAR_IMG/GEN_SCENE_IMG/GEN_SHOT_IMG/GEN_VIDEO/EXPORT_ZIP',
                      status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING/RUNNING/SUCCEEDED/FAILED/CANCELED',
                      progress INT NOT NULL DEFAULT 0 COMMENT '任务进度：0-100',
                      total_items INT NOT NULL DEFAULT 0 COMMENT '子任务总数（批量生成用）',
                      done_items INT NOT NULL DEFAULT 0 COMMENT '已完成子任务数',
                      started_at DATETIME(3) NULL COMMENT '开始时间（用于计时展示）',
                      finished_at DATETIME(3) NULL COMMENT '结束时间',
                      error_message TEXT NULL COMMENT '错误信息（失败时）',
                      meta_json JSON NULL COMMENT '扩展参数（JSON）：如模型、比例、导出模式（current/all）等',
                      created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                      updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
                      KEY idx_project_created (project_id, created_at),
                      KEY idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务主表（用于队列小窗展示与异步处理）';

CREATE TABLE job_items (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '子任务ID',
                           job_id BIGINT NOT NULL COMMENT '所属任务ID',
                           target_type VARCHAR(16) NOT NULL COMMENT '目标对象类型：LIB_CHAR/PCHAR/LIB_SCENE/PSCENE/SHOT/PROJECT',
                           target_id BIGINT NOT NULL COMMENT '目标对象ID',
                           status VARCHAR(16) NOT NULL DEFAULT 'PENDING' COMMENT '子任务状态：PENDING/RUNNING/SUCCEEDED/FAILED/CANCELED',
                           input_json JSON NULL COMMENT '输入参数（JSON）：prompt、参考图version_id、数量、比例等',
                           output_asset_version_id BIGINT NULL COMMENT '输出资产版本ID（成功后写入）',
                           error_message TEXT NULL COMMENT '子任务错误信息',
                           started_at DATETIME(3) NULL COMMENT '子任务开始时间',
                           finished_at DATETIME(3) NULL COMMENT '子任务结束时间',
                           created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                           KEY idx_job (job_id),
                           CONSTRAINT fk_item_job FOREIGN KEY (job_id) REFERENCES jobs(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务子项（批量生成：每个分镜/角色/场景一条）';

-- =========================
-- 9) 计费规则（分模型/分业务）与使用扣费记录
-- =========================

CREATE TABLE pricing_rules (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计费规则ID',
                               biz_type VARCHAR(16) NOT NULL COMMENT '业务类型：IMAGE/VIDEO/TEXT',
                               model_code VARCHAR(64) NOT NULL COMMENT '模型标识：jimeng-4.5/sora-2/gemini等',
                               unit VARCHAR(16) NOT NULL COMMENT '计费单位：PER_ITEM/PER_SECOND/PER_1K_TOKENS等',
                               price INT NOT NULL COMMENT '单价（积分）',
                               enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用，0禁用',
                               created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                               UNIQUE KEY uk_biz_model (biz_type, model_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计费规则（不同模型不同价格）';

CREATE TABLE usage_charges (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '扣费记录ID',
                               user_id BIGINT NOT NULL COMMENT '用户ID',
                               job_id BIGINT NOT NULL COMMENT '关联任务ID',
                               biz_type VARCHAR(16) NOT NULL COMMENT '业务类型：IMAGE/VIDEO/TEXT',
                               model_code VARCHAR(64) NOT NULL COMMENT '模型标识',
                               quantity INT NOT NULL COMMENT '计费数量（如张数/秒数/千tokens单位数）',
                               unit_price INT NOT NULL COMMENT '单价（积分）',
                               total_cost INT NOT NULL COMMENT '总费用（积分，通常为正数；实际扣减写入钱包流水）',
                               created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                               KEY idx_user (user_id),
                               KEY idx_job (job_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='使用扣费记录（便于审计与对账）';

SET FOREIGN_KEY_CHECKS = 1;


-- 充值商品（套餐，可选）
CREATE TABLE IF NOT EXISTS recharge_products (
                                                 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '充值商品ID',
                                                 name VARCHAR(64) NOT NULL COMMENT '商品名称',
                                                 points INT NOT NULL COMMENT '到账积分',
                                                 price_cents INT NOT NULL COMMENT '售价（分）',
                                                 enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用，0禁用',
                                                 sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
                                                 created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                                 updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值商品（套餐）';

-- 自定义金额兑换规则：多少“分”兑换多少“积分”（可随时改规则，保留历史）
CREATE TABLE IF NOT EXISTS recharge_exchange_rules (
                                                       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '兑换规则ID',
                                                       name VARCHAR(64) NOT NULL COMMENT '规则名称',
                                                       currency VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '币种：默认CNY',
                                                       cents_per_point INT NOT NULL COMMENT '每多少“分”兑换1积分（如10表示0.1元=1积分）',
                                                       min_amount_cents INT NOT NULL DEFAULT 1 COMMENT '最小充值金额（分）',
                                                       max_amount_cents INT NULL COMMENT '最大充值金额（分，可空表示不限制）',
                                                       enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1启用，0禁用',
                                                       created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                                       updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自定义充值金额兑换规则';

-- 支付订单（网页扫码：scene=NATIVE）
CREATE TABLE IF NOT EXISTS pay_orders (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付订单ID',
                                          user_id BIGINT NOT NULL COMMENT '用户ID',
                                          product_id BIGINT NULL COMMENT '充值商品ID（可空：自定义金额）',
                                          exchange_rule_id BIGINT NULL COMMENT '兑换规则ID（自定义金额时使用）',

                                          order_no VARCHAR(64) NOT NULL COMMENT '商户订单号（业务侧生成，唯一）',
                                          provider VARCHAR(16) NOT NULL DEFAULT 'WECHAT' COMMENT '支付渠道：WECHAT等',
                                          scene VARCHAR(16) NOT NULL DEFAULT 'NATIVE' COMMENT '支付场景：NATIVE(网页扫码)/JSAPI/H5等',
                                          status VARCHAR(16) NOT NULL DEFAULT 'CREATED' COMMENT '状态：CREATED/PAYING/SUCCEEDED/FAILED/CANCELED/REFUNDED',

                                          amount_cents INT NOT NULL COMMENT '订单金额（分）',
                                          currency VARCHAR(8) NOT NULL DEFAULT 'CNY' COMMENT '币种：默认CNY',
                                          points INT NOT NULL COMMENT '支付成功应到账积分（下单时计算并固化）',

                                          title VARCHAR(128) NULL COMMENT '订单标题/商品描述（传给渠道）',
                                          client_ip VARCHAR(64) NULL COMMENT '下单客户端IP（可选）',
                                          expire_at DATETIME(3) NULL COMMENT '订单过期时间',

                                          provider_prepay_id VARCHAR(128) NULL COMMENT '渠道预支付ID（如微信prepay_id）',
                                          provider_pay_url VARCHAR(1024) NULL COMMENT '扫码支付URL（如微信code_url）',
                                          provider_trade_no VARCHAR(128) NULL COMMENT '渠道交易号（支付成功后回填）',

                                          paid_at DATETIME(3) NULL COMMENT '支付完成时间',
                                          closed_at DATETIME(3) NULL COMMENT '关闭/取消时间',
                                          created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                          updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

                                          UNIQUE KEY uk_order_no (order_no),
                                          KEY idx_user_created (user_id, created_at),
                                          KEY idx_status_created (status, created_at),
                                          CONSTRAINT fk_pay_order_user FOREIGN KEY (user_id) REFERENCES users(id),
                                          CONSTRAINT fk_pay_order_product FOREIGN KEY (product_id) REFERENCES recharge_products(id),
                                          CONSTRAINT fk_pay_order_exchange_rule FOREIGN KEY (exchange_rule_id) REFERENCES recharge_exchange_rules(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单（对接微信扫码支付，成功后发积分）';

-- 支付回调/事件（幂等 + 审计）
CREATE TABLE IF NOT EXISTS pay_events (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付事件ID',
                                          provider VARCHAR(16) NOT NULL COMMENT '支付渠道：WECHAT等',
                                          event_id VARCHAR(128) NOT NULL COMMENT '渠道事件ID/通知ID（用于幂等）',
                                          order_no VARCHAR(64) NOT NULL COMMENT '商户订单号',
                                          provider_trade_no VARCHAR(128) NULL COMMENT '渠道交易号',
                                          event_type VARCHAR(32) NOT NULL COMMENT '事件类型：PAY_SUCCESS/PAY_FAIL/REFUND等',
                                          payload_json JSON NOT NULL COMMENT '回调原始内容（JSON）',
                                          processed TINYINT NOT NULL DEFAULT 0 COMMENT '是否已处理：1是，0否',
                                          processed_at DATETIME(3) NULL COMMENT '处理时间',
                                          created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

                                          UNIQUE KEY uk_provider_event (provider, event_id),
                                          KEY idx_order_no (order_no),
                                          KEY idx_processed (processed, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付回调事件记录（幂等、审计、排障）';



-- pricing_rules 初始化（示例：占位）
INSERT INTO pricing_rules (biz_type, model_code, unit, price, enabled)
VALUES
    ('TEXT',  'gemini-3-pro-preview',        'PER_1K_TOKENS',  1, 1),
    ('IMAGE', 'jimeng-4.5',                  'PER_ITEM',      20, 1),
    ('IMAGE', 'gemini-3-pro-image-preview',  'PER_ITEM',      25, 1),
    ('VIDEO', 'sora-2',                      'PER_SECOND',   200, 1);

-- 1) 兑换规则：示例（你可改成 1元=100积分 等）
INSERT INTO recharge_exchange_rules (name, currency, cents_per_point, min_amount_cents, max_amount_cents, enabled)
VALUES ('默认规则', 'CNY', 10, 100, NULL, 1);

-- 2) 套餐：示例（可选）
INSERT INTO recharge_products (name, points, price_cents, enabled, sort_order)
VALUES
    ('小额套餐', 1000, 990, 1, 10),
    ('标准套餐', 5000, 4590, 1, 20),
    ('大额套餐', 12000, 9990, 1, 30);

-- 3) pricing_rules：示例（占位，按你实际改）
INSERT INTO pricing_rules (biz_type, model_code, unit, price, enabled)
VALUES
    ('TEXT',  'gemini-3-pro-preview',        'PER_1K_TOKENS',  1, 1),
    ('IMAGE', 'jimeng-4.5',                  'PER_ITEM',      20, 1),
    ('IMAGE', 'gemini-3-pro-image-preview',  'PER_ITEM',      25, 1),
    ('VIDEO', 'sora-2',                      'PER_SECOND',   200, 1)
ON DUPLICATE KEY UPDATE
                     unit=VALUES(unit), price=VALUES(price), enabled=VALUES(enabled);

