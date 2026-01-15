SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 修复外键约束，添加级联删除策略
-- =========================

-- 1. 删除现有外键约束
ALTER TABLE user_auth DROP FOREIGN KEY fk_user_auth_user;
ALTER TABLE wallets DROP FOREIGN KEY fk_wallet_user;
ALTER TABLE wallet_transactions DROP FOREIGN KEY fk_tx_user;
ALTER TABLE project_folders DROP FOREIGN KEY fk_folder_user;
ALTER TABLE projects DROP FOREIGN KEY fk_project_user;
ALTER TABLE projects DROP FOREIGN KEY fk_project_folder;
ALTER TABLE character_categories DROP FOREIGN KEY fk_char_cat_user;
ALTER TABLE character_library DROP FOREIGN KEY fk_char_lib_user;
ALTER TABLE character_library DROP FOREIGN KEY fk_char_lib_cat;
ALTER TABLE project_characters DROP FOREIGN KEY fk_proj_char_project;
ALTER TABLE project_characters DROP FOREIGN KEY fk_proj_char_lib;
ALTER TABLE scene_categories DROP FOREIGN KEY fk_scene_cat_user;
ALTER TABLE scene_library DROP FOREIGN KEY fk_scene_lib_user;
ALTER TABLE scene_library DROP FOREIGN KEY fk_scene_lib_cat;
ALTER TABLE project_scenes DROP FOREIGN KEY fk_proj_scene_project;
ALTER TABLE project_scenes DROP FOREIGN KEY fk_proj_scene_lib;
ALTER TABLE storyboard_shots DROP FOREIGN KEY fk_shot_project;
ALTER TABLE shot_bindings DROP FOREIGN KEY fk_binding_shot;
ALTER TABLE assets DROP FOREIGN KEY fk_asset_project;
ALTER TABLE asset_versions DROP FOREIGN KEY fk_version_asset;
ALTER TABLE asset_refs DROP FOREIGN KEY fk_ref_version;
ALTER TABLE job_items DROP FOREIGN KEY fk_item_job;
ALTER TABLE prompt_templates DROP FOREIGN KEY fk_prompt_template_user;
ALTER TABLE invite_codes DROP FOREIGN KEY fk_invite_code_user;
ALTER TABLE invite_records DROP FOREIGN KEY fk_invite_record_code;
ALTER TABLE invite_records DROP FOREIGN KEY fk_invite_record_inviter;
ALTER TABLE invite_records DROP FOREIGN KEY fk_invite_record_invitee;
ALTER TABLE pay_orders DROP FOREIGN KEY fk_pay_order_user;
ALTER TABLE pay_orders DROP FOREIGN KEY fk_pay_order_product;
ALTER TABLE pay_orders DROP FOREIGN KEY fk_pay_order_exchange_rule;

-- 2. 重新添加外键约束，带级联策略

-- 用户相关表：用户删除时，认证信息、钱包、流水都级联删除
ALTER TABLE user_auth 
    ADD CONSTRAINT fk_user_auth_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE wallets 
    ADD CONSTRAINT fk_wallet_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE wallet_transactions 
    ADD CONSTRAINT fk_tx_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 项目文件夹：用户删除时级联删除
ALTER TABLE project_folders 
    ADD CONSTRAINT fk_folder_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 项目：用户删除时级联删除，文件夹删除时设为NULL
ALTER TABLE projects 
    ADD CONSTRAINT fk_project_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE projects 
    ADD CONSTRAINT fk_project_folder 
    FOREIGN KEY (folder_id) REFERENCES project_folders(id) 
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 角色分类：用户删除时级联删除
ALTER TABLE character_categories 
    ADD CONSTRAINT fk_char_cat_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 角色库：用户删除时级联删除，分类删除时设为NULL
