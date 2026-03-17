# STS代码调整方案

> 生成时间: 2026-03-17
> 目标: 根据最新的数据库表结构调整STS相关代码

---

## 📋 问题分析

### 当前STS实现流程

```
1. 用户调用 getSTSForClientUpload(GetSTSReqDTO)
   ↓
2. 查询Bucket配置: bucketConfigMapper.selectByBucketName(bucketName)
   ↓
3. 调用STS服务: stsService.generateSTSCredentials()
   ↓
4. STSService从application.properties读取配置 ❌ 问题！
   - aliyun.sts.access-key-id
   - aliyun.sts.access-key-secret
   - aliyun.sts.role-arn
   ↓
5. 返回STS临时凭证
```

### 问题根源

**STSService直接从配置文件读取阿里云密钥**，而不是从数据库的bucket_config表读取！

```java
// ❌ 当前实现 - STSService.java
@Value("${aliyun.sts.access-key-id}")
private String accessKeyId;

@Value("${aliyun.sts.access-key-secret}")
private String accessKeySecret;

@Value("${aliyun.sts.role-arn}")
private String roleArn;
```

**应该改为**：从bucket_config表读取加密的密钥！

```java
// ✅ 应该这样做
// 1. 从数据库查询bucket_config
// 2. 解密access_key_id和access_key_secret
// 3. 解密sts_role_arn（如果有）
```

---

## 🔧 需要调整的内容

### 1. BucketConfig实体类调整

#### 问题1：字段重复

**当前实体类有重复的配置字段**：

```java
// 基础配置字段（数据库中有）
private String lifecycleConfig;      // 行151
private String corsConfig;           // 行156
private String refererConfig;        // 行161
private String bucketPolicy;         // 行166
private String replicationConfig;    // 行171
private String accessMonitorConfig;  // 行176
private String inventoryConfig;      // 行181
private String websiteConfig;        // 行186
private String loggingConfig;        // 行191
private String wormConfig;           // 行196

// OSS同步配置字段（数据库中可能没有）
private String configAcl;            // 行318 ❓ 数据库没有？
private String configPolicy;         // 行323 ❓ 数据库没有？
private String configReferer;        // 行328 ❓ 数据库没有？
private String configCors;           // 行333 ❓ 数据库没有？
private String configLifecycle;     // 行338 ❓ 数据库没有？
private String configVersioning;     // 行343 ❓ 数据库没有？
private String configLogging;        // 行348 ❓ 数据库没有？
private String configWebsite;        // 行353 ❓ 数据库没有？
private String configTransferAcceleration; // 行358 ❓ 数据库没有？
private String configWorm;           // 行363 ❓ 数据库没有？

// 其他不在数据库中的字段
private Boolean isActive;            // 行308 ❓ 数据库没有？
private LocalDateTime syncTime;      // 行313 ❓ 数据库没有？
```

**解决方案**：删除或注释掉不在数据库中的字段

---

### 2. BucketConfigMapper.xml调整

#### 当前映射缺少高级配置字段

**当前XML只映射了基础字段**，缺少：

```xml
<!-- ❌ 当前缺少以下字段的映射 -->
lifecycle_config
cors_config
referer_config
bucket_policy
replication_config
access_monitor_config
inventory_config
website_config
logging_config
worm_config
```

**需要添加到Base_Column_List和ResultMap中**

---

### 3. STSService完全重写

#### 当前实现的问题

```java
// ❌ 问题1: 从配置文件读取密钥
@Value("${aliyun.sts.access-key-id}")
private String accessKeyId;

// ❌ 问题2: 没有使用数据库中的bucket_config
// ❌ 问题3: 没有解密API密钥
// ❌ 问题4: 没有根据不同Bucket使用不同的密钥
```

#### 应该改为

```java
// ✅ 正确实现
@Service
public class STSService {

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Value("${duda.file.encryption.key}")
    private String encryptionKey;

    public STSCredentialsDTO generateSTSCredentials(String bucketName, String objectPrefix, Long durationSeconds) {
        // 1. 从数据库查询Bucket配置
        BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);

        // 2. 解密API密钥
        String accessKeyId = AesUtil.decrypt(bucketConfig.getAccessKeyId(), encryptionKey);
        String accessKeySecret = AesUtil.decrypt(bucketConfig.getAccessKeySecret(), encryptionKey);
        String region = bucketConfig.getRegion();

        // 3. 使用解密后的密钥调用STS API
        // ...
    }
}
```

