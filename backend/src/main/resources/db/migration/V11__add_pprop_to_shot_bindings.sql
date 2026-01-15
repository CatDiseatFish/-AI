SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 修改 shot_bindings 表的 bind_type 约束,添加 PPROP 支持
-- =========================

-- 1. 删除现有的 bind_type 约束
ALTER TABLE shot_bindings 
    DROP CONSTRAINT chk_shot_bindings_bind_type;

-- 2. 重新添加约束,包含 PCHAR, PSCENE, PPROP 三种类型
ALTER TABLE shot_bindings 
    ADD CONSTRAINT chk_shot_bindings_bind_type 
    CHECK (bind_type IN ('PCHAR', 'PSCENE', 'PPROP'));

SET FOREIGN_KEY_CHECKS = 1;
