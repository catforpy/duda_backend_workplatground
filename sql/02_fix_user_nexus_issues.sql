-- ===================================================================
-- user_nexus 数据库优化补充脚本
-- ===================================================================
-- 说明: 基于专业分析后的调整，解决3个关键问题
-- 创建时间: 2026-03-27
-- 执行人员: 用户
--
-- 解决的问题:
--   问题1: mini_program_categories 和 mini_program_templates 不需要租户隔离（全局共享）
--   问题2: 关联表的 tenant_id 一致性检查和修复
--   问题3: 删除冗余索引 idx_tenant_id
--
-- 执行顺序: 必须在 02_alter_user_nexus_add_tenant_id.sql 之后执行
-- ===================================================================

USE `user_nexus`;

-- ===================================================================
-- 问题1: 回滚不需要 tenant_id 的全局共享表
-- ===================================================================

SELECT '=== 问题1: 回滚全局共享表的 tenant_id ===' AS problem_title;

-- 1.1 回滚 mini_program_categories 表
SELECT '正在回滚 mini_program_categories 表...' AS rollback_info;

-- 检查是否存在 tenant_id 字段
SET @check_column = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'user_nexus'
    AND TABLE_NAME = 'mini_program_categories'
    AND COLUMN_NAME = 'tenant_id'
);

-- 如果存在 tenant_id，则删除
SET @sql_drop = IF(@check_column > 0,
    'ALTER TABLE `mini_program_categories` DROP COLUMN `tenant_id`',
    'SELECT ''mini_program_categories 没有 tenant_id 字段，跳过'' AS message'
);

PREPARE stmt FROM @sql_drop;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 删除相关索引（如果存在）
ALTER TABLE `mini_program_categories` DROP INDEX IF EXISTS `idx_tenant_id`;

SELECT '✅ mini_program_categories 表回滚完成（保持全局共享）' AS completion_message;


-- 1.2 回滚 mini_program_templates 表
SELECT '正在回滚 mini_program_templates 表...' AS rollback_info;

-- 检查是否存在 tenant_id 字段
SET @check_column = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'user_nexus'
    AND TABLE_NAME = 'mini_program_templates'
    AND COLUMN_NAME = 'tenant_id'
);

-- 如果存在 tenant_id，则删除
SET @sql_drop = IF(@check_column > 0,
    'ALTER TABLE `mini_program_templates` DROP COLUMN `tenant_id`',
    'SELECT ''mini_program_templates 没有 tenant_id 字段，跳过'' AS message'
);

PREPARE stmt FROM @sql_drop;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 删除相关索引（如果存在）
ALTER TABLE `mini_program_templates` DROP INDEX IF EXISTS `idx_tenant_id`;

SELECT '✅ mini_program_templates 表回滚完成（保持全局共享）' AS completion_message;


-- ===================================================================
-- 问题2: 关联表的 tenant_id 一致性检查和修复
-- ===================================================================

SELECT '=== 问题2: 数据一致性检查 ===' AS problem_title;

-- 2.1 检查 mini_program_certification 表的一致性
SELECT '正在检查 mini_program_certification 表的数据一致性...' AS check_info;

SELECT
    'mini_program_certification' AS table_name,
    COUNT(*) AS total_records,
    SUM(CASE WHEN c.tenant_id != p.tenant_id THEN 1 ELSE 0 END) AS inconsistent_count,
    SUM(CASE WHEN c.tenant_id = p.tenant_id THEN 1 ELSE 0 END) AS consistent_count
FROM mini_program_certification c
LEFT JOIN mini_programs p ON c.mini_program_id = p.id;

-- 如果发现不一致的数据，显示详细信息
SELECT '=== mini_program_certification 不一致数据详情 ===' AS detail_title;

SELECT
    c.id AS certification_id,
    c.mini_program_id,
    c.tenant_id AS certification_tenant_id,
    p.tenant_id AS program_tenant_id,
    c.certification_status,
    CASE
        WHEN c.tenant_id != p.tenant_id THEN '❌ 不一致'
        ELSE '✅ 一致'
    END AS consistency_status
FROM mini_program_certification c
LEFT JOIN mini_programs p ON c.mini_program_id = p.id
WHERE c.tenant_id != p.tenant_id
LIMIT 10;


-- 2.2 检查 mini_program_development_tasks 表的一致性
SELECT '正在检查 mini_program_development_tasks 表的数据一致性...' AS check_info;

SELECT
    'mini_program_development_tasks' AS table_name,
    COUNT(*) AS total_records,
    SUM(CASE WHEN t.tenant_id != p.tenant_id THEN 1 ELSE 0 END) AS inconsistent_count,
    SUM(CASE WHEN t.tenant_id = p.tenant_id THEN 1 ELSE 0 END) AS consistent_count
FROM mini_program_development_tasks t
LEFT JOIN mini_programs p ON t.mini_program_id = p.id;

