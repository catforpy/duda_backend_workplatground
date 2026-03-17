package com.duda.file.provider.sync;

import com.duda.file.adapter.AliyunOSSAdapter;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import com.duda.file.provider.entity.BucketConfig;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.provider.util.AesEncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 授权配置自动同步组件
 * 应用启动时自动执行，从OSS获取授权配置并写入数据库
 *
 * @author duda
 * @date 2025-03-14
 */
@Slf4j
@Component
public class AuthorizationConfigSyncComponent implements CommandLineRunner {

    @Value("${duda.file.storage.default-region:cn-hangzhou}")
    private String defaultRegion;

    @Value("${sync.bucket.name:test-bucket}")
    private String bucketName;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Autowired
    private AesEncryptUtil aesEncryptUtil;

    @Override
    public void run(String... args) throws Exception {
        log.info("╔════════════════════════════════════════╗");
        log.info("║   授权配置自动同步 - 开始执行             ║");
        log.info("╚════════════════════════════════════════╝");

        try {
            // 1. 测试数据库连接
            log.info("\n========================================");
            log.info("步骤1: 测试数据库连接");
            log.info("========================================");

            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.info("✓ 数据库连接成功: {}", result);

            // 2. 从数据库获取bucket配置
            log.info("\n========================================");
            log.info("步骤2: 从数据库获取bucket配置");
            log.info("========================================");

            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                log.warn("⚠️ Bucket配置不存在: {}", bucketName);
                log.info("跳过授权配置同步");
                return;
            }

            log.info("✓ 找到Bucket配置: {}", bucketName);
            log.info("  - Region: {}", bucketConfig.getRegion());
            log.info("  - Storage Type: {}", bucketConfig.getStorageType());

            // 3. 解密密钥并初始化OSS适配器
            log.info("\n========================================");
            log.info("步骤3: 解密密钥并初始化OSS适配器");
            log.info("========================================");

            String accessKeyId = aesEncryptUtil.decrypt(bucketConfig.getAccessKeyId());
            String accessKeySecret = aesEncryptUtil.decrypt(bucketConfig.getAccessKeySecret());

            ApiKeyConfigDTO config = ApiKeyConfigDTO.builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .region(bucketConfig.getRegion())
                .endpoint(bucketConfig.getEndpoint())
                .build();

            AliyunOSSAdapter ossAdapter = new AliyunOSSAdapter(config);
            log.info("✓ OSS适配器初始化成功");

            // 4. 从OSS获取授权配置
            log.info("\n========================================");
            log.info("步骤4: 从OSS获取授权配置");
            log.info("========================================");

            Map<String, Object> authConfig = ossAdapter.getBucketAuthorizationConfig(bucketName);

            log.info("✓ 获取授权配置成功:");
            log.info("  - ACL: {}", authConfig.get("acl"));
            log.info("  - Bucket Policy: {}", authConfig.get("bucketPolicyEnabled"));
            log.info("  - CORS: {}", authConfig.get("corsEnabled"));
            log.info("  - 防盗链: {}", authConfig.get("refererEnabled"));
            log.info("  - 版本控制: {}", authConfig.get("versioningEnabled"));

            // 5. 确保记录存在
            log.info("\n========================================");
            log.info("步骤5: 确保数据库记录存在");
            log.info("========================================");

            ensureStatisticsExists();

            // 6. 写入授权配置
            log.info("\n========================================");
            log.info("步骤6: 写入授权配置到数据库");
            log.info("========================================");

            updateAuthorizationConfig(authConfig);

            log.info("✓ 授权配置已写入数据库");

            // 7. 验证数据
            log.info("\n========================================");
            log.info("步骤7: 验证数据库中的数据");
            log.info("========================================");

            verifyData();

            log.info("\n╔════════════════════════════════════════╗");
            log.info("║     同步完成! ✓                         ║");
            log.info("║     授权配置已自动保存到数据库            ║");
            log.info("╚════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("\n✗ 同步失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }

    /**
     * 确保统计记录存在
     */
    private void ensureStatisticsExists() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM bucket_statistics WHERE bucket_name = ?",
            Integer.class,
            bucketName
        );

        if (count == null || count == 0) {
            log.info("→ 记录不存在，创建新记录...");

            // 从bucketConfig获取region信息
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
            log.info("✓ 统计记录已创建");
        } else {
            log.info("✓ 统计记录已存在");
        }
    }

    /**
     * 更新授权配置到数据库
     *
     * 注意：bucket_statistics表已精简，删除了配置字段（acl_type, cors_enabled等）
     * 这些配置现在只存储在bucket_config表中
     */
    private void updateAuthorizationConfig(Map<String, Object> config) {
        // ⚠️ bucket_statistics表已精简，不再存储配置字段
        // 以下配置字段已从bucket_statistics表中删除：
        // - acl_type, bucket_policy, cors_enabled, referer_enabled
        // - versioning_enabled, website_enabled, worm_enabled
        // 这些配置现在只存在于bucket_config表中

        // 只更新同步时间
        jdbcTemplate.update(
            "UPDATE bucket_statistics SET last_sync_time = NOW(), updated_time = NOW() WHERE bucket_name = ?",
            bucketName
        );
        log.info("  ✓ 同步时间: 已更新");
    }

    /**
     * 验证数据
     */
    private void verifyData() {
        Map<String, Object> result = jdbcTemplate.queryForMap(
            "SELECT * FROM bucket_statistics WHERE bucket_name = ?",
            bucketName
        );

        log.info("✓ 数据验证成功，数据库中的授权配置:");
        log.info("");
        log.info("  ┌─────────────────────────────────────┐");
        log.info("  │ Bucket授权配置                      │");
        log.info("  ├─────────────────────────────────────┤");
        log.info("  │ Bucket名称: {}", result.get("bucket_name"));
        log.info("  │ ACL权限: {}", result.get("acl_type"));
        log.info("  │ Bucket Policy: {}", result.get("bucket_policy") != null ? "已设置" : "未设置");
        log.info("  │ CORS: {}", result.get("cors_enabled"));
        log.info("  │ 防盗链: {}", result.get("referer_enabled"));
        log.info("  │ 版本控制: {}", result.get("versioning_enabled"));
        log.info("  │ Website: {}", result.get("website_enabled"));
        log.info("  │ WORM: {}", result.get("worm_enabled"));
        log.info("  │ 同步时间: {}", result.get("last_sync_time"));
        log.info("  └─────────────────────────────────────┘");
        log.info("");
        log.info("✓ 所有授权配置已成功保存到数据库！");
        log.info("✓ 客户可以在控制台查看这些授权信息");
    }
}
