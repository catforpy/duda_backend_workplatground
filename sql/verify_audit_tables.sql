-- =============================================
-- 验证审计和统计表是否正确创建
-- =============================================

-- 1. 检查操作日志表
SELECT '检查 oss_operation_log 表结构' AS '';
DESC oss_operation_log;

-- 2. 检查统计表
SELECT '检查 bucket_statistics 表结构' AS '';
DESC bucket_statistics;

-- 3. 查询操作日志（最新10条）
SELECT '查看最新10条操作日志' AS '';
SELECT
    id,
    bucket_name,
    operation_type,
    operation_category,
    object_key,
    status,
    file_size,
    DATE_FORMAT(created_time, '%Y-%m-%d %H:%i:%s') AS created_time
FROM oss_operation_log
ORDER BY created_time DESC
LIMIT 10;

-- 4. 查询Bucket统计
SELECT '查看Bucket统计信息' AS '';
SELECT
    bucket_name,
    region,
    storage_type,
    total_file_count,
    total_storage_size,
    image_count,
    video_count,
    document_count,
    total_traffic_bytes,
    download_traffic_bytes,
    storage_cost,
    traffic_cost,
    total_cost,
    DATE_FORMAT(last_upload_time, '%Y-%m-%d %H:%i:%s') AS last_upload_time,
    DATE_FORMAT(last_download_time, '%Y-%m-%d %H:%i:%s') AS last_download_time
FROM bucket_statistics
ORDER BY updated_time DESC;

-- 5. 统计操作类型分布
SELECT '操作类型统计' AS '';
SELECT
    operation_type,
    COUNT(*) AS total_count,
    COUNT(CASE WHEN status = 'SUCCESS' THEN 1 END) AS success_count,
    COUNT(CASE WHEN status = 'FAILED' THEN 1 END) AS failed_count
FROM oss_operation_log
GROUP BY operation_type;

-- 6. 检查索引
SELECT '检查 oss_operation_log 索引' AS '';
SHOW INDEX FROM oss_operation_log;

SELECT '检查 bucket_statistics 索引' AS '';
SHOW INDEX FROM bucket_statistics;
