package com.duda.file.provider.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.duda.file.dto.upload.STSCredentialsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * 阿里云STS临时凭证服务
 *
 * @author duda
 * @date 2025-03-14
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

    @Value("${aliyun.sts.region:cn-hangzhou}")
    private String region;

    @Value("${aliyun.sts.default-duration:3600}")
    private Long defaultDuration;

    /**
     * 生成STS临时凭证
     *
     * @param bucketName 存储空间名称
     * @param objectPrefix 对象前缀（可选，用于限制权限范围）
     * @param durationSeconds 过期时间（秒）
     * @return STS临时凭证
     */
    public STSCredentialsDTO generateSTSCredentials(String bucketName, String objectPrefix, Long durationSeconds) {
        log.info("Generating STS credentials for bucket: {}, prefix: {}", bucketName, objectPrefix);

        DefaultAcsClient client = null;
        try {
            // 1. 创建STS客户端配置
            DefaultProfile profile = DefaultProfile.getProfile(
                region,
                accessKeyId,
                accessKeySecret
            );

            // 2. 创建STS客户端
            client = new DefaultAcsClient(profile);

            // 3. 构建权限策略
            String policy = buildBucketPolicy(bucketName, objectPrefix);

            // 4. 创建AssumeRole请求
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setRoleArn(roleArn);
            request.setRoleSessionName("duda-file-upload");
            request.setPolicy(policy);
            request.setDurationSeconds(durationSeconds != null ? durationSeconds : defaultDuration);
            request.setMethod(com.aliyuncs.http.MethodType.POST);

            // 5. 调用STS API获取临时凭证
            AssumeRoleResponse response = client.getAcsResponse(request);

            // 6. 提取临时凭证信息
            AssumeRoleResponse.Credentials credentials = response.getCredentials();

            // 7. 解析过期时间(阿里云STS返回的格式为: 2025-03-14T12:00:00Z)
            String expirationStr = credentials.getExpiration();
            ZonedDateTime zdt = ZonedDateTime.parse(expirationStr);
            LocalDateTime expiration = zdt.withZoneSameInstant(ZoneOffset.systemDefault()).toLocalDateTime();

            // 8. 计算剩余有效时间(秒)
            long remainingSeconds = java.time.Duration.between(
                LocalDateTime.now(),
                expiration
            ).getSeconds();

            // 9. 构建返回DTO
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
