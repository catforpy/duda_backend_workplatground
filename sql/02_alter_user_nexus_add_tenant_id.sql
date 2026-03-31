-- ===================================================================
-- user_nexus 数据库改造脚本 - 增加租户隔离字段
-- ===================================================================
-- 数据库名: user_nexus
-- 改造目标: 为11张核心业务表增加 tenant_id 字段，实现多租户数据隔离
-- 创建时间: 2026-03-27
-- 执行人员: 用户
--
-- 改造说明:
--   1. 为11张表增加 tenant_id 字段（BIGINT, NOT NULL, DEFAULT 1）
--   2. 为部分表增加 version 字段（乐观锁）
--   3. 重建 merchants 表的唯一索引（包含 tenant_id）
--   4. 更新现有数据（tenant_id 设置为 1）
--   5. 创建 idx_tenant_id 索引
--
-- 验收标准:
--   1. 11张表都增加了 tenant_id 字段
--   2. 所有表的 tenant_id 字段都是 NOT NULL DEFAULT 1
--   3. merchants 表的唯一索引已重建为 uk_tenant_merchant_code
--   4. 所有表都创建了 idx_tenant_id 索引
--   5. 现有数据的 tenant_id 都设置为 1
--   6. 数据完整性100%通过
-- ===================================================================

USE `user_nexus`;

-- ===================================================================
-- 优先级 P0: 核心业务表改造（Day 2）
-- ===================================================================

-- ===================================================================
-- 1. 改造 merchants 表（商户表）⭐⭐⭐
-- ===================================================================
-- 说明: 商户基础信息表，需要强租户隔离

-- 1.1 增加字段
ALTER TABLE `merchants`
ADD COLUMN `tenant_id` BIGINT COMMENT '租户ID' AFTER `id`;

-- 1.2 更新现有数据（设置为默认租户）
UPDATE `merchants` SET `tenant_id` = 1 WHERE `tenant_id` IS NULL;

-- 1.3 修改字段为 NOT NULL
ALTER TABLE `merchants`
MODIFY COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID';

-- 1.4 增加 version 字段（如果不存在）
-- 检查字段是否存在，不存在则添加
SET @check_version = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'user_nexus'
    AND TABLE_NAME = 'merchants'
    AND COLUMN_NAME = 'version'
);

SET @sql_version = IF(@check_version = 0,
    'ALTER TABLE `merchants` ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT ''版本号（乐观锁）'' AFTER `update_by`',
    'SELECT ''version column already exists'' AS message'
);

PREPARE stmt FROM @sql_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 1.5 创建索引
ALTER TABLE `merchants`
ADD INDEX `idx_tenant_id` (`tenant_id`);

-- 1.6 重建唯一索引（包含 tenant_id，实现租户隔离）
-- 先删除旧的唯一索引（如果存在）
ALTER TABLE `merchants` DROP INDEX IF EXISTS `uk_merchant_code`;

-- 创建新的复合唯一索引
ALTER TABLE `merchants`
ADD UNIQUE KEY `uk_tenant_merchant_code` (`tenant_id`, `merchant_code`);

SELECT '✅ merchants 表改造完成' AS completion_message;


-- ===================================================================
-- 2. 改造 merchant_users 表（商户用户表）⭐⭐⭐
-- ===================================================================
-- 说明: 商户用户关联表，需要租户隔离

ALTER TABLE `merchant_users`
ADD COLUMN `tenant_id` BIGINT COMMENT '租户ID' AFTER `merchant_id`;

UPDATE `merchant_users` SET `tenant_id` = 1 WHERE `tenant_id` IS NULL;

ALTER TABLE `merchant_users`
MODIFY COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID';

-- 增加 version 字段（如果不存在）
SET @check_version = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'user_nexus'
    AND TABLE_NAME = 'merchant_users'
    AND COLUMN_NAME = 'version'
);

SET @sql_version = IF(@check_version = 0,
    'ALTER TABLE `merchant_users` ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT ''版本号（乐观锁）'' AFTER `deleted`',
    'SELECT ''version column already exists'' AS message'
);

PREPARE stmt FROM @sql_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `merchant_users`
ADD INDEX `idx_tenant_id` (`tenant_id`);

SELECT '✅ merchant_users 表改造完成' AS completion_message;


-- ===================================================================
-- 3. 改造 mini_programs 表（小程序表）⭐⭐⭐
-- ===================================================================
-- 说明: 小程序基础信息表，需要租户隔离

ALTER TABLE `mini_programs`
ADD COLUMN `tenant_id` BIGINT COMMENT '租户ID' AFTER `company_id`;

UPDATE `mini_programs` SET `tenant_id` = 1 WHERE `tenant_id` IS NULL;

ALTER TABLE `mini_programs`
MODIFY COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID';

