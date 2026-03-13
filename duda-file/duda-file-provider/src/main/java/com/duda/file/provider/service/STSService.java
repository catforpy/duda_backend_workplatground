package com.duda.file.provider.service;

import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.exceptions.ClientException;
import com.duda.file.dto.upload.STSCredentialsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 阿里云STS临时凭证服务
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
@Service
public class STSService {

    @Value("${aliyun.sts.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sts.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sts.role-arn}")
    private String roleArn;

    @Value("${aliyun.sts.endpoint:sts.cn-hangzhou.aliyuncs.com}")
    private String endpoint;

    @Value("${aliyun.sts.default-duration:3600}")
    private Long defaultDuration;

    /**
     * 生成STS临时凭证
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键（可选，用于限制权限范围）
     * @param durationSeconds 过期时间（秒）
     * @return STS临时凭证
     */
    public STSCredentialsDTO generateSTSCredentials(String bucketName, String objectKey, Long durationSeconds) {
        log.info("Generating STS credentials for bucket: {}, object: {}", bucketName, objectKey);

        try {
            // 1. 创建STS客户端配置
            DefaultProfile profile = DefaultProfile.getProfile(
                getRegionFromEndpoint(endpoint),
                accessKeyId,
                accessKeySecret
            );

            // 2. 创建STS客户端
            DefaultAcsClient client = new DefaultAcsClient(profile);

            // 3. 构建权限策略
            String policy = buildBucketPolicy(bucketName, objectKey);

            // 4. 创建AssumeRole请求
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setRoleArn(roleArn);
            request.setRoleSessionName("duda-file-upload");
            request.setPolicy(policy);
            request.setDurationSeconds(durationSeconds != null ? durationSeconds.intValue() : defaultDuration.intValue());
            request.setMethod(com.aliyuncs.http.MethodType.POST);

            // 5. 调用STS API获取临时凭证
            AssumeRoleResponse response = client.getAcsResponse(request);

            // 6. 提取临时凭证信息
            AssumeRoleResponse.Credentials credentials = response.getCredentials();

            // 7. 构建返回DTO
            // 解析过期时间(阿里云STS返回的格式为: 2025-03-14T12:00:00Z)
            String expirationStr = credentials.getExpiration();
            java.time.LocalDateTime expiration = java.time.LocalDateTime.parse(
                expirationStr.replace("Z", ""),
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            );

            // 计算剩余有效时间(秒)
            long remainingSeconds = java.time.Duration.between(
                java.time.LocalDateTime.now(),
                expiration
            ).getSeconds();

            return STSCredentialsDTO.builder()
                .accessKeyId(credentials.getAccessKeyId())
                .accessKeySecret(credentials.getAccessKeySecret())
                .securityToken(credentials.getSecurityToken())
                .expiration(expiration)
                .durationSeconds(remainingSeconds)
                .roleArn(roleArn)
                .roleSessionName("duda-file-upload")
                .policy(policy)
                .build();

        } catch (ClientException e) {
            log.error("Failed to generate STS credentials for bucket: {}, object: {}, errorCode: {}, errorMsg: {}",
                bucketName, objectKey, e.getErrCode(), e.getErrMsg(), e);
            throw new RuntimeException("Failed to generate STS credentials: " + e.getErrMsg(), e);
        } catch (Exception e) {
            log.error("Failed to generate STS credentials for bucket: {}, object: {}", bucketName, objectKey, e);
            throw new RuntimeException("Failed to generate STS credentials: " + e.getMessage(), e);
        }
    }

    /**
     * 构建Bucket访问权限策略
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键（可选，用于限制权限范围）
     * @return JSON格式的权限策略
     */
    private String buildBucketPolicy(String bucketName, String objectKey) {
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

        if (objectKey != null && !objectKey.isEmpty()) {
            // 限制特定对象的权限
            policy.append("        \"acs:oss:*:*:").append(bucketName).append("/").append(objectKey).append("\"\n");
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
     * 从endpoint获取region
     *
     * @param endpoint STS接入点
     * @return region
     */
    private String getRegionFromEndpoint(String endpoint) {
        // sts.cn-hangzhou.aliyuncs.com -> cn-hangzhou
        if (endpoint.contains(".")) {
            String[] parts = endpoint.split("\\.");
            if (parts.length >= 1) {
                return parts[0].replace("sts", "");
            }
        }
        return "cn-hangzhou"; // 默认
    }
}