ALTER TABLE character_library 
    ADD CONSTRAINT fk_char_lib_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE character_library 
    ADD CONSTRAINT fk_char_lib_cat 
    FOREIGN KEY (category_id) REFERENCES character_categories(id) 
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 项目角色：项目删除时级联删除，角色库删除时级联删除
ALTER TABLE project_characters 
    ADD CONSTRAINT fk_proj_char_project 
    FOREIGN KEY (project_id) REFERENCES projects(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE project_characters 
    ADD CONSTRAINT fk_proj_char_lib 
    FOREIGN KEY (library_character_id) REFERENCES character_library(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 场景分类：用户删除时级联删除
ALTER TABLE scene_categories 
    ADD CONSTRAINT fk_scene_cat_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 场景库：用户删除时级联删除，分类删除时设为NULL
ALTER TABLE scene_library 
    ADD CONSTRAINT fk_scene_lib_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE scene_library 
    ADD CONSTRAINT fk_scene_lib_cat 
    FOREIGN KEY (category_id) REFERENCES scene_categories(id) 
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 项目场景：项目删除时级联删除，场景库删除时级联删除
ALTER TABLE project_scenes 
    ADD CONSTRAINT fk_proj_scene_project 
    FOREIGN KEY (project_id) REFERENCES projects(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE project_scenes 
    ADD CONSTRAINT fk_proj_scene_lib 
    FOREIGN KEY (library_scene_id) REFERENCES scene_library(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 分镜：项目删除时级联删除
ALTER TABLE storyboard_shots 
    ADD CONSTRAINT fk_shot_project 
    FOREIGN KEY (project_id) REFERENCES projects(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 分镜绑定：分镜删除时级联删除
ALTER TABLE shot_bindings 
    ADD CONSTRAINT fk_binding_shot 
    FOREIGN KEY (shot_id) REFERENCES storyboard_shots(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 资产：项目删除时级联删除
ALTER TABLE assets 
    ADD CONSTRAINT fk_asset_project 
    FOREIGN KEY (project_id) REFERENCES projects(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 资产版本：资产删除时级联删除
ALTER TABLE asset_versions 
    ADD CONSTRAINT fk_version_asset 
    FOREIGN KEY (asset_id) REFERENCES assets(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 资产引用：版本删除时级联删除
ALTER TABLE asset_refs 
    ADD CONSTRAINT fk_ref_version 
    FOREIGN KEY (asset_version_id) REFERENCES asset_versions(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 新增：资产引用的项目外键约束
ALTER TABLE asset_refs 
    ADD CONSTRAINT fk_ref_project 
    FOREIGN KEY (project_id) REFERENCES projects(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 任务子项：任务删除时级联删除
ALTER TABLE job_items 
    ADD CONSTRAINT fk_item_job 
    FOREIGN KEY (job_id) REFERENCES jobs(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 提示词模板：用户删除时级联删除
ALTER TABLE prompt_templates 
    ADD CONSTRAINT fk_prompt_template_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 邀请码：用户删除时级联删除
ALTER TABLE invite_codes 
    ADD CONSTRAINT fk_invite_code_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 邀请记录：邀请码删除时级联删除
ALTER TABLE invite_records 
    ADD CONSTRAINT fk_invite_record_code 
    FOREIGN KEY (invite_code_id) REFERENCES invite_codes(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE invite_records 
    ADD CONSTRAINT fk_invite_record_inviter 
    FOREIGN KEY (inviter_user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE invite_records 
    ADD CONSTRAINT fk_invite_record_invitee 
    FOREIGN KEY (invitee_user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 支付订单：用户删除时级联删除
ALTER TABLE pay_orders 
    ADD CONSTRAINT fk_pay_order_user 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE ON UPDATE CASCADE;

-- 支付订单：商品删除时设为NULL
ALTER TABLE pay_orders 
    ADD CONSTRAINT fk_pay_order_product 
    FOREIGN KEY (product_id) REFERENCES recharge_products(id) 
    ON DELETE SET NULL ON UPDATE CASCADE;

-- 支付订单：兑换规则删除时设为NULL
ALTER TABLE pay_orders 
    ADD CONSTRAINT fk_pay_order_exchange_rule 
    FOREIGN KEY (exchange_rule_id) REFERENCES recharge_exchange_rules(id) 
    ON DELETE SET NULL ON UPDATE CASCADE;

-- =========================
-- 3. 为多态关联字段添加CHECK约束（MySQL 8.0.16+）
-- =========================

-- assets表的owner_type约束
ALTER TABLE assets 
    ADD CONSTRAINT chk_assets_owner_type 
    CHECK (owner_type IN ('LIB_CHAR', 'PCHAR', 'LIB_SCENE', 'PSCENE', 'SHOT', 'PROJECT'));

-- asset_refs表的ref_type约束
ALTER TABLE asset_refs 
    ADD CONSTRAINT chk_asset_refs_ref_type 
    CHECK (ref_type IN ('LIB_CHAR_CURRENT', 'PCHAR_CURRENT', 'LIB_SCENE_CURRENT', 'PSCENE_CURRENT', 'SHOT_IMG_CURRENT', 'SHOT_VIDEO_CURRENT'));

-- shot_bindings表的bind_type约束
ALTER TABLE shot_bindings 
    ADD CONSTRAINT chk_shot_bindings_bind_type 
    CHECK (bind_type IN ('PCHAR', 'PSCENE'));

-- job_items表的target_type约束
ALTER TABLE job_items 
    ADD CONSTRAINT chk_job_items_target_type 
    CHECK (target_type IN ('LIB_CHAR', 'PCHAR', 'LIB_SCENE', 'PSCENE', 'SHOT', 'PROJECT'));

SET FOREIGN_KEY_CHECKS = 1;
