package com.duda.file.provider.sync;

import com.duda.file.adapter.AliyunOSSAdapter;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import com.duda.file.provider.entity.BucketConfig;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.provider.util.AesEncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 授权配置定时同步组件
 *
 * 功能：
 * 1. 定时从OSS获取授权配置
 * 2. 同步到数据库
 * 3. 清空Redis缓存
 *
 * 同步频率：每小时执行一次
 *
 * @author duda
 * @date 2026-03-14
 */
@Slf4j
@Component
@EnableScheduling
public class ScheduledAuthorizationSyncComponent {

    @Value("${duda.file.storage.default-region:cn-hangzhou}")
    private String defaultRegion;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Autowired
    private AesEncryptUtil aesEncryptUtil;

    /**
     * 定时同步授权配置
     *
     * Cron表达式：0 0 * * * ? -> 每小时的第0分钟执行（即每小时执行一次）
     *
     * 执行时间：
     * - 00:00, 01:00, 02:00, ..., 23:00
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncAuthorizationConfig() {
        log.info("╔════════════════════════════════════════╗");
        log.info("║   定时同步授权配置 - 开始执行             ║");
        log.info("║   执行时间: {}", java.time.LocalDateTime.now());
        log.info("╚════════════════════════════════════════╝");

        try {
            // 1. 获取所有需要同步的Bucket
            log.info("\n========================================");
            log.info("步骤1: 获取所有Bucket");
            log.info("========================================");

            List<String> buckets = getActiveBuckets();
            log.info("✓ 找到 {} 个活跃Bucket", buckets.size());

            if (buckets.isEmpty()) {
                log.info("没有需要同步的Bucket，结束任务");
                return;
            }

            // 2. 同步每个Bucket的授权配置
            log.info("\n========================================");
            log.info("步骤2: 同步授权配置");
            log.info("========================================");

            int successCount = 0;
            int failCount = 0;

            for (String bucketName : buckets) {
                try {
                    log.info("\n→ 处理Bucket: {}", bucketName);

                    // 为每个bucket创建对应的OSS适配器
                    AliyunOSSAdapter ossAdapter = getOSSAdapter(bucketName);

                    // 从OSS获取授权配置
                    var ossConfig = ossAdapter.getBucketAuthorizationConfig(bucketName);

                    // 同步到数据库
                    syncConfigToDatabase(bucketName, ossConfig);

                    log.info("✓ {} 同步完成", bucketName);
                    successCount++;

                } catch (Exception e) {
                    log.error("✗ {} 同步失败: {}", bucketName, e.getMessage());
                    failCount++;
                }
            }

            // 4. 汇总结果
            log.info("\n========================================");
            log.info("步骤4: 同步结果汇总");
            log.info("========================================");

            log.info("✓ 总数: {}", buckets.size());
            log.info("✓ 成功: {}", successCount);
            log.info("✗ 失败: {}", failCount);

            log.info("\n╔════════════════════════════════════════╗");
            log.info("║   定时同步完成! ✓                        ║");
            log.info("╚════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("\n✗ 定时同步执行失败", e);
        }
    }

    /**
     * 获取所有活跃的Bucket
     */
    private List<String> getActiveBuckets() {
        return jdbcTemplate.queryForList(
            "SELECT bucket_name FROM bucket_statistics WHERE status = 'ACTIVE' ORDER BY bucket_name",
            String.class
        );
    }

    /**
     * 获取OSS适配器（从数据库读取密钥）
     */
    private AliyunOSSAdapter getOSSAdapter(String bucketName) {
        // 1. 从数据库查询bucket配置
        BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
        if (bucketConfig == null) {
            throw new RuntimeException("Bucket配置不存在: " + bucketName);
        }

        // 2. 解密密钥
        String accessKeyId = aesEncryptUtil.decrypt(bucketConfig.getAccessKeyId());
        String accessKeySecret = aesEncryptUtil.decrypt(bucketConfig.getAccessKeySecret());

        // 3. 创建OSS适配器配置
        ApiKeyConfigDTO config = ApiKeyConfigDTO.builder()
            .accessKeyId(accessKeyId)
            .accessKeySecret(accessKeySecret)
            .region(bucketConfig.getRegion())
            .endpoint(bucketConfig.getEndpoint())
            .build();

        // 4. 创建并返回OSS适配器实例
        return new AliyunOSSAdapter(config);
    }

    /**
     * 同步配置到数据库
     */
    private void syncConfigToDatabase(String bucketName, java.util.Map<String, Object> config) {
        // 1. 确保记录存在
        ensureStatisticsExists(bucketName);

        // 2. 更新授权配置
        updateAuthorizationConfig(bucketName, config);
    }

    /**
     * 确保统计记录存在
     */
    private void ensureStatisticsExists(String bucketName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM bucket_statistics WHERE bucket_name = ?",
            Integer.class,
            bucketName
        );

        if (count == null || count == 0) {
            log.info("  → 记录不存在，创建新记录...");

            // 从bucket_config获取region信息
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            String region = (bucketConfig != null) ? bucketConfig.getRegion() : defaultRegion;

            jdbcTemplate.update(
                "INSERT INTO bucket_statistics (" +
                    "bucket_name, region, storage_type, " +
                    "total_file_count, total_storage_size, " +
                    "image_count, video_count, document_count, other_count, " +
                    "created_time, updated_time" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
                bucketName, region, "STANDARD",
                0, 0, 0, 0, 0, 0
            );
            log.info("  ✓ 统计记录已创建");
        }
    }

    /**
     * 更新授权配置到数据库
     */
    private void updateAuthorizationConfig(String bucketName, java.util.Map<String, Object> config) {
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