---

## 📝 详细调整方案

### 步骤1: 调整BucketConfig实体类

```java
// 文件: BucketConfig.java

// 1. ✅ 保留数据库中存在的字段
private String lifecycleConfig;
private String corsConfig;
private String refererConfig;
private String bucketPolicy;
private String replicationConfig;
private String accessMonitorConfig;
private String inventoryConfig;
private String websiteConfig;
private String loggingConfig;
private String wormConfig;

// 2. ❌ 删除或注释掉数据库中没有的字段
// private Boolean isActive;
// private LocalDateTime syncTime;
// private String configAcl;
// private String configPolicy;
// private String configReferer;
// private String configCors;
// private String configLifecycle;
// private String configVersioning;
// private String configLogging;
// private String configWebsite;
// private String configTransferAcceleration;
// private String configWorm;
```

**修改后的实体类字段清单**：

```java
// ==================== 基础字段 ====================
private Long id;
private String bucketName;
private String bucketDisplayName;
private String storageType;
private String region;
private Long userId;
private String userType;
private Long tenantId;
private String storageClass;
private String dataRedundancyType;
private String aclType;
private Long maxFileSize;
private Integer maxFileCount;
private Long maxStorageSize;
private String allowedFileTypes;
private String blockedFileTypes;
private String domainName;
private Boolean cdnEnabled;
private String cdnDomain;
private Boolean versioningEnabled;
private Boolean corsEnabled;
private Boolean watermarkEnabled;
private Boolean encryptionEnabled;
private Boolean lifecycleEnabled;
private Boolean transferAccelerationEnabled;
private Integer currentFileCount;
private Long currentStorageSize;
private Long storageUsedQuota;
private String accessKeyId;              // AES加密
private String accessKeySecret;          // AES加密
private String secretKey;                // AES加密
private String endpoint;
private String stsRoleArn;              // STS角色ARN
private String stsExternalId;           // STS外部ID
private String status;
private Boolean autoRenewEnabled;
private String tags;                     // JSON类型
private String category;
private String description;
private Long createdBy;
private LocalDateTime createdTime;
private Long updatedBy;
private LocalDateTime updatedTime;
private LocalDateTime deletedTime;
private Boolean isDeleted;

// ==================== 高级配置字段（JSON格式） ====================
private String lifecycleConfig;
private String corsConfig;
private String refererConfig;
private String bucketPolicy;
private String replicationConfig;
private String accessMonitorConfig;
private String inventoryConfig;
private String websiteConfig;
private String loggingConfig;
private String wormConfig;
```

---

### 步骤2: 调整BucketConfigMapper.xml

```xml
<!-- 文件: BucketConfigMapper.xml -->

<resultMap id="BaseResultMap" type="com.duda.file.provider.entity.BucketConfig">
    <!-- 原有字段映射保持不变 -->

    <!-- ✅ 新增：高级配置字段映射 -->
    <result column="lifecycle_config" property="lifecycleConfig" jdbcType="VARCHAR"/>
    <result column="cors_config" property="corsConfig" jdbcType="VARCHAR"/>
    <result column="referer_config" property="refererConfig" jdbcType="VARCHAR"/>
    <result column="bucket_policy" property="bucketPolicy" jdbcType="LONGVARCHAR"/>
    <result column="replication_config" property="replicationConfig" jdbcType="VARCHAR"/>
    <result column="access_monitor_config" property="accessMonitorConfig" jdbcType="VARCHAR"/>
    <result column="inventory_config" property="inventoryConfig" jdbcType="VARCHAR"/>
    <result column="website_config" property="websiteConfig" jdbcType="VARCHAR"/>
    <result column="logging_config" property="loggingConfig" jdbcType="VARCHAR"/>
    <result column="worm_config" property="wormConfig" jdbcType="VARCHAR"/>
</resultMap>

<sql id="Base_Column_List">
    <!-- 原有字段 -->
    id, bucket_name, bucket_display_name, storage_type, region, user_id, user_type, tenant_id,
    storage_class, data_redundancy_type, acl_type, max_file_size, max_file_count, max_storage_size,
    allowed_file_types, blocked_file_types, domain_name, cdn_enabled, cdn_domain,
    versioning_enabled, cors_enabled, watermark_enabled, encryption_enabled, lifecycle_enabled,
    transfer_acceleration_enabled,
    current_file_count, current_storage_size, storage_used_quota, access_key_id, access_key_secret,
    secret_key, endpoint, sts_role_arn, sts_external_id, status, auto_renew_enabled, tags,
    category, description, created_by, created_time, updated_by, updated_time, deleted_time, is_deleted,

    <!-- ✅ 新增：高级配置字段 -->
    lifecycle_config, cors_config, referer_config, bucket_policy, replication_config,
    access_monitor_config, inventory_config, website_config, logging_config, worm_config
</sql>
```

