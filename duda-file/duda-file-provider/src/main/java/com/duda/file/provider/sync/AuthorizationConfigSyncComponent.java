package com.duda.file.provider.sync;

import com.duda.file.adapter.AliyunOSSAdapter;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
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

    @Value("${aliyun.sts.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sts.access-key-secret}")
    private String accessKeySecret;

    @Value("${duda.file.storage.default-region:cn-hangzhou}")
    private String region;

    @Value("${sync.bucket.name:duda-java-backend-test}")
    private String bucketName;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

            // 2. 初始化OSS适配器
            log.info("\n========================================");
            log.info("步骤2: 初始化OSS适配器");
            log.info("========================================");

            ApiKeyConfigDTO config = ApiKeyConfigDTO.builder()
                .accessKeyId(accessKeyId)
                .accessKeySecret(accessKeySecret)
                .region(region)
                .build();

            AliyunOSSAdapter ossAdapter = new AliyunOSSAdapter(config);
            log.info("✓ OSS适配器初始化成功");
            log.info("  - Bucket: {}", bucketName);
            log.info("  - Region: {}", region);

            // 3. 从OSS获取授权配置
            log.info("\n========================================");
            log.info("步骤3: 从OSS获取授权配置");
            log.info("========================================");

            Map<String, Object> authConfig = ossAdapter.getBucketAuthorizationConfig(bucketName);

            log.info("✓ 获取授权配置成功:");
            log.info("  - ACL: {}", authConfig.get("acl"));
            log.info("  - Bucket Policy: {}", authConfig.get("bucketPolicyEnabled"));
            log.info("  - CORS: {}", authConfig.get("corsEnabled"));
            log.info("  - 防盗链: {}", authConfig.get("refererEnabled"));
            log.info("  - 版本控制: {}", authConfig.get("versioningEnabled"));

            // 4. 确保记录存在
            log.info("\n========================================");
            log.info("步骤4: 确保数据库记录存在");
            log.info("========================================");

            ensureStatisticsExists();

            // 5. 写入授权配置
            log.info("\n========================================");
            log.info("步骤5: 写入授权配置到数据库");
            log.info("========================================");

            updateAuthorizationConfig(authConfig);

            log.info("✓ 授权配置已写入数据库");

            // 6. 验证数据
            log.info("\n========================================");
            log.info("步骤6: 验证数据库中的数据");
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
     */
    private void updateAuthorizationConfig(Map<String, Object> config) {
        // 更新ACL
        String acl = (String) config.get("acl");
        if (acl != null) {
            jdbcTemplate.update(
                "UPDATE bucket_statistics SET acl_type = ?, updated_time = NOW() WHERE bucket_name = ?",
                acl, bucketName
            );
            log.info("  ✓ ACL: {}", acl);
        }

        // 更新Bucket Policy
        String bucketPolicy = (String) config.get("bucketPolicy");
        if (bucketPolicy != null) {
            jdbcTemplate.update(
                "UPDATE bucket_statistics SET bucket_policy = ?, updated_time = NOW() WHERE bucket_name = ?",
                bucketPolicy, bucketName
            );
            log.info("  ✓ Bucket Policy: 已设置");
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

        // 更新WORM
        Boolean wormEnabled = (Boolean) config.get("wormEnabled");
        if (wormEnabled != null) {
            jdbcTemplate.update(
                "UPDATE bucket_statistics SET worm_enabled = ?, updated_time = NOW() WHERE bucket_name = ?",
                wormEnabled ? 1 : 0, bucketName
            );
            log.info("  ✓ WORM: {}", wormEnabled ? "已启用" : "未启用");
        }

        // 更新同步时间
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
