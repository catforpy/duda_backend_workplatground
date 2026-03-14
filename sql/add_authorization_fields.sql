-- =============================================
-- 添加授权相关字段到 bucket_statistics 表
-- =============================================

-- 1. 添加Bucket Policy授权配置
ALTER TABLE bucket_statistics
ADD COLUMN `bucket_policy` TEXT COMMENT 'Bucket Policy授权策略（JSON格式）' AFTER `acl_type`;

-- 2. 添加CORS配置详细信息
ALTER TABLE bucket_statistics
ADD COLUMN `cors_config` TEXT COMMENT 'CORS跨域配置（JSON格式）' AFTER `cors_enabled`;

-- 3. �添加防盗链配置
ALTER TABLE bucket_statistics
ADD COLUMN `referer_config` TEXT COMMENT '防盗链白名单配置（JSON格式）' AFTER `cors_config`,
ADD COLUMN `referer_enabled` TINYINT DEFAULT 0 COMMENT '是否启用防盗链：0-否 1-是' AFTER `referer_config`;

-- 4. 添加Lifecycle生命周期配置
ALTER TABLE bucket_statistics
ADD COLUMN `lifecycle_config` TEXT COMMENT '生命周期规则配置（JSON格式）' AFTER `referer_enabled`;

-- 5. 添加WORM（保留策略）配置
ALTER TABLE bucket_statistics
ADD COLUMN `worm_enabled` TINYINT DEFAULT 0 COMMENT '是否开启WORM保留策略：0-否 1-是' AFTER `lifecycle_config`;

-- 6. 添加日志记录配置
ALTER TABLE bucket_statistics
ADD COLUMN `logging_config` TEXT COMMENT '日志记录配置（JSON格式）' AFTER `worm_enabled`;

-- 7. 添加Website托管配置
ALTER TABLE bucket_statistics
ADD COLUMN `website_config` TEXT COMMENT '静态网站托管配置（JSON格式）' AFTER `logging_config`,
ADD COLUMN `website_enabled` TINYINT DEFAULT 0 COMMENT '是否启用网站托管：0-否 1-是' AFTER `website_config`;

-- 8. 添加跨区域复制配置
ALTER TABLE bucket_statistics
ADD COLUMN `replication_config` TEXT COMMENT '跨区域复制配置（JSON格式）' AFTER `website_enabled`;

-- 9. 添加传输加速配置
ALTER TABLE bucket_statistics
ADD COLUMN `transfer_acceleration_enabled` TINYINT DEFAULT 0 COMMENT '是否启用传输加速：0-否 1-是' AFTER `replication_config`;

-- 10. 添加访问监控配置
ALTER TABLE bucket_statistics
ADD COLUMN `access_monitor_enabled` TINYINT DEFAULT 0 COMMENT '是否启用访问监控：0-否 1-是' AFTER `transfer_acceleration_enabled`;

-- =============================================
-- 验证添加的字段
-- =============================================
DESC bucket_statistics;

-- =============================================
-- 使用示例
-- =============================================
-- 1. 更新ACL授权
-- UPDATE bucket_statistics
-- SET acl_type = 'private',
--     bucket_policy = '{"Version":"1","Statement":[{"Effect":"Allow","Principal":["123456789"],"Action":["oss:GetObject"],"Resource":["acs:oss:*:*:duda-java-backend-test/*"]}]}'
-- WHERE bucket_name = 'duda-java-backend-test';
--
-- 2. 更新防盗链配置
-- UPDATE bucket_statistics
-- SET referer_enabled = 1,
--     referer_config = '{"allowEmpty":false,"refererList":["https://example.com","https://www.example.com"]}'
-- WHERE bucket_name = 'duda-java-backend-test';
--
-- 3. 更新CORS配置
-- UPDATE bucket_statistics
-- SET cors_enabled = 1,
--     cors_config = '{"allowedOrigins":["*"],"allowedMethods":["GET","POST"],"allowedHeaders":["*"],"exposeHeaders":["ETag"],"maxAgeSeconds":3600}'
-- WHERE bucket_name = 'duda-java-backend-test';
-- =============================================
