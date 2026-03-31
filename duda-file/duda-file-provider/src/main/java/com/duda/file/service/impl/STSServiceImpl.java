package com.duda.file.service.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.duda.file.common.util.AesUtil;
import com.duda.file.dto.upload.STSCredentialsDTO;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.service.STSService;
import com.duda.user.dto.userapikey.UserApiKeyDTO;
import com.duda.user.rpc.IUserApiKeyRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * STS服务实现
 * 阿里云STS临时凭证服务实现类
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@Service
public class STSServiceImpl implements STSService {

    @DubboReference(
        version = "1.0.0",
        group = "USER_GROUP",
        registry = "userRegistry",
        check = false
    )
    private IUserApiKeyRpc userApiKeyRpc;

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Value("${duda.file.encryption.key:duda-file-encryption-key}")
    private String encryptionKey;

    @Value("${aliyun.sts.default-duration:3600}")
    private Long defaultDuration;

    /**
     * OSS配置（从bootstrap.yml读取，作为RPC失败时的fallback）
     */
    @Value("${oss.access-key-id:}")
    private String ossAccessKeyId;

    @Value("${oss.access-key-secret:}")
    private String ossAccessKeySecret;

    @Value("${oss.region:cn-hangzhou}")
    private String ossRegion;

    @Value("${oss.sts-role-arn:}")
    private String ossStsRoleArn;

