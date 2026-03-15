package com.duda.file.provider.service;

import com.aliyun.oss.common.utils.BinaryUtil;
import com.duda.file.dto.upload.OssPostSignatureDTO;
import com.duda.file.service.UploadService;
import com.duda.file.common.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * OSS POST签名服务
 *
 * <p>实现阿里云官方文档的POST签名方案</p>
 *
 * @author DudaNexus
 * @since 2025-03-15
 */
@Slf4j
@Service
public class OssPostSignatureService {

    @Value("${oss.region:cn-hangzhou}")
    private String region;

    @Value("${oss.access-key-id:}")
    private String accessKeyId;

    @Value("${oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${oss.sts-role-arn:}")
    private String stsRoleArn;

    @Value("${oss.upload-dir:uploads/}")
    private String uploadDir;

    @Value("${oss.expire-time:3600}")
    private Long expireTime;

    /**
     * 获取OSS POST签名
     *
     * @param bucketName Bucket名称
     * @return POST签名响应
     */
    public OssPostSignatureDTO getOssPostSignature(String bucketName) {
        try {
            log.info("开始生成OSS POST签名，bucket: {}, region: {}", bucketName, region);

            // 步骤1：获取STS临时凭证（如果配置了Role ARN）
            String securityToken = null;
            String actualAccessKeyId = accessKeyId;
            String actualAccessKeySecret = accessKeySecret;

            if (stsRoleArn != null && !stsRoleArn.isEmpty()) {
                log.info("使用STS Role获取临时凭证: {}", stsRoleArn);
                Map<String, String> credentials = getStsCredential();
                actualAccessKeyId = credentials.get("accessKeyId");
                actualAccessKeySecret = credentials.get("accessKeySecret");
                securityToken = credentials.get("securityToken");
                log.info("STS临时凭证获取成功");
            } else {
                log.info("使用AccessKey直接签名（未配置STS Role）");
            }

            // 步骤2：生成签名所需的时间参数
            ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String date = today.format(dateFormatter);

            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
            String xOssDate = now.format(dateTimeFormatter);

            // 步骤3：构造凭证字符串
            String xOssCredential = actualAccessKeyId + "/" + date + "/" + region + "/oss/aliyun_v4_request";

            // 步骤4：生成host
            String host = String.format("https://%s.oss-%s.aliyuncs.com", bucketName, region);

            // 步骤5：构造Policy
            String policy = buildPolicy(bucketName, actualAccessKeyId, xOssCredential, xOssDate, securityToken);
            String base64Policy = Base64.getEncoder().encodeToString(policy.getBytes(StandardCharsets.UTF_8));

            // 步骤6：计算签名
            String signature = calculateSignature(base64Policy, actualAccessKeySecret, date);

            // 步骤7：组装响应
            return OssPostSignatureDTO.builder()
                    .version("OSS4-HMAC-SHA256")
                    .policy(base64Policy)
                    .xOssCredential(xOssCredential)
                    .xOssDate(xOssDate)
                    .signature(signature)
                    .securityToken(securityToken)
                    .dir(uploadDir)
                    .host(host)
                    .expireTime(expireTime)
                    .build();

        } catch (Exception e) {
            log.error("生成OSS POST签名失败", e);
            throw new StorageException("生成签名失败: " + e.getMessage());
        }
    }

    /**
     * 获取STS临时凭证
     *
     * @return 凭证信息
     */
    private Map<String, String> getStsCredential() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint("sts." + region + ".aliyuncs.com");

        com.aliyun.sts20150401.Client client = new com.aliyun.sts20150401.Client(config);

        com.aliyun.sts20150401.models.AssumeRoleRequest assumeRoleRequest =
                new com.aliyun.sts20150401.models.AssumeRoleRequest()
                        .setRoleArn(stsRoleArn)
                        .setRoleSessionName("duda-file-upload-session")
                        .setDurationSeconds(expireTime);

        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();