-- 增加 version 字段（如果不存在）
SET @check_version = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'user_nexus'
    AND TABLE_NAME = 'mini_programs'
    AND COLUMN_NAME = 'version'
);

SET @sql_version = IF(@check_version = 0,
    'ALTER TABLE `mini_programs` ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT ''版本号（乐观锁）'' AFTER `update_time`',
    'SELECT ''version column already exists'' AS message'
);

PREPARE stmt FROM @sql_version;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `mini_programs`
ADD INDEX `idx_tenant_id` (`tenant_id`);

SELECT '✅ mini_programs 表改造完成' AS completion_message;


-- ===================================================================
-- 4. 改造 mini_program_templates 表（小程序模板表）⭐⭐
-- ===================================================================
-- 说明: 小程序模板表，需要租户隔离

ALTER TABLE `mini_program_templates`
ADD COLUMN `tenant_id` BIGINT COMMENT '租户ID' AFTER `id`;

UPDATE `mini_program_templates` SET `tenant_id` = 1 WHERE `tenant_id` IS NULL;

ALTER TABLE `mini_program_templates`
MODIFY COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID';

ALTER TABLE `mini_program_templates`
ADD INDEX `idx_tenant_id` (`tenant_id`);

SELECT '✅ mini_program_templates 表改造完成' AS completion_message;


-- ===================================================================
-- 优先级 P1: 扩展业务表改造（Day 3）
-- ===================================================================

-- ===================================================================
-- 5. 改造 mini_program_categories 表（小程序分类表）⭐⭐
-- ===================================================================
ALTER TABLE `mini_program_categories`
ADD COLUMN `tenant_id` BIGINT COMMENT '租户ID' AFTER `id`;

UPDATE `mini_program_categories` SET `tenant_id` = 1 WHERE `tenant_id` IS NULL;

ALTER TABLE `mini_program_categories`
MODIFY COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID';

ALTER TABLE `mini_program_categories`
ADD INDEX `idx_tenant_id` (`tenant_id`);

SELECT '✅ mini_program_categories 表改造完成' AS completion_message;


-- ===================================================================
-- 6. 改造 mini_program_certification 表（小程序认证表）⭐
-- ===================================================================
ALTER TABLE `mini_program_certification`
ADD COLUMN `tenant_id` BIGINT COMMENT '租户ID' AFTER `mini_program_id`;

UPDATE `mini_program_certification` SET `tenant_id` = 1 WHERE `tenant_id` IS NULL;

ALTER TABLE `mini_program_certification`
MODIFY COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID';

ALTER TABLE `mini_program_certification`
ADD INDEX `idx_tenant_id` (`tenant_id`);

SELECT '✅ mini_program_certification 表改造完成' AS completion_message;


-- ===================================================================
-- 7. 改造 mini_program_development_tasks 表（小程序开发任务表）⭐
-- ===================================================================
ALTER TABLE `mini_program_development_tasks`
ADD COLUMN `tenant_id` BIGINT COMMENT '租户ID' AFTER `mini_program_id`;

UPDATE `mini_program_development_tasks` SET `tenant_id` = 1 WHERE `tenant_id` IS NULL;

ALTER TABLE `mini_program_development_tasks`
MODIFY COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID';

ALTER TABLE `mini_program_development_tasks`
ADD INDEX `idx_tenant_id` (`tenant_id`);

SELECT '✅ mini_program_development_tasks 表改造完成' AS completion_message;


-- ===================================================================
-- 8. 改造 mini_program_filing 表（小程序备案表）⭐
-- ===================================================================
ALTER TABLE `mini_program_filing`
ADD COLUMN `tenant_id` BIGINT COMMENT '租户ID' AFTER `mini_program_id`;

UPDATE `mini_program_filing` SET `tenant_id` = 1 WHERE `tenant_id` IS NULL;

ALTER TABLE `mini_program_filing`
MODIFY COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID';

ALTER TABLE `mini_program_filing`
ADD INDEX `idx_tenant_id` (`tenant_id`);

SELECT '✅ mini_program_filing 表改造完成' AS completion_message;


-- ===================================================================
-- 9. 改造 open_api_keys 表（开放API密钥表）⭐
-- ===================================================================
-- 说明: API密钥表，已有version字段，只需增加tenant_id

ALTER TABLE `open_api_keys`
ADD COLUMN `tenant_id` BIGINT COMMENT '租户ID' AFTER `app_owner_id`;

UPDATE `open_api_keys` SET `tenant_id` = 1 WHERE `tenant_id` IS NULL;

ALTER TABLE `open_api_keys`
MODIFY COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID';

ALTER TABLE `open_api_keys`
ADD INDEX `idx_tenant_id` (`tenant_id`);