    @Override
    public STSCredentialsDTO generateSTSCredentials(String bucketName, String objectPrefix, Long durationSeconds, Long userId) {
        log.info("Service: Generating STS credentials for bucket: {}, prefix: {}, userId: {}", bucketName, objectPrefix, userId);

        try {
            // ✅ 1. 通过 bucket-config.api_key_id 查询 user-api-keys（和上传逻辑一致）
            // 先获取 bucket 配置
            com.duda.file.provider.entity.BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new RuntimeException("Bucket not found: " + bucketName);
            }

            log.info("获取到Bucket配置: bucketName={}, apiKeyId={}", bucketName, bucketConfig.getApiKeyId());

            // 变量存储API密钥信息
            String accessKeyId;
            String accessKeySecret;
            String region;
            String stsRoleArn;

            try {
                // 2. 通过 api_key_id 查询 API Key
                com.duda.user.dto.userapikey.UserApiKeyDTO apiKeyDTO =
                    userApiKeyRpc.getUserApiKeyById(bucketConfig.getApiKeyId());

                if (apiKeyDTO != null) {
                    // ✅ 验证 API Key 是否属于该用户（权限验证）
                    if (!apiKeyDTO.getUserId().equals(userId)) {
                        throw new RuntimeException("权限拒绝！API Key (id=" + bucketConfig.getApiKeyId() +
                            ") 属于用户 " + apiKeyDTO.getUserId() +
                            "，但请求来自用户 " + userId);
                    }

                    log.info("获取到API密钥: keyName={}, keyType={}, region={}, userId={}",
                        apiKeyDTO.getKeyName(), apiKeyDTO.getKeyType(), apiKeyDTO.getRegion(), userId);

                    // 3. 检查密钥类型
                    if (!"aliyun_oss".equalsIgnoreCase(apiKeyDTO.getKeyType())) {
                        throw new RuntimeException("密钥类型不支持,仅支持aliyun_oss: " + apiKeyDTO.getKeyType());
                    }

                    // 4. 检查是否有STS Role ARN
                    if (apiKeyDTO.getStsRoleArn() == null || apiKeyDTO.getStsRoleArn().isEmpty()) {
                        throw new RuntimeException("API密钥未配置STS Role ARN: " + apiKeyDTO.getKeyName() +
                            "\n请先在阿里云 RAM 中创建 STS 角色，然后在 user-api-keys 表中配置 sts_role_arn 字段");
                    }

                    // 5. ✅ 使用明文 API Key（RPC 返回的已解密）
                    accessKeyId = apiKeyDTO.getPlainAccessKeyId();
                    accessKeySecret = apiKeyDTO.getPlainAccessKeySecret();
                    region = apiKeyDTO.getRegion();
                    stsRoleArn = apiKeyDTO.getStsRoleArn();

                    log.info("✓✓✓ 通过RPC获取API密钥成功: accessKeyId={}, region={}, stsRoleArn={}",
                            accessKeyId.substring(0, 8) + "****", region, stsRoleArn);
                } else {
                    throw new RuntimeException("API密钥不存在，keyId: " + bucketConfig.getApiKeyId());
                }
            } catch (Exception e) {
                log.warn("⚠️ RPC调用获取API密钥失败，使用fallback配置: keyId={}, error={}",
                    bucketConfig.getApiKeyId(), e.getMessage());

                // Fallback: 使用bootstrap.yml中的OSS配置
                if (ossAccessKeyId == null || ossAccessKeyId.isEmpty() ||
                    ossAccessKeySecret == null || ossAccessKeySecret.isEmpty()) {
                    throw new RuntimeException("OSS配置未找到，且RPC调用失败，请检查配置");
                }

                accessKeyId = ossAccessKeyId;
                accessKeySecret = ossAccessKeySecret;
                region = ossRegion;
                stsRoleArn = ossStsRoleArn;

                if (stsRoleArn == null || stsRoleArn.isEmpty()) {
                    throw new RuntimeException("STS Role ARN未配置，请在bootstrap.yml中配置oss.sts-role-arn");
                }

                log.info("✓✓✓ 使用fallback OSS配置: region={}, stsRoleArn={}", region, stsRoleArn);
            }

            // 6. 创建STS客户端配置
            DefaultProfile profile = DefaultProfile.getProfile(
                region,
                accessKeyId,
                accessKeySecret
            );

            // 7. 创建STS客户端
            DefaultAcsClient client = new DefaultAcsClient(profile);

            // 8. 构建权限策略
            String policy = buildBucketPolicy(bucketName, objectPrefix);

            // 9. 创建AssumeRole请求
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setRoleArn(stsRoleArn);
            request.setRoleSessionName("duda-file-upload-" + bucketName);
            request.setPolicy(policy);
            request.setDurationSeconds(durationSeconds != null ? durationSeconds : defaultDuration);
            request.setMethod(com.aliyuncs.http.MethodType.POST);

            // 10. 调用STS API获取临时凭证
            AssumeRoleResponse response = client.getAcsResponse(request);

            // 11. 提取临时凭证信息
            AssumeRoleResponse.Credentials credentials = response.getCredentials();

            // 12. 解析过期时间(阿里云STS返回的格式为: 2025-03-14T12:00:00Z)
            String expirationStr = credentials.getExpiration();
            ZonedDateTime zdt = ZonedDateTime.parse(expirationStr);
            LocalDateTime expiration = zdt.withZoneSameInstant(ZoneOffset.systemDefault()).toLocalDateTime();

            // 13. 计算剩余有效时间(秒)
            long remainingSeconds = java.time.Duration.between(
                LocalDateTime.now(),
                expiration
            ).getSeconds();

            log.info("✓✓✓ STS临时凭证生成成功,过期时间: {}, 剩余有效时间: {}秒", expiration, remainingSeconds);

            // 14. 构建返回DTO
            return STSCredentialsDTO.builder()
                .accessKeyId(credentials.getAccessKeyId())
                .accessKeySecret(credentials.getAccessKeySecret())
                .securityToken(credentials.getSecurityToken())
                .expiration(expiration)
                .durationSeconds(remainingSeconds)
                .roleArn(stsRoleArn)
                .roleSessionName("duda-file-upload-" + bucketName)
                .policy(policy)
                .build();

        } catch (ClientException e) {
            log.error("ClientException: Failed to generate STS credentials for bucket: {}, prefix: {}, errorCode: {}, errorMsg: {}",
                bucketName, objectPrefix, e.getErrCode(), e.getErrMsg(), e);
            throw new RuntimeException("Failed to generate STS credentials: " + e.getErrMsg(), e);
        } catch (Exception e) {
            log.error("Failed to generate STS credentials for bucket: {}, prefix: {}",
                bucketName, objectPrefix, e);
            throw new RuntimeException("Failed to generate STS credentials: " + e.getMessage(), e);
        }
    }

    /**
     * 解密API密钥
     * 使用common模块中的AesUtil进行解密
     *
     * @param encryptedKey 加密的密钥
     * @return 解密后的明文密钥
     */
    private String decryptApiKey(String encryptedKey) {
        if (encryptedKey == null || encryptedKey.isEmpty()) {
            return "";
        }
        try {
            String decrypted = AesUtil.decrypt(encryptedKey, encryptionKey);
            log.debug("密钥解密成功,加密密钥长度: {}, 解密后长度: {}",
                encryptedKey.length(), decrypted.length());
            return decrypted;
        } catch (Exception e) {
            log.error("解密API密钥失败", e);
            throw new RuntimeException("解密API密钥失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建Bucket访问权限策略
     *
     * @param bucketName 存储空间名称
     * @param objectPrefix 对象前缀(可选,用于限制权限范围)
     * @return JSON格式的权限策略
     */
    private String buildBucketPolicy(String bucketName, String objectPrefix) {
        StringBuilder policy = new StringBuilder();
        policy.append("{\n");
        policy.append("  \"Version\": \"1\",\n");
        policy.append("  \"Statement\": [\n");

        // 允许PutObject(上传文件)
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

    /**
     * 掩码AccessKeyId(显示前8个字符)
     */
    private String maskAccessKey(String accessKeyId) {
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            return "";
        }
        int showLength = Math.min(8, accessKeyId.length());
        return accessKeyId.substring(0, showLength) + "***";
    }

    /**
     * 掩码AccessKeySecret(仅显示长度)
     */
    private String maskAccessKeySecret(String accessKeySecret) {
        if (accessKeySecret == null || accessKeySecret.isEmpty()) {
            return "";
        }
        return "******(长度:" + accessKeySecret.length() + ")******";
    }
}
