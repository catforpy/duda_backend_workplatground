-- =============================================
-- bucket_config表增加新字段
-- 用途：支持OSS高级功能配置（使用JSON存储配置）
-- =============================================

USE duda_file;

-- 1. 生命周期配置
ALTER TABLE bucket_config ADD COLUMN `lifecycle_config` JSON COMMENT '生命周期规则配置（JSON）' AFTER `lifecycle_enabled`;

-- 2. CORS配置
ALTER TABLE bucket_config ADD COLUMN `cors_config` JSON COMMENT 'CORS规则配置（JSON）' AFTER `cors_enabled`;

-- 3. 防盗链配置
ALTER TABLE bucket_config ADD COLUMN `referer_config` JSON COMMENT '防盗链配置（JSON）' AFTER `cors_enabled`;

-- 4. Bucket Policy配置
ALTER TABLE bucket_config ADD COLUMN `bucket_policy` JSON COMMENT 'Bucket策略文档（JSON）' AFTER `tags`;

-- 5. 跨区域复制配置
ALTER TABLE bucket_config ADD COLUMN `replication_config` JSON COMMENT '跨区域复制配置（JSON）' AFTER `bucket_policy`;

-- 6. 访问跟踪配置
ALTER TABLE bucket_config ADD COLUMN `access_monitor_config` JSON COMMENT '访问跟踪配置（JSON）' AFTER `description`;

-- 7. 存储空间清单配置
ALTER TABLE bucket_config ADD COLUMN `inventory_config` JSON COMMENT '存储空间清单配置（JSON）' AFTER `access_monitor_config`;

-- 8. 静态网站托管配置
ALTER TABLE bucket_config ADD COLUMN `website_config` JSON COMMENT '静态网站托管配置（JSON）' AFTER `inventory_config`;

-- 9. 日志转存配置
ALTER TABLE bucket_config ADD COLUMN `logging_config` JSON COMMENT '日志转存配置（JSON）' AFTER `website_config`;

-- 10. 合规保留策略配置
ALTER TABLE bucket_config ADD COLUMN `worm_config` JSON COMMENT '合规保留策略配置（JSON）' AFTER `logging_config`;

-- 11. 传输加速配置
ALTER TABLE bucket_config ADD COLUMN `transfer_acceleration_enabled` TINYINT DEFAULT 0 COMMENT '是否开启传输加速：0-否 1-是' AFTER `worm_config`;

-- 验证新增字段
DESC bucket_config;
