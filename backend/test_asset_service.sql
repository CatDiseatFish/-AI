-- ============================================
-- AssetService测试数据 - 完整的数据依赖链
-- ============================================
-- 数据依赖关系:
-- users -> projects -> assets -> asset_versions
--                           └─> asset_refs
-- ============================================

-- 1. 创建测试用户
INSERT INTO users (id, nickname, avatar_url, status, created_at, updated_at)
VALUES (1, '测试用户', 'https://example.com/avatar.jpg', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE nickname = '测试用户';

-- 2. 创建测试项目(属于用户1)
INSERT INTO projects (id, user_id, folder_id, name, aspect_ratio, style_code, era_setting, raw_text, status, model_config_json, created_at, updated_at, deleted_at)
VALUES (1, 1, NULL, '测试项目-资产版本管理', '16:9', 'cinematic', '现代', '这是一个测试项目的文本', 'DRAFT', '{}', NOW(), NOW(), NULL)
ON DUPLICATE KEY UPDATE name = '测试项目-资产版本管理', deleted_at = NULL;

-- 3. 创建测试资产(属于项目1)
-- 场景1: 一个角色库角色的资产
INSERT INTO assets (id, project_id, asset_type, owner_type, owner_id, created_at)
VALUES (1, 1, 'CHAR_IMG', 'LIB_CHAR', 101, NOW())
ON DUPLICATE KEY UPDATE project_id = 1;

-- 场景2: 一个场景的资产
INSERT INTO assets (id, project_id, asset_type, owner_type, owner_id, created_at)
VALUES (2, 1, 'SCENE_IMG', 'SCENE', 201, NOW())
ON DUPLICATE KEY UPDATE project_id = 1;

-- 4. 为资产1创建多个版本(用于测试版本历史)
INSERT INTO asset_versions (id, asset_id, version_no, source, provider, url, object_key, prompt, params_json, status, created_by, created_at)
VALUES
  -- 第1版: AI生成
  (1, 1, 1, 'AI', 'OSS', 'https://oss.example.com/assets/char_v1.jpg', 'assets/char_v1.jpg', '一个帅气的男主角', '{"model":"jimeng-4.5","seed":123}', 'READY', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY)),

  -- 第2版: AI生成(改进提示词)
  (2, 1, 2, 'AI', 'OSS', 'https://oss.example.com/assets/char_v2.jpg', 'assets/char_v2.jpg', '一个帅气的男主角,现代风格,商务装', '{"model":"jimeng-4.5","seed":456}', 'READY', NULL, DATE_SUB(NOW(), INTERVAL 1 DAY)),

  -- 第3版: 用户上传的本地图片
  (3, 1, 3, 'UPLOAD', 'OSS', 'https://oss.example.com/assets/char_v3.jpg', 'assets/char_v3.jpg', NULL, NULL, 'READY', 1, NOW())
ON DUPLICATE KEY UPDATE asset_id = VALUES(asset_id);

-- 5. 为资产2创建版本
INSERT INTO asset_versions (id, asset_id, version_no, source, provider, url, object_key, prompt, params_json, status, created_by, created_at)
VALUES
  (4, 2, 1, 'AI', 'OSS', 'https://oss.example.com/assets/scene_v1.jpg', 'assets/scene_v1.jpg', '现代办公室场景', '{"model":"jimeng-4.5"}', 'READY', NULL, NOW())
ON DUPLICATE KEY UPDATE asset_id = VALUES(asset_id);

-- 6. 设置当前版本引用(asset_refs表)
-- 资产1当前使用第3版(最新上传的版本)
INSERT INTO asset_refs (id, project_id, ref_type, ref_owner_id, asset_version_id, updated_at)
VALUES (1, 1, 'LIB_CHAR_CURRENT', 101, 3, NOW())
ON DUPLICATE KEY UPDATE asset_version_id = 3, updated_at = NOW();

-- 资产2当前使用第1版
INSERT INTO asset_refs (id, project_id, ref_type, ref_owner_id, asset_version_id, updated_at)
VALUES (2, 1, 'SCENE_CURRENT', 201, 4, NOW())
ON DUPLICATE KEY UPDATE asset_version_id = 4, updated_at = NOW();

-- ============================================
-- 数据验证查询
-- ============================================

-- 验证用户
SELECT id, nickname, status FROM users WHERE id = 1;

-- 验证项目(确保deleted_at为NULL)
SELECT id, user_id, name, deleted_at FROM projects WHERE id = 1;

-- 验证资产及其关联项目
SELECT
    a.id AS asset_id,
    a.asset_type,
    a.owner_type,
    a.owner_id,
    p.id AS project_id,
    p.name AS project_name,
    p.user_id,
    p.deleted_at
FROM assets a
LEFT JOIN projects p ON a.project_id = p.id
WHERE a.id IN (1, 2);

-- 验证资产版本
SELECT
    av.id,
    av.asset_id,
    av.version_no,
    av.source,
    av.url,
    av.status,
    av.created_by,
    av.created_at
FROM asset_versions av
WHERE av.asset_id IN (1, 2)
ORDER BY av.asset_id, av.version_no DESC;

-- 验证当前版本引用
SELECT
    ar.id,
    ar.project_id,
    ar.ref_type,
    ar.ref_owner_id,
    ar.asset_version_id,
    av.version_no AS current_version_no
FROM asset_refs ar
LEFT JOIN asset_versions av ON ar.asset_version_id = av.id
WHERE ar.project_id = 1;

-- 完整的资产版本查询(模拟API返回)
SELECT
    av.id,
    av.asset_id,
    av.version_no,
    av.source,
    av.provider,
    av.url,
    av.object_key,
    av.prompt,
    av.params_json,
    av.status,
    av.created_by,
    av.created_at,
    CASE WHEN ar.asset_version_id = av.id THEN TRUE ELSE FALSE END AS is_current
FROM asset_versions av
LEFT JOIN assets a ON av.asset_id = a.id
LEFT JOIN asset_refs ar ON ar.project_id = a.project_id
    AND ar.ref_type = CONCAT(a.owner_type, '_CURRENT')
    AND ar.ref_owner_id = a.owner_id
WHERE av.asset_id = 1
ORDER BY av.version_no DESC;

-- ============================================
-- 测试场景说明
-- ============================================
/*
测试场景1: 获取资产版本历史
curl -X GET http://localhost:8080/api/assets/1/versions \
  -H "Authorization: Bearer {your_jwt_token}"

预期结果: 返回3个版本,第3版标记为is_current=true

测试场景2: 上传新图片
curl -X POST http://localhost:8080/api/assets/1/versions/upload \
  -H "Authorization: Bearer {your_jwt_token}" \
  -F "file=@/path/to/your/image.jpg"

预期结果: 创建第4版,version_no=4

测试场景3: 切换到历史版本
curl -X PUT http://localhost:8080/api/assets/1/current \
  -H "Authorization: Bearer {your_jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{"versionId": 2}'

预期结果: asset_refs表中的asset_version_id从3改为2

测试场景4: 再次查询版本历史
curl -X GET http://localhost:8080/api/assets/1/versions \
  -H "Authorization: Bearer {your_jwt_token}"

预期结果: 返回的列表中,第2版标记为is_current=true
*/
