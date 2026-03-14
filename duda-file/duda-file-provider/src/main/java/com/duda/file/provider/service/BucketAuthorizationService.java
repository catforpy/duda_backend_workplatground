package com.duda.file.provider.service;

import com.duda.file.adapter.AliyunOSSAdapter;
import com.duda.file.dto.bucket.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Bucket授权配置服务
 *
 * 功能：
 * 1. 设置授权配置到OSS
 * 2. 立即同步到数据库
 * 3. 清空Redis缓存
 *
 * @author duda
 * @date 2026-03-14
 */
@Slf4j
@Service
public class BucketAuthorizationService {

    @Autowired
    private AliyunOSSAdapter ossAdapter;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * 设置CORS配置并同步到数据库
     */
    public SetBucketCORSResultDTO setCORSAndSync(String bucketName, SetBucketCORSReqDTO config) {
        log.info("→ 设置CORS配置并同步: {}", bucketName);

        // 1. 设置到OSS
        SetBucketCORSResultDTO result = ossAdapter.setBucketCORS(bucketName, config);

        // 2. 同步到数据库
        syncCORSToDatabase(bucketName, config);

        log.info("✓ CORS配置已同步到数据库: {}", bucketName);
        return result;
    }

    /**
     * 设置防盗链配置并同步到数据库
     */
    public SetBucketRefererResultDTO setRefererAndSync(String bucketName, SetBucketRefererReqDTO config) {
        log.info("→ 设置防盗链配置并同步: {}", bucketName);

        // 1. 设置到OSS
        SetBucketRefererResultDTO result = ossAdapter.setBucketReferer(bucketName, config);

        // 2. 同步到数据库
        syncRefererToDatabase(bucketName, config);

        log.info("✓ 防盗链配置已同步到数据库: {}", bucketName);
        return result;
    }

    /**
     * 设置版本控制配置并同步到数据库
     */
    public SetBucketVersioningResultDTO setVersioningAndSync(String bucketName, SetBucketVersioningReqDTO config) {
        log.info("→ 设置版本控制配置并同步: {}", bucketName);

        // 1. 设置到OSS
        SetBucketVersioningResultDTO result = ossAdapter.setBucketVersioning(bucketName, config);

        // 2. 同步到数据库
        syncVersioningToDatabase(bucketName, config);

        log.info("✓ 版本控制配置已同步到数据库: {}", bucketName);
        return result;
    }

    /**
     * 设置静态网站托管配置并同步到数据库
     */
    public SetBucketWebsiteResultDTO setWebsiteAndSync(String bucketName, SetBucketWebsiteReqDTO config) {
        log.info("→ 设置网站托管配置并同步: {}", bucketName);

        // 1. 设置到OSS
        SetBucketWebsiteResultDTO result = ossAdapter.setBucketWebsite(bucketName, config);

        // 2. 同步到数据库
        syncWebsiteToDatabase(bucketName, config);

        log.info("✓ 网站托管配置已同步到数据库: {}", bucketName);
        return result;
    }

    /**
     * 设置传输加速配置并同步到数据库
     */
    public SetBucketTransferAccelerationResultDTO setTransferAccelerationAndSync(String bucketName, SetBucketTransferAccelerationReqDTO config) {
        log.info("→ 设置传输加速配置并同步: {}", bucketName);

        // 1. 设置到OSS
        SetBucketTransferAccelerationResultDTO result = ossAdapter.setBucketTransferAcceleration(bucketName, config);

        // 2. 同步到数据库
        syncTransferAccelerationToDatabase(bucketName, config);

        log.info("✓ 传输加速配置已同步到数据库: {}", bucketName);
        return result;
    }