SELECT '✅ open_api_keys 表改造完成' AS completion_message;


-- ===================================================================
-- 优先级 P2: 公司相关表改造（Day 3，时间允许）
-- ===================================================================

-- ===================================================================
-- 10. 改造 company_qualifications 表（公司资质表）
-- ===================================================================
ALTER TABLE `company_qualifications`
ADD COLUMN `tenant_id` BIGINT COMMENT '租户ID' AFTER `company_id`;

UPDATE `company_qualifications` SET `tenant_id` = 1 WHERE `tenant_id` IS NULL;

ALTER TABLE `company_qualifications`
MODIFY COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID';

ALTER TABLE `company_qualifications`
ADD INDEX `idx_tenant_id` (`tenant_id`);

SELECT '✅ company_qualifications 表改造完成' AS completion_message;


-- ===================================================================
-- 11. 改造 company_service_providers 表（公司服务商表）
-- ===================================================================
ALTER TABLE `company_service_providers`
ADD COLUMN `tenant_id` BIGINT COMMENT '租户ID' AFTER `company_id`;

UPDATE `company_service_providers` SET `tenant_id` = 1 WHERE `tenant_id` IS NULL;

ALTER TABLE `company_service_providers`
MODIFY COLUMN `tenant_id` BIGINT NOT NULL DEFAULT 1 COMMENT '租户ID';

ALTER TABLE `company_service_providers`
ADD INDEX `idx_tenant_id` (`tenant_id`);

SELECT '✅ company_service_providers 表改造完成' AS completion_message;


-- ===================================================================
-- 验证数据完整性
-- ===================================================================
SELECT '=== user_nexus 数据库改造验证报告 ===' AS verification_title;

SELECT
    'merchants' AS table_name,
    COUNT(*) AS total_rows,
    COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) AS with_tenant_id,
    COUNT(CASE WHEN tenant_id = 1 THEN 1 END) AS with_default_tenant,
    (COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) = COUNT(*)) AS data_complete
FROM merchants
UNION ALL
SELECT
    'merchant_users' AS table_name,
    COUNT(*) AS total_rows,
    COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) AS with_tenant_id,
    COUNT(CASE WHEN tenant_id = 1 THEN 1 END) AS with_default_tenant,
    (COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) = COUNT(*)) AS data_complete
FROM merchant_users
UNION ALL
SELECT
    'mini_programs' AS table_name,
    COUNT(*) AS total_rows,
    COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) AS with_tenant_id,
    COUNT(CASE WHEN tenant_id = 1 THEN 1 END) AS with_default_tenant,
    (COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) = COUNT(*)) AS data_complete
FROM mini_programs
UNION ALL
SELECT
    'mini_program_templates' AS table_name,
    COUNT(*) AS total_rows,
    COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) AS with_tenant_id,
    COUNT(CASE WHEN tenant_id = 1 THEN 1 END) AS with_default_tenant,
    (COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) = COUNT(*)) AS data_complete
FROM mini_program_templates
UNION ALL
SELECT
    'mini_program_categories' AS table_name,
    COUNT(*) AS total_rows,
    COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) AS with_tenant_id,
    COUNT(CASE WHEN tenant_id = 1 THEN 1 END) AS with_default_tenant,
    (COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) = COUNT(*)) AS data_complete
FROM mini_program_categories
UNION ALL
SELECT
    'open_api_keys' AS table_name,
    COUNT(*) AS total_rows,
    COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) AS with_tenant_id,
    COUNT(CASE WHEN tenant_id = 1 THEN 1 END) AS with_default_tenant,
    (COUNT(CASE WHEN tenant_id IS NOT NULL THEN 1 END) = COUNT(*)) AS data_complete
FROM open_api_keys;


-- ===================================================================
-- 验证索引创建
-- ===================================================================
SELECT '=== 索引验证 ===' AS index_verification;

SELECT
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'user_nexus'
AND TABLE_NAME IN ('merchants', 'merchant_users', 'mini_programs')
AND INDEX_NAME LIKE 'idx_tenant_id%'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;


-- ===================================================================
-- 验证 merchants 表的唯一索引重建
-- ===================================================================
SELECT '=== merchants 表唯一索引验证 ===' AS unique_index_verification;

SELECT
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'user_nexus'
AND TABLE_NAME = 'merchants'
AND INDEX_NAME = 'uk_tenant_merchant_code'
GROUP BY INDEX_NAME;


-- ===================================================================
-- 查看表结构示例
-- ===================================================================
SELECT '=== merchants 表结构 ===' AS table_structure;
DESC merchants;


-- ===================================================================
-- 脚本执行完成
-- ===================================================================
SELECT '✅ user_nexus 数据库改造完成！' AS completion_message;