---

### 步骤3: 完全重写STSService

```java
// 文件: STSService.java

package com.duda.file.provider.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.duda.file.dto.upload.STSCredentialsDTO;
import com.duda.file.provider.entity.BucketConfig;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.common.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * 阿里云STS临时凭证服务（重构版）
 *
 * @author duda
 * @date 2026-03-17
 */
@Slf4j
@Service
public class STSService {

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    /**
     * API密钥加密密钥(从Nacos配置中心读取)
     */
    @Value("${duda.file.encryption.key}")
    private String encryptionKey;

    /**
     * STS默认过期时间(秒)
     */
    @Value("${aliyun.sts.default-duration:3600}")
    private Long defaultDuration;

    /**
     * 生成STS临时凭证（从数据库读取配置）
     *
     * @param bucketName 存储空间名称
     * @param objectPrefix 对象前缀（可选，用于限制权限范围）
     * @param durationSeconds 过期时间（秒）
     * @return STS临时凭证
     */
    public STSCredentialsDTO generateSTSCredentials(String bucketName, String objectPrefix, Long durationSeconds) {
        log.info("Generating STS credentials for bucket: {}, prefix: {}", bucketName, objectPrefix);

        // 1. 从数据库查询Bucket配置
        BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
        if (bucketConfig == null) {
            throw new RuntimeException("Bucket not found: " + bucketName);
        }

        // 2. 检查Bucket状态
        if (bucketConfig.getIsDeleted()) {
            throw new RuntimeException("Bucket has been deleted: " + bucketName);
        }
        if (!"ACTIVE".equals(bucketConfig.getStatus())) {
            throw new RuntimeException("Bucket is not active: " + bucketName);
        }

        // 3. 解密API密钥
        String accessKeyId;
        String accessKeySecret;
        String stsRoleArn = null;
        String region;

        try {
            accessKeyId = AesUtil.decrypt(bucketConfig.getAccessKeyId(), encryptionKey);
            accessKeySecret = AesUtil.decrypt(bucketConfig.getAccessKeySecret(), encryptionKey);
            region = bucketConfig.getRegion();
            stsRoleArn = bucketConfig.getStsRoleArn(); // 可能是null

            log.debug("Decrypted API key for bucket: {}, region: {}", bucketName, region);

        } catch (Exception e) {
            log.error("Failed to decrypt API key for bucket: {}", bucketName, e);
            throw new RuntimeException("Failed to decrypt API key for bucket: " + bucketName);
        }

        // 4. 检查是否配置了STS Role ARN
        if (stsRoleArn == null || stsRoleArn.isEmpty()) {
            log.warn("STS Role ARN not configured for bucket: {}, will use access key directly", bucketName);
            // TODO: 如果没有配置STS Role，可以考虑直接返回AccessKey（不推荐生产环境）
            throw new RuntimeException("STS Role ARN not configured for bucket: " + bucketName);
        }

        // 5. 调用STS API生成临时凭证
        DefaultAcsClient client = null;
        try {
            // 创建STS客户端配置
            DefaultProfile profile = DefaultProfile.getProfile(
                region,
                accessKeyId,
                accessKeySecret
            );

            // 创建STS客户端
            client = new DefaultAcsClient(profile);

            // 构建权限策略
            String policy = buildBucketPolicy(bucketName, objectPrefix);

            // 创建AssumeRole请求
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setRoleArn(stsRoleArn);
            request.setRoleSessionName("duda-file-upload-session");
            request.setPolicy(policy);
            request.setDurationSeconds(durationSeconds != null ? durationSeconds : defaultDuration);
            request.setMethod(com.aliyuncs.http.MethodType.POST);

            // 调用STS API获取临时凭证
            AssumeRoleResponse response = client.getAcsResponse(request);

            // 提取临时凭证信息
            AssumeRoleResponse.Credentials credentials = response.getCredentials();

            // 解析过期时间(阿里云STS返回的格式为: 2025-03-14T12:00:00Z)
            String expirationStr = credentials.getExpiration();
            ZonedDateTime zdt = ZonedDateTime.parse(expirationStr);
            LocalDateTime expiration = zdt.withZoneSameInstant(ZoneOffset.systemDefault()).toLocalDateTime();

            // 计算剩余有效时间(秒)
            long remainingSeconds = java.time.Duration.between(
                LocalDateTime.now(),
                expiration
            ).getSeconds();

            // 构建返回DTO
            return STSCredentialsDTO.builder()
                .accessKeyId(credentials.getAccessKeyId())
                .accessKeySecret(credentials.getAccessKeySecret())
                .securityToken(credentials.getSecurityToken())
                .expiration(expiration)
                .durationSeconds(remainingSeconds)
                .roleArn(stsRoleArn)
                .roleSessionName("duda-file-upload-session")
                .policy(policy)
                .build();

        } catch (ClientException e) {
            log.error("ClientException: Failed to generate STS credentials for bucket: {}, prefix: {}, errorCode: {}, errorMsg: {}",
                bucketName, objectPrefix, e.getErrCode(), e.getErrMsg(), e);
            log.error("诊断地址: {}", e.getData() != null ? e.getData().get("Recommend") : "N/A");
            throw new RuntimeException("Failed to generate STS credentials: " + e.getErrMsg(), e);
        } catch (Exception e) {
            log.error("Failed to generate STS credentials for bucket: {}, prefix: {}",
                bucketName, objectPrefix, e);
            throw new RuntimeException("Failed to generate STS credentials: " + e.getMessage(), e);
        } finally {
            if (client != null) {
                try {
                    client.shutdown();
                } catch (Exception e) {
                    log.warn("Failed to close STS client", e);
                }
            }
        }
    }

    /**
     * 构建Bucket访问权限策略
     *
     * @param bucketName 存储空间名称
     * @param objectPrefix 对象前缀（可选，用于限制权限范围）
     * @return JSON格式的权限策略
     */
    private String buildBucketPolicy(String bucketName, String objectPrefix) {
        StringBuilder policy = new StringBuilder();
        policy.append("{\n");
        policy.append("  \"Version\": \"1\",\n");
        policy.append("  \"Statement\": [\n");

        // 允许PutObject（上传文件）
        policy.append("    {\n");
        policy.append("      \"Effect\": \"Allow\",\n");
        policy.append("      \"Action\": [\n");
        policy.append("        \"oss:PutObject\"\n");
        policy.append("      ],\n");
        policy.append("      \"Resource\": [\n");

        if (objectPrefix != null && !objectPrefix.isEmpty()) {
            // 限制特定前缀的权限
            policy.append("        \"acs:oss:*:*:").append(bucketName).append("/").append(objectPrefix).append("*\"\n");
        } else {
            // 整个Bucket的权限
            policy.append("        \"acs:oss:*:*:").append(bucketName).append("/*\"\n");
        }

        policy.append("      ]\n");
        policy.append("    }\n");

        policy.append("  ]\n");
        policy.append("}");

        String policyStr = policy.toString();
        log.debug("Generated STS policy: {}", policyStr);
        return policyStr;
    }
}
```