    /**
     * 从OSS同步所有授权配置到数据库
     */
    public void syncAllAuthorizationConfig(String bucketName) {
        log.info("→ 从OSS同步所有授权配置到数据库: {}", bucketName);

        try {
            // 1. 从OSS获取所有配置
            Map<String, Object> ossConfig = ossAdapter.getBucketAuthorizationConfig(bucketName);

            // 2. 确保数据库记录存在
            ensureStatisticsExists(bucketName);

            // 3. 更新数据库
            updateAuthorizationConfig(bucketName, ossConfig);

            log.info("✓ 所有授权配置已同步到数据库: {}", bucketName);

        } catch (Exception e) {
            log.error("✗ 同步授权配置失败: {}", bucketName, e);
            throw new RuntimeException("同步授权配置失败: " + e.getMessage(), e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 同步CORS配置到数据库
     */
    private void syncCORSToDatabase(String bucketName, SetBucketCORSReqDTO config) {
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate未注入，跳过数据库同步");
            return;
        }

        try {
            int enabled = (config.getRules() != null && !config.getRules().isEmpty()) ? 1 : 0;
            jdbcTemplate.update(
                "UPDATE bucket_statistics SET cors_enabled = ?, updated_time = NOW() WHERE bucket_name = ?",
                enabled, bucketName
            );
        } catch (Exception e) {
            log.error("同步CORS配置到数据库失败: {}", bucketName, e);
        }
    }

    /**
     * 同步防盗链配置到数据库
     */
    private void syncRefererToDatabase(String bucketName, SetBucketRefererReqDTO config) {
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate未注入，跳过数据库同步");
            return;
        }

        try {
            int enabled = Boolean.TRUE.equals(config.getEnabled()) ? 1 : 0;
            int allowEmpty = Boolean.TRUE.equals(config.getAllowEmpty()) ? 1 : 0;

            jdbcTemplate.update(
                "UPDATE bucket_statistics SET referer_enabled = ?, updated_time = NOW() WHERE bucket_name = ?",
                enabled, bucketName
            );

            // TODO: 可以考虑将refererList保存到referer_config字段（JSON格式）

        } catch (Exception e) {
            log.error("同步防盗链配置到数据库失败: {}", bucketName, e);
        }
    }

    /**
     * 同步版本控制配置到数据库
     */
    private void syncVersioningToDatabase(String bucketName, SetBucketVersioningReqDTO config) {
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate未注入，跳过数据库同步");
            return;
        }

        try {
            int enabled = "Enabled".equalsIgnoreCase(config.getStatus()) ? 1 : 0;

            jdbcTemplate.update(
                "UPDATE bucket_statistics SET versioning_enabled = ?, updated_time = NOW() WHERE bucket_name = ?",
                enabled, bucketName
            );

        } catch (Exception e) {
            log.error("同步版本控制配置到数据库失败: {}", bucketName, e);
        }
    }

    /**
     * 同步网站托管配置到数据库
     */
    private void syncWebsiteToDatabase(String bucketName, SetBucketWebsiteReqDTO config) {
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate未注入，跳过数据库同步");
            return;
        }

