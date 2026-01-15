-- 为角色库表添加缩略图字段
ALTER TABLE character_library ADD COLUMN thumbnail_url VARCHAR(512) NULL COMMENT '角色缩略图URL';

-- 为场景库表添加缩略图字段（保持一致性）
ALTER TABLE scene_library ADD COLUMN thumbnail_url VARCHAR(512) NULL COMMENT '场景缩略图URL';
