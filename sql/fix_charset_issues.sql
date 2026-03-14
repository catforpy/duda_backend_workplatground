-- =============================================
-- 修复数据库字符集问题
-- =============================================
-- 问题：数据库注释显示乱码
-- 原因：MySQL连接或存储字符集不是UTF-8
-- =============================================

-- 1. 检查当前数据库字符集
SELECT
    DEFAULT_CHARACTER_SET_NAME,
    DEFAULT_COLLATION_NAME
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME = 'duda_file';

-- 2. 检查表的字符集
SELECT
    TABLE_NAME,
    TABLE_COLLATION,
    CHARACTER_SET_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'duda_file'
  AND TABLE_NAME IN (
    'bucket_config',
    'bucket_storage_log',
    'bucket_traffic_log',
    'bucket_billing_record',
    'bucket_billing_config'
  );

-- 3. 修复数据库字符集（如果不是utf8mb4）
ALTER DATABASE duda_file
CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;

-- 4. 修复表字符集（如果不是utf8mb4）
ALTER TABLE bucket_config
CONVERT TO CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

ALTER TABLE bucket_storage_log
CONVERT TO CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

ALTER TABLE bucket_traffic_log
CONVERT TO CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

ALTER TABLE bucket_billing_record
CONVERT TO CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

ALTER TABLE bucket_billing_config
CONVERT TO CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 5. 验证修复结果
SELECT
    TABLE_NAME,
    TABLE_COLLATION,
    CHARACTER_SET_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'duda_file'
ORDER BY TABLE_NAME;

-- =============================================
-- 使用说明：
-- =============================================
-- 1. 查看当前字符集：
--    SHOW VARIABLES LIKE 'character%';
--    SHOW VARIABLES LIKE 'collation%';
--
-- 2. 执行此SQL文件时，确保使用UTF-8编码：
--    mysql -h120.26.170.213 -uroot -p'duda2024' --default-character-set=utf8mb4 duda_file < fix_charset_issues.sql
--
-- 3. 或者在MySQL客户端执行：
--    SET NAMES utf8mb4;
--    SOURCE /path/to/fix_charset_issues.sql;
--
-- 4. 检查连接字符集：
--    SHOW VARIABLES LIKE 'character_set_client';
--    SHOW VARIABLES LIKE 'character_set_results';
--    SHOW VARIABLES LIKE 'character_set_connection';
--
-- 5. 如果还是乱码，检查Java连接字符串：
--    jdbc:mysql://120.26.170.213:3306/duda_file?useSSL=false&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8
--    添加: &useUnicode=true&characterEncoding=utf8
-- =============================================