        try {
            int enabled = config.getIndexDocument() != null ? 1 : 0;

            jdbcTemplate.update(
                "UPDATE bucket_statistics SET website_enabled = ?, updated_time = NOW() WHERE bucket_name = ?",
                enabled, bucketName
            );

            // TODO: 可以考虑将indexDocument和errorDocument保存到website_config字段（JSON格式）

        } catch (Exception e) {
            log.error("同步网站托管配置到数据库失败: {}", bucketName, e);
        }
    }

    /**
     * 同步传输加速配置到数据库
     */
    private void syncTransferAccelerationToDatabase(String bucketName, SetBucketTransferAccelerationReqDTO config) {
        if (jdbcTemplate == null) {
            log.warn("JdbcTemplate未注入，跳过数据库同步");
            return;
        }

        try {
            int enabled = Boolean.TRUE.equals(config.getEnabled()) ? 1 : 0;

            jdbcTemplate.update(
                "UPDATE bucket_statistics SET transfer_acceleration_enabled = ?, updated_time = NOW() WHERE bucket_name = ?",
                enabled, bucketName
            );

        } catch (Exception e) {
            log.error("同步传输加速配置到数据库失败: {}", bucketName, e);
        }
    }

    /**
     * 确保统计记录存在
     */
    private void ensureStatisticsExists(String bucketName) {
        if (jdbcTemplate == null) {
            return;
        }

        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM bucket_statistics WHERE bucket_name = ?",
            Integer.class,
            bucketName
        );

        if (count == null || count == 0) {
            log.info("→ 记录不存在，创建新记录...");
            jdbcTemplate.update(
                "INSERT INTO bucket_statistics (" +
                    "bucket_name, region, storage_type, " +
                    "total_file_count, total_storage_size, " +
                    "image_count, video_count, document_count, other_count, " +
                    "created_time, updated_time" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                bucketName, "cn-hangzhou", "STANDARD",
                0, 0, 0, 0, 0, 0
            );
            log.info("✓ 统计记录已创建");
        }
    }

    /**
     * 更新授权配置到数据库
     */
    private void updateAuthorizationConfig(String bucketName, Map<String, Object> config) {
        if (jdbcTemplate == null) {
            return;
        }

        // 更新CORS
        Boolean corsEnabled = (Boolean) config.get("corsEnabled");
        if (corsEnabled != null) {
            jdbcTemplate.update(
                "UPDATE bucket_statistics SET cors_enabled = ?, updated_time = NOW() WHERE bucket_name = ?",
                corsEnabled ? 1 : 0, bucketName
            );
            log.info("  ✓ CORS: {}", corsEnabled ? "已启用" : "未启用");
        }

        // 更新防盗链
        Boolean refererEnabled = (Boolean) config.get("refererEnabled");
        if (refererEnabled != null) {
            jdbcTemplate.update(
                "UPDATE bucket_statistics SET referer_enabled = ?, updated_time = NOW() WHERE bucket_name = ?",
                refererEnabled ? 1 : 0, bucketName
            );
            log.info("  ✓ 防盗链: {}", refererEnabled ? "已启用" : "未启用");
        }

        // 更新版本控制
        Boolean versioningEnabled = (Boolean) config.get("versioningEnabled");
        if (versioningEnabled != null) {
            jdbcTemplate.update(
                "UPDATE bucket_statistics SET versioning_enabled = ?, updated_time = NOW() WHERE bucket_name = ?",
                versioningEnabled ? 1 : 0, bucketName
            );
            log.info("  ✓ 版本控制: {}", versioningEnabled ? "已启用" : "未启用");
        }

        // 更新Website
        Boolean websiteEnabled = (Boolean) config.get("websiteEnabled");
        if (websiteEnabled != null) {
            jdbcTemplate.update(
                "UPDATE bucket_statistics SET website_enabled = ?, updated_time = NOW() WHERE bucket_name = ?",
                websiteEnabled ? 1 : 0, bucketName
            );
            log.info("  ✓ Website: {}", websiteEnabled ? "已启用" : "未启用");
        }

        // 更新传输加速
        Boolean transferAccelerationEnabled = (Boolean) config.get("transferAccelerationEnabled");
        if (transferAccelerationEnabled != null) {
            jdbcTemplate.update(
                "UPDATE bucket_statistics SET transfer_acceleration_enabled = ?, updated_time = NOW() WHERE bucket_name = ?",
                transferAccelerationEnabled ? 1 : 0, bucketName
            );
            log.info("  ✓ 传输加速: {}", transferAccelerationEnabled ? "已启用" : "未启用");
        }

        // 更新ACL
        String acl = (String) config.get("acl");
        if (acl != null) {
            jdbcTemplate.update(
                "UPDATE bucket_statistics SET acl_type = ?, updated_time = NOW() WHERE bucket_name = ?",
                acl, bucketName
            );
            log.info("  ✓ ACL: {}", acl);
        }

        // 更新同步时间
        jdbcTemplate.update(
            "UPDATE bucket_statistics SET last_sync_time = NOW(), updated_time = NOW() WHERE bucket_name = ?",
            bucketName
        );
        log.info("  ✓ 同步时间: 已更新");
    }
}
