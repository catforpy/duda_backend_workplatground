package com.duda.file.api.controller;

import com.duda.file.dto.upload.OssPostSignatureDTO;
import com.duda.file.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OSS POST签名直传控制器
 *
 * <p>通过Dubbo调用provider服务实现POST签名功能
 *
 * @author DudaNexus
 * @since 2025-03-15
 */
@Slf4j
@RestController
@RequestMapping("/api/oss/upload")
@Tag(name = "OSS上传签名", description = "生成POST签名用于表单直传")
public class OssPostSignatureController {

    @DubboReference(
            version = "1.0.0",
            group = "DUDA_FILE_GROUP",
            check = false
    )
    private UploadService uploadService;

    /**
     * 获取POST签名用于OSS表单直传
     *
     * <p>此接口返回生成POST签名所需的所有参数，包括：
     * <ul>
     *   <li>version: 签名版本（OSS4-HMAC-SHA256）</li>
     *   <li>policy: 上传策略（Base64编码）</li>
     *   <li>x_oss_credential: 凭证字符串</li>
     *   <li>x_oss_date: 签名时间</li>
     *   <li>signature: 签名值</li>
     *   <li>security_token: STS临时令牌（如果使用STS）</li>
     *   <li>dir: 上传目录前缀</li>
     *   <li>host: OSS服务地址</li>
     * </ul>
     *
     * @param bucketName Bucket名称（可选，默认使用配置的bucket）
     * @return POST签名响应
     */
    @GetMapping("/post-signature")
    @Operation(summary = "获取POST签名", description = "获取OSS表单直传所需的签名参数")
    public ResponseEntity<OssPostSignatureDTO> getPostSignature(
            @Parameter(description = "Bucket名称，不传则使用默认配置")
            @RequestParam(required = false) String bucketName) {
        try {
            log.info("开始获取OSS POST签名，bucket: {}", bucketName);

            // 如果没有指定bucket，使用默认的bucket
            if (bucketName == null || bucketName.isEmpty()) {
                bucketName = "duda-java-backend-test"; // 默认bucket
            }

            // 调用provider服务获取签名
            OssPostSignatureDTO signature = uploadService.getOssPostSignature(bucketName);

            log.info("OSS POST签名获取成功");
            return ResponseEntity.ok(signature);

        } catch (Exception e) {
            log.error("获取OSS POST签名失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