-- 如果发现不一致的数据，显示详细信息
SELECT '=== mini_program_development_tasks 不一致数据详情 ===' AS detail_title;

SELECT
    t.id AS task_id,
    t.task_no,
    t.mini_program_id,
    t.tenant_id AS task_tenant_id,
    p.tenant_id AS program_tenant_id,
    t.task_name,
    t.task_status,
    CASE
        WHEN t.tenant_id != p.tenant_id THEN '❌ 不一致'
        ELSE '✅ 一致'
    END AS consistency_status
FROM mini_program_development_tasks t
LEFT JOIN mini_programs p ON t.mini_program_id = p.id
WHERE t.tenant_id != p.tenant_id
LIMIT 10;


-- 2.3 检查 mini_program_filing 表的一致性
SELECT '正在检查 mini_program_filing 表的数据一致性...' AS check_info;

SELECT
    'mini_program_filing' AS table_name,
    COUNT(*) AS total_records,
    SUM(CASE WHEN f.tenant_id != p.tenant_id THEN 1 ELSE 0 END) AS inconsistent_count,
    SUM(CASE WHEN f.tenant_id = p.tenant_id THEN 1 ELSE 0 END) AS consistent_count
FROM mini_program_filing f
LEFT JOIN mini_programs p ON f.mini_program_id = p.id;


-- 2.4 修复不一致的数据（如果发现）
SELECT '=== 开始修复不一致的数据 ===' AS fix_title;

-- 修复 mini_program_certification 表
UPDATE mini_program_certification c
INNER JOIN mini_programs p ON c.mini_program_id = p.id
SET c.tenant_id = p.tenant_id
WHERE c.tenant_id != p.tenant_id;

SELECT
    ROW_COUNT() AS fixed_count,
    'mini_program_certification' AS table_name
AS fix_result;


-- 修复 mini_program_development_tasks 表
UPDATE mini_program_development_tasks t
INNER JOIN mini_programs p ON t.mini_program_id = p.id
SET t.tenant_id = p.tenant_id
WHERE t.tenant_id != p.tenant_id;

SELECT
    ROW_COUNT() AS fixed_count,
    'mini_program_development_tasks' AS table_name
AS fix_result;


-- 修复 mini_program_filing 表
UPDATE mini_program_filing f
INNER JOIN mini_programs p ON f.mini_program_id = p.id
SET f.tenant_id = p.tenant_id
WHERE f.tenant_id != p.tenant_id;

SELECT
    ROW_COUNT() AS fixed_count,
    'mini_program_filing' AS table_name
AS fix_result;


-- 2.5 再次验证修复后的数据一致性
SELECT '=== 修复后数据一致性验证 ===' AS verify_title;

SELECT
    'mini_program_certification' AS table_name,
    COUNT(*) AS total_records,
    SUM(CASE WHEN c.tenant_id != p.tenant_id THEN 1 ELSE 0 END) AS inconsistent_count
FROM mini_program_certification c
LEFT JOIN mini_programs p ON c.mini_program_id = p.id
UNION ALL
SELECT
    'mini_program_development_tasks' AS table_name,
    COUNT(*) AS total_records,
    SUM(CASE WHEN t.tenant_id != p.tenant_id THEN 1 ELSE 0 END) AS inconsistent_count
FROM mini_program_development_tasks t
LEFT JOIN mini_programs p ON t.mini_program_id = p.id
UNION ALL
SELECT
    'mini_program_filing' AS table_name,
    COUNT(*) AS total_records,
    SUM(CASE WHEN f.tenant_id != p.tenant_id THEN 1 ELSE 0 END) AS inconsistent_count
FROM mini_program_filing f
LEFT JOIN mini_programs p ON f.mini_program_id = p.id;

SELECT '✅ 数据一致性修复完成' AS completion_message;


-- ===================================================================
-- 问题3: 删除冗余索引 idx_tenant_id
-- ===================================================================

SELECT '=== 问题3: 删除冗余索引 ===' AS problem_title;

-- 3.1 删除 merchants 表的冗余索引
SELECT '正在删除 merchants 表的冗余索引 idx_tenant_id...' AS drop_info;

SET @check_index = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'user_nexus'
    AND TABLE_NAME = 'merchants'
    AND INDEX_NAME = 'idx_tenant_id'
);

SET @sql_drop = IF(@check_index > 0,
    'ALTER TABLE `merchants` DROP INDEX `idx_tenant_id`',
    'SELECT ''idx_tenant_id 索引不存在或已删除'' AS message'
);

PREPARE stmt FROM @sql_drop;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '✅ merchants 表的冗余索引已删除' AS completion_message;


-- 3.2 删除 merchant_users 表的冗余索引（如果存在唯一索引包含tenant_id）
-- 检查是否有复合唯一索引包含 tenant_id
SET @has_unique_index = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'user_nexus'
    AND TABLE_NAME = 'merchant_users'
    AND INDEX_NAME = 'uk_tenant_merchant_user'
    AND NON_UNIQUE = 0
);