---

### 步骤4: 调整UploadServiceImpl中的validateAndGetBucket方法

```java
// 文件: UploadServiceImpl.java

private BucketConfig validateAndGetBucket(Long userId, String bucketName) {
    BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);

    if (bucketConfig == null) {
        throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
    }

    // ✅ 新增：检查软删除标记
    if (bucketConfig.getIsDeleted() != null && bucketConfig.getIsDeleted()) {
        throw new StorageException("BUCKET_DELETED", "Bucket has been deleted: " + bucketName);
    }

    // ✅ 新增：检查Bucket状态
    if (!"ACTIVE".equals(bucketConfig.getStatus())) {
        throw new StorageException("BUCKET_INACTIVE",
            "Bucket is not active, current status: " + bucketConfig.getStatus());
    }

    // ✅ 新增：检查用户权限
    if (!bucketConfig.getUserId().equals(userId)) {
        throw new StorageException("PERMISSION_DENIED",
            "No permission to access bucket: " + bucketName);
    }

    // ✅ 新增：检查API密钥是否存在
    if (bucketConfig.getAccessKeyId() == null || bucketConfig.getAccessKeyId().isEmpty()) {
        throw new StorageException("API_KEY_MISSING",
            "Access Key ID not configured for bucket: " + bucketName);
    }

    if (bucketConfig.getAccessKeySecret() == null || bucketConfig.getAccessKeySecret().isEmpty()) {
        throw new StorageException("API_KEY_MISSING",
            "Access Key Secret not configured for bucket: " + bucketName);
    }

    return bucketConfig;
}
```

