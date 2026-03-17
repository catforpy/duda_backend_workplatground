package com.duda.file.provider.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.duda.file.dto.upload.STSCredentialsDTO;
import com.duda.file.provider.entity.BucketConfig;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.provider.util.AesEncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * 阿里云STS临时凭证服务（从数据库读取密钥）
 *
 * @author duda
 * @date 2025-03-14
 */
@Slf4j
@Service
public class STSService {

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Autowired
    private AesEncryptUtil aesEncryptUtil;

    @Value("${aliyun.sts.default-duration:3600}")
    private Long defaultDuration;

    /**
     * 生成STS临时凭证（从数据库读取密钥）
     *
     * @param bucketName 存储空间名称
     * @param objectPrefix 对象前缀（可选，用于限制权限范围）
     * @param durationSeconds 过期时间（秒）
     * @return STS临时凭证
     */
    public STSCredentialsDTO generateSTSCredentials(String bucketName, String objectPrefix, Long durationSeconds) {
        log.info("Generating STS credentials for bucket: {}, prefix: {}", bucketName, objectPrefix);

        try {
            // 1. 从数据库查询Bucket配置
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                throw new RuntimeException("Bucket配置不存在: " + bucketName);
            }

            // 2. 检查Bucket状态
            if (!"ACTIVE".equalsIgnoreCase(bucketConfig.getStatus())) {
                throw new RuntimeException("Bucket状态异常: " + bucketConfig.getStatus());
            }

            // 3. 检查是否有STS Role ARN
            if (bucketConfig.getStsRoleArn() == null || bucketConfig.getStsRoleArn().isEmpty()) {
                throw new RuntimeException("Bucket未配置STS Role ARN: " + bucketName);
            }

            // 4. 从数据库解密密钥
            String accessKeyId = aesEncryptUtil.decrypt(bucketConfig.getAccessKeyId());
            String accessKeySecret = aesEncryptUtil.decrypt(bucketConfig.getAccessKeySecret());

            log.debug("Decrypted credentials for bucket: {}, accessKeyId: {}***", bucketName,
                accessKeyId.substring(0, Math.min(8, accessKeyId.length())));

            // 5. 创建STS客户端配置
            DefaultProfile profile = DefaultProfile.getProfile(
                bucketConfig.getRegion(),
                accessKeyId,
                accessKeySecret
            );

            // 6. 创建STS客户端
            DefaultAcsClient client = new DefaultAcsClient(profile);

            // 7. 构建权限策略
            String policy = buildBucketPolicy(bucketName, objectPrefix);

            // 8. 创建AssumeRole请求
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setRoleArn(bucketConfig.getStsRoleArn());
            request.setRoleSessionName("duda-file-upload-" + bucketName);
            request.setPolicy(policy);
            request.setDurationSeconds(durationSeconds != null ? durationSeconds : defaultDuration);
            request.setMethod(com.aliyuncs.http.MethodType.POST);

            // 如果配置了外部ID，添加到请求中
            if (bucketConfig.getStsExternalId() != null && !bucketConfig.getStsExternalId().isEmpty()) {
                request.setExternalId(bucketConfig.getStsExternalId());
            }

            // 9. 调用STS API获取临时凭证
            AssumeRoleResponse response = client.getAcsResponse(request);

            // 10. 提取临时凭证信息
            AssumeRoleResponse.Credentials credentials = response.getCredentials();

            // 11. 解析过期时间(阿里云STS返回的格式为: 2025-03-14T12:00:00Z)
            String expirationStr = credentials.getExpiration();
            ZonedDateTime zdt = ZonedDateTime.parse(expirationStr);
            LocalDateTime expiration = zdt.withZoneSameInstant(ZoneOffset.systemDefault()).toLocalDateTime();

            // 12. 计算剩余有效时间(秒)
            long remainingSeconds = java.time.Duration.between(
                LocalDateTime.now(),
                expiration
            ).getSeconds();

            // 13. 构建返回DTO
            return STSCredentialsDTO.builder()
                .accessKeyId(credentials.getAccessKeyId())
                .accessKeySecret(credentials.getAccessKeySecret())
                .securityToken(credentials.getSecurityToken())
                .expiration(expiration)
                .durationSeconds(remainingSeconds)
                .roleArn(bucketConfig.getStsRoleArn())
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