-- 如果没有复合唯一索引，保留单列索引；如果有复合唯一索引，删除单列索引
-- 这里暂时保留 merchant_users 的 idx_tenant_id（如果没有复合唯一索引）


-- 3.3 检查其他表的冗余索引
SELECT '=== 其他表索引优化建议 ===' AS index_suggestion;

SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'user_nexus'
AND TABLE_NAME IN ('merchants', 'merchant_users', 'mini_programs')
AND INDEX_NAME = 'idx_tenant_id'
ORDER BY TABLE_NAME;


-- ===================================================================
-- 最终验证报告
-- ===================================================================

SELECT '=== 最终验证报告 ===' AS final_verification;

-- 验证1: 全局共享表不应该有 tenant_id
SELECT '验证1: 全局共享表不应该有 tenant_id' AS verification_item;

SELECT
    'mini_program_categories' AS table_name,
    CASE
        WHEN COUNT(*) = 0 THEN '✅ 正确（无 tenant_id 字段）'
        ELSE '❌ 错误（仍有 tenant_id 字段）'
    END AS status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'user_nexus'
AND TABLE_NAME = 'mini_program_categories'
AND COLUMN_NAME = 'tenant_id'
UNION ALL
SELECT
    'mini_program_templates' AS table_name,
    CASE
        WHEN COUNT(*) = 0 THEN '✅ 正确（无 tenant_id 字段）'
        ELSE '❌ 错误（仍有 tenant_id 字段）'
    END AS status
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'user_nexus'
AND TABLE_NAME = 'mini_program_templates'
AND COLUMN_NAME = 'tenant_id';


-- 验证2: 关联表的数据一致性
SELECT '验证2: 关联表的数据一致性（不一致数量应该为0）' AS verification_item;

SELECT
    'mini_program_certification' AS table_name,
    SUM(CASE WHEN c.tenant_id != p.tenant_id THEN 1 ELSE 0 END) AS inconsistent_count,
    CASE
        WHEN SUM(CASE WHEN c.tenant_id != p.tenant_id THEN 1 ELSE 0 END) = 0
        THEN '✅ 数据一致'
        ELSE '❌ 存在不一致数据'
    END AS status
FROM mini_program_certification c
LEFT JOIN mini_programs p ON c.mini_program_id = p.id
UNION ALL
SELECT
    'mini_program_development_tasks' AS table_name,
    SUM(CASE WHEN t.tenant_id != p.tenant_id THEN 1 ELSE 0 END) AS inconsistent_count,
    CASE
        WHEN SUM(CASE WHEN t.tenant_id != p.tenant_id THEN 1 ELSE 0 END) = 0
        THEN '✅ 数据一致'
        ELSE '❌ 存在不一致数据'
    END AS status
FROM mini_program_development_tasks t
LEFT JOIN mini_programs p ON t.mini_program_id = p.id
UNION ALL
SELECT
    'mini_program_filing' AS table_name,
    SUM(CASE WHEN f.tenant_id != p.tenant_id THEN 1 ELSE 0 END) AS inconsistent_count,
    CASE
        WHEN SUM(CASE WHEN f.tenant_id != p.tenant_id THEN 1 ELSE 0 END) = 0
        THEN '✅ 数据一致'
        ELSE '❌ 存在不一致数据'
    END AS status
FROM mini_program_filing f
LEFT JOIN mini_programs p ON f.mini_program_id = p.id;


-- 验证3: merchants 表的冗余索引应该删除
SELECT '验证3: merchants 表的冗余索引应该已删除' AS verification_item;

SELECT
    CASE
        WHEN COUNT(*) = 0 THEN '✅ 冗余索引已删除'
        ELSE '❌ 冗余索引仍然存在'
    END AS status
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'user_nexus'
AND TABLE_NAME = 'merchants'
AND INDEX_NAME = 'idx_tenant_id';


-- 验证4: 显示关键的索引信息
SELECT '验证4: 关键索引信息' AS verification_item;

SELECT
    TABLE_NAME,
    INDEX_NAME,
    NON_UNIQUE,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS columns
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'user_nexus'
AND TABLE_NAME IN ('merchants', 'merchant_users', 'mini_programs')
AND INDEX_NAME IN ('uk_tenant_merchant_code', 'idx_tenant_id', 'uk_tenant_merchant_user')
ORDER BY TABLE_NAME, INDEX_NAME;


-- ===================================================================
-- 脚本执行完成
-- ===================================================================
SELECT '✅ user_nexus 数据库优化补充脚本执行完成！' AS final_message;
SELECT '' AS blank_line;
SELECT '优化总结:' AS summary;
SELECT '1. ✅ mini_program_categories 和 mini_program_templates 已回滚为全局共享表' AS summary_1;
SELECT '2. ✅ 关联表的数据一致性已检查和修复' AS summary_2;
SELECT '3. ✅ merchants 表的冗余索引已删除' AS summary_3;