---

### 步骤5: 更新GetSTSReqDTO（如果需要）

根据最新的数据表结构，可能需要调整GetSTSReqDTO：

```java
// 文件: GetSTSReqDTO.java

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSTSReqDTO implements Serializable {

    /**
     * 存储空间名称（必填）
     */
    private String bucketName;

    /**
     * 对象键前缀（可选，用于限制临时凭证的访问范围）
     */
    private String objectPrefix;

    /**
     * 有效期(秒) 范围: 900-3600 (15分钟-1小时) 默认: 3600
     */
    private Long durationSeconds;

    /**
     * 权限类型 - ReadOnly: 只读 - ReadWrite: 读写 - FullControl: 完全控制
     */
    private String permissionType;

    /**
     * 用户ID（必填）
     */
    private Long userId;

    /**
     * 是否强制使用HTTPS
     */
    private Boolean httpsOnly;

    /**
     * 验证请求参数
     */
    public boolean validate() {
        if (bucketName == null || bucketName.isEmpty()) {
            return false;
        }
        if (userId == null) {
            return false;
        }
        if (durationSeconds != null && (durationSeconds < 900 || durationSeconds > 3600)) {
            return false;
        }
        return true;
    }

    /**
     * 构建默认请求(读写权限,1小时有效期)
     */
    public static GetSTSReqDTO buildDefault(String bucketName, Long userId) {
        return GetSTSReqDTO.builder()
                .bucketName(bucketName)
                .userId(userId)
                .durationSeconds(3600L)
                .permissionType("ReadWrite")
                .httpsOnly(true)
                .build();
    }
}
```

---

## 🎯 总结：需要修改的文件清单

### 必须修改（P0）

1. ✅ **BucketConfig.java** - 删除不在数据库中的字段
2. ✅ **BucketConfigMapper.xml** - 添加高级配置字段的映射
3. ✅ **STSService.java** - 完全重写，从数据库读取配置
4. ✅ **UploadServiceImpl.java** - 增强validateAndGetBucket方法的验证逻辑

### 可选修改（P1）

5. ⚠️ **GetSTSReqDTO.java** - 简化字段，删除不需要的字段
6. ⚠️ **STSCredentialsDTO.java** - 检查字段是否合理

---

## ✅ 预期效果

修改后，STS流程将变为：

```
1. 用户请求STS凭证 → getSTSForClientUpload(GetSTSReqDTO)
   ↓
2. 查询数据库 → bucketConfigMapper.selectByBucketName(bucketName)
   ↓
3. 验证Bucket状态和权限 → validateAndGetBucket()
   ↓
4. 解密API密钥 → AesUtil.decrypt(accessKeyId, encryptionKey)
   ↓
5. 调用阿里云STS API → 使用解密后的密钥
   ↓
6. 返回临时凭证 → STSCredentialsDTO
```

**关键改进**：
- ✅ 每个Bucket使用自己的API密钥（从数据库读取）
- ✅ API密钥加密存储（AES-256-GCM）
- ✅ 支持多用户、多租户
- ✅ 密钥与Bucket配置绑定，便于管理

---

需要我立即执行这些修改吗？