        try {
            com.aliyun.sts20150401.models.AssumeRoleResponse response =
                    client.assumeRoleWithOptions(assumeRoleRequest, runtime);

            Map<String, String> credentials = new HashMap<>();
            credentials.put("accessKeyId", response.body.credentials.accessKeyId);
            credentials.put("accessKeySecret", response.body.credentials.accessKeySecret);
            credentials.put("securityToken", response.body.credentials.securityToken);

            log.info("STS AssumeRole成功");
            return credentials;

        } catch (com.aliyun.tea.TeaException error) {
            log.error("STS AssumeRole失败: {}", error.getMessage());
            log.error("诊断地址: {}", error.getData().get("Recommend"));
            throw new Exception("获取STS临时凭证失败: " + error.getMessage());
        }
    }

    /**
     * 构造上传策略
     *
     * @param bucketName Bucket名称
     * @param actualAccessKeyId 实际使用的AccessKey ID
     * @param xOssCredential OSS凭证字符串
     * @param xOssDate OSS日期时间
     * @param securityToken STS安全令牌
     * @return JSON格式的策略字符串
     */
    private String buildPolicy(String bucketName, String actualAccessKeyId, String xOssCredential,
                              String xOssDate, String securityToken) {
        Map<String, Object> policy = new HashMap<>();
        policy.put("expiration", generateExpiration(expireTime));

        List<Object> conditions = new ArrayList<>();

        // 限制bucket
        Map<String, String> bucketCondition = new HashMap<>();
        bucketCondition.put("bucket", bucketName);
        conditions.add(bucketCondition);

        // 如果使用STS，必须包含security-token
        if (securityToken != null) {
            Map<String, String> securityTokenCondition = new HashMap<>();
            securityTokenCondition.put("x-oss-security-token", securityToken);
            conditions.add(securityTokenCondition);
        }

        // 指定签名版本
        Map<String, String> signatureVersionCondition = new HashMap<>();
        signatureVersionCondition.put("x-oss-signature-version", "OSS4-HMAC-SHA256");
        conditions.add(signatureVersionCondition);

        // 指定凭证
        Map<String, String> credentialCondition = new HashMap<>();
        credentialCondition.put("x-oss-credential", xOssCredential);
        conditions.add(credentialCondition);

        // 指定日期
        Map<String, String> dateCondition = new HashMap<>();
        dateCondition.put("x-oss-date", xOssDate);
        conditions.add(dateCondition);

        // 限制文件大小（1B - 10MB）
        conditions.add(Arrays.asList("content-length-range", 1, 10240000));

        // 上传成功后返回状态码200
        conditions.add(Arrays.asList("eq", "$success_action_status", "200"));

        // 文件前缀
        conditions.add(Arrays.asList("starts-with", "$key", uploadDir));

        policy.put("conditions", conditions);

        return toJson(policy);
    }

    /**
     * 计算签名
     *
     * @param stringToSign 待签名字符串
     * @param accessKeySecret AccessKey Secret
     * @param date 日期字符串（yyyyMMdd）
     * @return 签名值（十六进制字符串）
     */
    private String calculateSignature(String stringToSign, String accessKeySecret, String date) {
        try {
            // 步骤1：计算Date Key
            byte[] dateKey = hmacsha256(("aliyun_v4" + accessKeySecret).getBytes(StandardCharsets.UTF_8), date);

            // 步骤2：计算DateRegion Key
            byte[] dateRegionKey = hmacsha256(dateKey, region);

            // 步骤3：计算DateRegionService Key
            byte[] dateRegionServiceKey = hmacsha256(dateRegionKey, "oss");

            // 步骤4：计算Signing Key
            byte[] signingKey = hmacsha256(dateRegionServiceKey, "aliyun_v4_request");

            // 步骤5：计算Signature
            byte[] result = hmacsha256(signingKey, stringToSign);

            // 转换为十六进制字符串
            return BinaryUtil.toHex(result);

        } catch (Exception e) {
            log.error("计算签名失败", e);
            throw new RuntimeException("计算签名失败", e);
        }
    }

    /**
     * HMAC-SHA256加密
     *
     * @param key 密钥
     * @param data 待加密数据
     * @return 加密结果
     */
    private byte[] hmacsha256(byte[] key, String data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256加密失败", e);
        }
    }

    /**
     * 生成过期时间
     *
     * @param seconds 有效时长（秒）
     * @return ISO8601格式的过期时间字符串
     */
    private String generateExpiration(long seconds) {
        long expirationTimestamp = Instant.now().getEpochSecond() + seconds;
        Instant instant = Instant.ofEpochSecond(expirationTimestamp);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return zonedDateTime.format(formatter);
    }

    /**
     * 简单的JSON序列化（用于生成Policy JSON）
     */
    private String toJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;

            json.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof List) {
                json.append("[");
                boolean firstItem = true;
                for (Object item : (List<?>) value) {
                    if (!firstItem) {
                        json.append(",");
                    }
                    firstItem = false;
                    if (item instanceof List) {
                        json.append("[");
                        boolean firstSubItem = true;
                        for (Object subItem : (List<?>) item) {
                            if (!firstSubItem) {
                                json.append(",");
                            }
                            firstSubItem = false;
                            json.append("\"").append(escapeJson(String.valueOf(subItem))).append("\"");
                        }
                        json.append("]");
                    } else if (item instanceof Map) {
                        json.append("{");
                        boolean firstMapEntry = true;
                        for (Map.Entry<?, ?> mapEntry : ((Map<?, ?>) item).entrySet()) {
                            if (!firstMapEntry) {
                                json.append(",");
                            }
                            firstMapEntry = false;
                            json.append("\"").append(mapEntry.getKey()).append("\":");
                            json.append("\"").append(escapeJson(String.valueOf(mapEntry.getValue()))).append("\"");
                        }
                        json.append("}");
                    } else {
                        json.append("\"").append(escapeJson(String.valueOf(item))).append("\"");
                    }
                }
                json.append("]");
            }
        }

        json.append("}");
        return json.toString();
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
