-- 为jobs表添加resultUrl和costPoints字段
ALTER TABLE jobs 
ADD COLUMN result_url VARCHAR(1024) NULL COMMENT '结果URL（生成的图片/视频URL等）' AFTER error_message,
ADD COLUMN cost_points INT NULL COMMENT '消耗积分' AFTER result_url;
