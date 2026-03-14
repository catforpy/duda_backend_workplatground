package com.duda.file.provider.service;

import com.alibaba.fastjson2.JSON;
import com.duda.file.adapter.AliyunOSSAdapter;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Bucket授权配置同步服务
 *
 * 功能：
 * 1. 从OSS获取Bucket的所有授权配置
 * 2. 将授权配置同步到bucket_statistics表
 * 3. 记录授权变更历史
 *
 * @author duda
 * @date 2025-03-14
 */
@Slf4j
@Service
public class BucketAuthorizationSyncService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OssOperationLogService logService;

    /**
     * 同步单个Bucket的授权配置
     *
     * @param bucketName Bucket名称
     * @param ossClient OSS客户端
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncBucketAuthorization(String bucketName, AliyunOSSAdapter ossClient) {
        try {
            log.info("→ 开始同步Bucket授权配置: {}", bucketName);

            // 1. 从OSS获取所有授权配置
            Map<String, Object> config = ossClient.getBucketAuthorizationConfig(bucketName);

            // 2. 确保统计记录存在
            ensureStatisticsExists(bucketName);

            // 3. 更新授权配置到数据库
            updateAuthorizationConfig(bucketName, config);

            // 4. 记录同步操作
            logService.logQuery(bucketName, null, "同步授权配置", 1);

            log.info("✓ Bucket授权配置同步成功: {}", bucketName);

        } catch (Exception e) {
            log.error("✗ 同步Bucket授权配置失败: {} - {}", bucketName, e.getMessage());
            throw e;
        }
    }

    /**
     * 更新授权配置到数据库
     */
    private void updateAuthorizationConfig(String bucketName, Map<String, Object> config) {
        // ACL权限
        String acl = (String) config.get("acl");
        if (acl != null) {
            updateField(bucketName, "acl_type", acl);
        }

        // Bucket Policy（RAM授权策略）
        String bucketPolicy = (String) config.get("bucketPolicy");
        if (bucketPolicy != null) {
            updateField(bucketName, "bucket_policy", bucketPolicy);
        }

        // CORS配置
        List<?> corsRules = (List<?>) config.get("corsRules");
        Boolean corsEnabled = (Boolean) config.get("corsEnabled");
        updateField(bucketName, "cors_enabled", corsEnabled ? 1 : 0);
        if (corsRules != null && !corsRules.isEmpty()) {
            updateField(bucketName, "cors_config", JSON.toJSONString(corsRules));
        }

        // 防盗链配置
        Boolean refererEnabled = (Boolean) config.get("refererEnabled");
        updateField(bucketName, "referer_enabled", refererEnabled ? 1 : 0);
        if (refererEnabled && config.get("referer") != null) {
            updateField(bucketName, "referer_config", JSON.toJSONString(config.get("referer")));
        }

        // 生命周期配置
        Boolean lifecycleEnabled = (Boolean) config.get("lifecycleEnabled");
        if (lifecycleEnabled && config.get("lifecycle") != null) {
            updateField(bucketName, "lifecycle_config", JSON.toJSONString(config.get("lifecycle")));
        }

        // 版本控制
        Boolean versioningEnabled = (Boolean) config.get("versioningEnabled");
        updateField(bucketName, "versioning_enabled", versioningEnabled ? 1 : 0);

        // 日志配置
        Boolean loggingEnabled = (Boolean) config.get("loggingEnabled");
        if (loggingEnabled && config.get("logging") != null) {
            updateField(bucketName, "logging_config", JSON.toJSONString(config.get("logging")));
        }

        // Website托管
        Boolean websiteEnabled = (Boolean) config.get("websiteEnabled");
        updateField(bucketName, "website_enabled", websiteEnabled ? 1 : 0);
        if (websiteEnabled && config.get("website") != null) {
            updateField(bucketName, "website_config", JSON.toJSONString(config.get("website")));
        }

        // WORM保留策略
        Boolean wormEnabled = (Boolean) config.get("wormEnabled");
        updateField(bucketName, "worm_enabled", wormEnabled ? 1 : 0);

        // 跨区域复制
        Boolean replicationEnabled = (Boolean) config.get("replicationEnabled");
        if (replicationEnabled && config.get("replication") != null) {
            updateField(bucketName, "replication_config", JSON.toJSONString(config.get("replication")));
        }

        // 传输加速
        Boolean transferAccelerationEnabled = (Boolean) config.get("transferAccelerationEnabled");
        updateField(bucketName, "transfer_acceleration_enabled", transferAccelerationEnabled ? 1 : 0);

        // 访问监控
        Boolean accessMonitorEnabled = (Boolean) config.get("accessMonitorEnabled");
        updateField(bucketName, "access_monitor_enabled", accessMonitorEnabled ? 1 : 0);

        // 更新同步时间
        updateField(bucketName, "last_sync_time", LocalDateTime.now().toString());

        log.debug("✓ 授权配置已更新到数据库: {}", bucketName);
    }

    /**
     * 更新单个字段
     */
    private void updateField(String bucketName, String fieldName, Object value) {
        String sql;
        if (value instanceof String) {
            sql = String.format(
                "UPDATE bucket_statistics SET %s = '%s', updated_time = '%s' WHERE bucket_name = '%s'",
                fieldName,
                escapeSql((String) value),
                LocalDateTime.now().toString(),
                bucketName
            );
        } else if (value instanceof Integer) {
            sql = String.format(
                "UPDATE bucket_statistics SET %s = %d, updated_time = '%s' WHERE bucket_name = '%s'",
                fieldName,
                value,
                LocalDateTime.now().toString(),
                bucketName
            );
        } else {
            return;
        }

        try {
            jdbcTemplate.update(sql);
        } catch (Exception e) {
            log.warn("更新字段失败: {} - {}", fieldName, e.getMessage());
        }
    }

    /**
     * SQL转义
     */
    private String escapeSql(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''");
    }

    /**
     * 确保统计记录存在
     */
    private void ensureStatisticsExists(String bucketName) {
        String countSql = String.format(
            "SELECT COUNT(*) FROM bucket_statistics WHERE bucket_name = '%s'",
            bucketName
        );
        Integer count = jdbcTemplate.queryForObject(countSql, Integer.class);

        if (count == null || count == 0) {
            String insertSql = String.format(
                "INSERT INTO bucket_statistics (" +
                    "bucket_name, region, storage_type, " +
                    "total_file_count, total_storage_size, " +
                    "image_count, video_count, document_count, other_count, " +
                    "created_time, updated_time" +
                ") VALUES (" +
                    "'%s', 'cn-hangzhou', 'STANDARD', " +
                    "0, 0, " +
                    "0, 0, 0, 0, " +
                    "'%s', '%s'" +
                ")",
                bucketName,
                LocalDateTime.now().toString(),
                LocalDateTime.now().toString()
            );

            jdbcTemplate.update(insertSql);
            log.info("✓ 创建统计记录: {}", bucketName);
        }
    }

    /**
     * 记录授权变更
     *
     * @param bucketName Bucket名称
     * @param configField 配置字段
     * @param oldValue 旧值
     * @param newValue 新值
     */
    public void logAuthorizationChange(String bucketName, String configField,
                                       Object oldValue, Object newValue) {
        try {
            String oldStr = oldValue != null ? JSON.toJSONString(oldValue) : "null";
            String newStr = newValue != null ? JSON.toJSONString(newValue) : "null";

            logService.logConfigChange(
                bucketName,
                configField,
                oldStr,
                newStr,
                "SUCCESS"
            );

            log.info("✓ 授权变更已记录: {} - {}", bucketName, configField);

        } catch (Exception e) {
            log.error("✗ 记录授权变更失败: {} - {}", bucketName, e.getMessage());
        }
    }
}
