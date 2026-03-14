package com.duda.file.provider.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bucket状态统计服务
 *
 * 功能：
 * 1. 实时统计Bucket的文件数量、存储量、流量
 * 2. 每次操作后自动更新统计信息
 * 3. 供客户控制台查看Bucket使用情况
 *
 * @author duda
 * @date 2025-03-14
 */
@Slf4j
@Service
public class BucketStatisticsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 记录文件上传操作
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象Key
     * @param fileSize 文件大小
     * @param fileType 文件类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordUpload(String bucketName, String objectKey, Long fileSize, String fileType) {
        try {
            // 1. 确保统计记录存在
            ensureStatisticsExists(bucketName);

            // 2. 判断文件类型
            String category = getFileCategory(fileType);

            // 3. 更新统计
            String sql = String.format(
                "UPDATE bucket_statistics SET " +
                    "total_file_count = total_file_count + 1, " +
                    "total_storage_size = total_storage_size + %d, " +
                    "%s_count = %s_count + 1, " +
                    "last_upload_time = '%s', " +
                    "updated_time = '%s' " +
                    "WHERE bucket_name = '%s'",
                fileSize,
                category + "_count",
                category + "_count",
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                bucketName
            );

            jdbcTemplate.update(sql);

            log.debug("✓ 上传统计已更新: {} (+1 文件, +{} bytes)", bucketName, fileSize);

        } catch (Exception e) {
            log.error("✗ 更新上传统计失败: {} - {}", bucketName, e.getMessage());
        }
    }

    /**
     * 记录文件下载操作
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象Key
     * @param fileSize 文件大小
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordDownload(String bucketName, String objectKey, Long fileSize) {
        try {
            // 1. 确保统计记录存在
            ensureStatisticsExists(bucketName);

            // 2. 更新流量统计
            String sql = String.format(
                "UPDATE bucket_statistics SET " +
                    "total_traffic_bytes = total_traffic_bytes + %d, " +
                    "download_traffic_bytes = download_traffic_bytes + %d, " +
                    "traffic_cost = traffic_cost + %s, " +
                    "last_download_time = '%s', " +
                    "updated_time = '%s' " +
                    "WHERE bucket_name = '%s'",
                fileSize,
                fileSize,
                calculateTrafficCost(fileSize),
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                bucketName
            );

            jdbcTemplate.update(sql);

            log.debug("✓ 下载统计已更新: {} (+{} bytes, +{}元)", bucketName, fileSize, calculateTrafficCost(fileSize));

        } catch (Exception e) {
            log.error("✗ 更新下载统计失败: {} - {}", bucketName, e.getMessage());
        }
    }

    /**
     * 记录文件删除操作
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象Key
     * @param fileSize 文件大小
     * @param fileType 文件类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordDelete(String bucketName, String objectKey, Long fileSize, String fileType) {
        try {
            // 1. 确保统计记录存在
            ensureStatisticsExists(bucketName);

            // 2. 判断文件类型
            String category = getFileCategory(fileType);

            // 3. 更新统计
            String sql = String.format(
                "UPDATE bucket_statistics SET " +
                    "total_file_count = total_file_count - 1, " +
                    "total_storage_size = total_storage_size - %d, " +
                    "%s_count = %s_count - 1, " +
                    "last_delete_time = '%s', " +
                    "updated_time = '%s' " +
                    "WHERE bucket_name = '%s'",
                fileSize,
                category + "_count",
                category + "_count",
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString(),
                bucketName
            );

            jdbcTemplate.update(sql);

            log.debug("✓ 删除统计已更新: {} (-1 文件, -{} bytes)", bucketName, fileSize);

        } catch (Exception e) {
            log.error("✗ 更新删除统计失败: {} - {}", bucketName, e.getMessage());
        }
    }

    /**
     * 确保统计记录存在
     */
    private void ensureStatisticsExists(String bucketName) {
        // 检查是否存在
        String countSql = String.format("SELECT COUNT(*) FROM bucket_statistics WHERE bucket_name = '%s'", bucketName);
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);

        if (count == null || count == 0) {
            // 不存在，插入新记录
            String insertSql = String.format(
                "INSERT INTO bucket_statistics (" +
                    "bucket_name, region, storage_type, " +
                    "total_file_count, total_storage_size, " +
                    "image_count, video_count, document_count, other_count, " +
                    "last_sync_time, created_time, updated_time" +
                ") VALUES (" +
                    "'%s', 'cn-hangzhou', 'STANDARD', " +
                    "0, 0, " +
                    "0, 0, 0, 0, " +
                    "'%s', NOW(), NOW()" +
                    ")",
                bucketName,
                LocalDateTime.now().toString()
            );

            jdbcTemplate.update(insertSql);
            log.info("✓ 创建统计记录: {}", bucketName);
        }
    }

    /**
     * 获取文件类型分类
     */
    private String getFileCategory(String fileType) {
        if (fileType == null) {
            return "other";
        }

        String type = fileType.toLowerCase();
        if (type.startsWith("image/")) {
            return "image";
        } else if (type.startsWith("video/")) {
            return "video";
        } else if (type.startsWith("application/pdf") || type.contains("document") || type.contains("text")) {
            return "document";
        } else {
            return "other";
        }
    }

    /**
     * 计算流量费用（示例：0.5元/GB）
     */
    private BigDecimal calculateTrafficCost(Long bytes) {
        // 费率：0.5元/GB
        BigDecimal rate = new BigDecimal("0.5");
        BigDecimal bytesPerGB = new BigDecimal("1073741824"); // 1024^3

        BigDecimal gb = new BigDecimal(bytes)
            .divide(bytesPerGB, 4, BigDecimal.ROUND_HALF_UP);

        return gb.multiply(rate).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
