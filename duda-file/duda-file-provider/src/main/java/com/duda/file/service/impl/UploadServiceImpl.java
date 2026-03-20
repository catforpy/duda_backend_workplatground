package com.duda.file.service.impl;

import com.duda.file.adapter.StorageService;
import com.duda.file.provider.helper.SimpleAdapterFactory;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import com.duda.file.dto.object.ObjectMetadataDTO;
import com.duda.file.dto.upload.*;
import com.duda.file.enums.StorageType;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.provider.mapper.ObjectMetadataMapper;
import com.duda.file.provider.mapper.UploadRecordMapper;
import com.duda.file.provider.mapper.FileAccessLogMapper;
import com.duda.file.provider.entity.BucketConfig;
import com.duda.file.provider.entity.ObjectMetadata;
import com.duda.file.provider.entity.UploadRecord;
import com.duda.file.provider.entity.FileAccessLog;
import com.duda.file.service.STSService;
import com.duda.file.service.UploadService;
import com.duda.file.common.exception.StorageException;
import com.duda.file.common.util.AesUtil;
import com.duda.user.rpc.IUserApiKeyRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 上传服务实现
 * 业务逻辑实现类
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private SimpleAdapterFactory storageAdapterFactory;

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Autowired
    private com.duda.file.provider.service.OssPostSignatureService ossPostSignatureService;

    @Autowired
    private ObjectMetadataMapper objectMetadataMapper;

    @Autowired
    private UploadRecordMapper uploadRecordMapper;

    @Autowired
    private FileAccessLogMapper fileAccessLogMapper;

    @DubboReference(
        version = "1.0.0",
        group = "USER_GROUP",
        registry = "userRegistry",
        check = false
    )
    private IUserApiKeyRpc userApiKeyRpc;

    @Autowired
    private STSService stsService;

    /**
     * API密钥加密密钥(从Nacos配置中心读取)
     */
    @Value("${duda.file.encryption.key:duda-file-encryption-key}")
    private String encryptionKey;

    @Override
    public UploadResultDTO simpleUpload(SimpleUploadReqDTO request) throws StorageException {
        log.info("Service: Simple upload: {}/{}", request.getBucketName(), request.getObjectKey());
        LocalDateTime startTime = LocalDateTime.now();
        String uploadStatus = "COMPLETED";
        String errorMessage = null;

        try {
            // 1. 验证Bucket是否存在
            BucketConfig bucketConfig = validateAndGetBucket(request.getUserId(), request.getBucketName());

            // 2. 获取存储适配器
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 3. 调用适配器上传
            UploadResultDTO result = adapter.uploadObject(
                    request.getBucketName(),
                    request.getObjectKey(),
                    request.getInputStream(),
                    request.getMetadata()
            );

            // 4. 保存对象元数据到数据库
            ObjectMetadata metadata = buildObjectMetadata(request, result, bucketConfig);
            objectMetadataMapper.insert(metadata);

            // 5. 记录上传日志
            saveUploadRecord(request, result, uploadStatus, errorMessage, startTime);

            // 6. 更新Bucket使用统计
            updateBucketUsage(request.getBucketName(), request.getMetadata().getContentLength());

            // 7. 记录访问日志
            saveAccessLog(request.getBucketName(), request.getObjectKey(), "UPLOAD",
                        request.getUserId(), request.getMetadata().getContentLength(),
                        "SUCCESS", null, startTime);

            log.info("Service: Simple upload completed: {}", result.getETag());
            return result;

        } catch (Exception e) {
            log.error("Service: Failed to simple upload", e);
            uploadStatus = "FAILED";
            errorMessage = e.getMessage();
            saveUploadRecord(request, null, uploadStatus, errorMessage, startTime);
            throw e;
        }
    }

    @Override
    public UploadResultDTO uploadBytes(String bucketName, String objectKey, byte[] data, ObjectMetadataDTO metadata, Long userId) throws StorageException {
        log.info("Service: Upload bytes: {}/{}, userId={}", bucketName, objectKey, userId);
        LocalDateTime startTime = LocalDateTime.now();

        try {
            // 验证Bucket
            BucketConfig bucketConfig = validateAndGetBucket(userId, bucketName);
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 将字节数组转换为输入流
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

            // 更新元数据的长度
            if (metadata != null) {
                metadata.setContentLength((long) data.length);
            }

            // 上传
            UploadResultDTO result = adapter.uploadObject(bucketName, objectKey, inputStream, metadata);

            // 保存元数据
            ObjectMetadata objMetadata = buildObjectMetadataFromBytes(bucketName, objectKey, data, metadata, userId);
            objectMetadataMapper.insert(objMetadata);

            // 更新统计
            updateBucketUsage(bucketName, (long) data.length);

            log.info("Service: Upload bytes completed");
            return result;

        } catch (Exception e) {
            log.error("Service: Failed to upload bytes", e);
            throw e;
        }
    }

    @Override
    public String initiateMultipartUpload(InitiateMultipartUploadReqDTO request) throws StorageException {
        log.info("Service: Initiating multipart upload: {}/{}", request.getBucketName(), request.getObjectKey());
        LocalDateTime startTime = LocalDateTime.now();

        try {
            Long userId = request.getUserId() != null ? request.getUserId() : 1L;

            // 验证Bucket
            BucketConfig bucketConfig = validateAndGetBucket(userId, request.getBucketName());
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 初始化分片上传
            String uploadId = adapter.initiateMultipartUpload(
                    request.getBucketName(),
                    request.getObjectKey(),
                    request.getMetadata()
            );

            // 保存上传记录到数据库
            UploadRecord record = UploadRecord.builder()
                .bucketName(request.getBucketName())
                .objectKey(request.getObjectKey())
                .uploadId(uploadId)
                .userId(userId)
                .userShard((int)(userId % 100))
                .fileSize(request.getMetadata().getContentLength())
                .contentType(request.getMetadata().getContentType())
                .uploadMethod("multipart")
                .uploadStatus("INIT")
                .startTime(startTime)
                .uploadIp("")  // TODO: 从请求获取
                .build();

            uploadRecordMapper.insert(record);

            log.info("Service: Multipart upload initiated: uploadId={}", uploadId);
            return uploadId;

        } catch (Exception e) {
            log.error("Service: Failed to initiate multipart upload", e);
            throw e;
        }
    }

    @Override
    public UploadPartResultDTO uploadPart(UploadPartReqDTO request) throws StorageException {
        log.info("Service: Uploading part: {}/{}, part={}",
                request.getBucketName(), request.getObjectKey(), request.getPartNumber());

        try {
            Long userId = request.getUserId() != null ? request.getUserId() : 1L;
            BucketConfig bucketConfig = validateAndGetBucket(userId, request.getBucketName());
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 上传分片
            String eTag = adapter.uploadPart(
                    request.getBucketName(),
                    request.getObjectKey(),
                    request.getUploadId(),
                    request.getPartNumber(),
                    request.getInputStream(),
                    request.getPartSize()
            );

            // 更新上传进度
            UploadRecord record = uploadRecordMapper.selectByUploadId(request.getUploadId());
            if (record != null) {
                record.setUploadedParts(record.getUploadedParts() + 1);
                uploadRecordMapper.updateProgress(record.getId(), record.getUploadedParts());
            }

            UploadPartResultDTO result = UploadPartResultDTO.builder()
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .uploadId(request.getUploadId())
                    .partNumber(request.getPartNumber())
                    .eTag(eTag)
                    .partSize(request.getPartSize())
                    .success(true)
                    .uploadTime(LocalDateTime.now())
                    .build();

            log.info("Service: Part uploaded: partNumber={}, eTag={}", request.getPartNumber(), eTag);
            return result;

        } catch (Exception e) {
            log.error("Service: Failed to upload part", e);
            throw e;
        }
    }

    @Override
    public UploadResultDTO completeMultipartUpload(CompleteMultipartUploadReqDTO request) throws StorageException {
        log.info("Service: Completing multipart upload: {}/{}", request.getBucketName(), request.getObjectKey());
        LocalDateTime startTime = LocalDateTime.now();

        try {
            Long userId = request.getUserId() != null ? request.getUserId() : 1L;
            BucketConfig bucketConfig = validateAndGetBucket(userId, request.getBucketName());
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 完成分片上传
            UploadResultDTO result = adapter.completeMultipartUpload(
                    request.getBucketName(),
                    request.getObjectKey(),
                    request.getUploadId(),
                    request.getPartETags()
            );

            // 保存对象元数据
            ObjectMetadata metadata = buildObjectMetadataFromMultipart(request, result, userId);
            objectMetadataMapper.insert(metadata);

            // 更新上传记录状态
            UploadRecord record = uploadRecordMapper.selectByUploadId(request.getUploadId());
            if (record != null) {
                record.setUploadStatus("COMPLETED");
                record.setCompleteTime(LocalDateTime.now());
                uploadRecordMapper.update(record);
            }

            // 更新统计
            if (result.getFileSize() != null) {
                updateBucketUsage(request.getBucketName(), result.getFileSize());
            }

            log.info("Service: Multipart upload completed: {}", result.getETag());
            return result;

        } catch (Exception e) {
            log.error("Service: Failed to complete multipart upload", e);
            throw e;
        }
    }

    @Override
    public void abortMultipartUpload(String bucketName, String objectKey, String uploadId) throws StorageException {
        log.info("Service: Aborting multipart upload: {}/{}, uploadId={}", bucketName, objectKey, uploadId);

        try {
            Long userId = 1L; // TODO: 从上下文获取用户ID
            BucketConfig bucketConfig = validateAndGetBucket(userId, bucketName);
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // 取消分片上传
            adapter.abortMultipartUpload(bucketName, objectKey, uploadId);

            // 更新上传记录状态
            UploadRecord record = uploadRecordMapper.selectByUploadId(uploadId);
            if (record != null) {
                record.setUploadStatus("CANCELLED");
                uploadRecordMapper.updateStatus(record.getId(), "CANCELLED");
            }

            log.info("Service: Multipart upload aborted");

        } catch (Exception e) {
            log.error("Service: Failed to abort multipart upload", e);
            throw e;
        }
    }

    @Override
    public ListPartsResultDTO listParts(String bucketName, String objectKey, String uploadId) {
        log.debug("Service: Listing parts: {}/{}, uploadId={}", bucketName, objectKey, uploadId);

        try {
            Long userId = 1L;
            BucketConfig bucketConfig = validateAndGetBucket(userId, bucketName);
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            List<java.util.Map<String, Object>> parts = adapter.listParts(bucketName, objectKey, uploadId);

            return ListPartsResultDTO.builder()
                    .bucketName(bucketName)
                    .objectKey(objectKey)
                    .uploadId(uploadId)
                    .parts(parts)
                    .partCount(parts.size())
                    .build();

        } catch (Exception e) {
            log.error("Service: Failed to list parts", e);
            throw new StorageException("LIST_PARTS_FAILED", "Failed to list parts: " + e.getMessage());
        }
    }

    @Override
    public Long appendObject(AppendObjectReqDTO request) throws StorageException {
        log.info("Service: Appending object: {}/{}", request.getBucketName(), request.getObjectKey());

        try {
            Long userId = request.getUserId() != null ? request.getUserId() : 1L;
            BucketConfig bucketConfig = validateAndGetBucket(userId, request.getBucketName());
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            adapter.appendObject(
                    request.getBucketName(),
                    request.getObjectKey(),
                    request.getInputStream(),
                    request.getPosition(),
                    request.getMetadata()
            );

            // 计算下一个位置
            long nextPosition = request.getPosition() + (request.getMetadata().getContentLength() != null ? request.getMetadata().getContentLength() : 0);

            // 更新元数据中的position
            ObjectMetadata metadata = objectMetadataMapper.selectByBucketAndKey(request.getBucketName(), request.getObjectKey());
            if (metadata != null) {
                metadata.setPosition(nextPosition);
                objectMetadataMapper.update(metadata);
            }

            log.info("Service: Object appended: position={}", nextPosition);
            return nextPosition;

        } catch (Exception e) {
            log.error("Service: Failed to append object", e);
            throw e;
        }
    }

    @Override
    public STSCredentialsDTO getSTSForClientUpload(GetSTSReqDTO request) {
        log.info("Service: Getting STS credentials for bucket: {}, prefix: {}",
                 request.getBucketName(), request.getObjectPrefix());

        try {
            // 1. 验证Bucket并获取配置
            BucketConfig bucketConfig = validateAndGetBucket(request.getUserId(), request.getBucketName());

            // 2. 调用STS服务生成临时凭证
            STSCredentialsDTO credentials = stsService.generateSTSCredentials(
                request.getBucketName(),
                request.getObjectPrefix(),
                request.getDurationSeconds(),
                request.getUserId()
            );

            // 3. 设置额外的响应信息(使用extra字段存储bucketName和region)
            if (credentials.getExtra() == null) {
                credentials.setExtra(new java.util.HashMap<>());
            }
            credentials.getExtra().put("bucketName", request.getBucketName());
            credentials.getExtra().put("region", bucketConfig.getRegion());

            log.info("Service: STS credentials generated successfully for bucket: {}", request.getBucketName());
            return credentials;

        } catch (Exception e) {
            log.error("Service: Failed to get STS credentials for bucket: {}", request.getBucketName(), e);
            // 不传递异常对象,避免 Dubbo 序列化问题
            throw new StorageException("STS_FAILED", "Failed to get STS credentials: " + e.getMessage());
        }
    }

    @Override
    public String generatePresignedUrl(PresignedUrlReqDTO request) {
        log.info("Service: Generating presigned URL: {}/{}", request.getBucketName(), request.getObjectKey());

        try {
            Long userId = request.getUserId() != null ? request.getUserId() : 1L;
            BucketConfig bucketConfig = validateAndGetBucket(userId, request.getBucketName());
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            return adapter.generatePresignedUrl(
                    request.getBucketName(),
                    request.getObjectKey(),
                    request.getExpiration(),
                    "PUT"
            );

        } catch (Exception e) {
            log.error("Service: Failed to generate presigned URL", e);
            throw new StorageException("URL_GENERATION_FAILED", "Failed to generate presigned URL: " + e.getMessage());
        }
    }

    @Override
    public Map<String, String> generatePostObjectForm(PostObjectFormReqDTO request) {
        log.info("Service: Generating post object form: {}", request.getBucketName());

        try {
            Long userId = request.getUserId() != null ? request.getUserId() : 1L;
            BucketConfig bucketConfig = validateAndGetBucket(userId, request.getBucketName());
            StorageService adapter = getStorageAdapterFromConfig(bucketConfig);

            // TODO: 生成表单上传的表单数据和签名
            Map<String, String> result = new java.util.HashMap<>();
            result.put("url", "");
            result.put("formData", "{}");
            return result;

        } catch (Exception e) {
            log.error("Service: Failed to generate post object form", e);
            throw new StorageException("FORM_DATA_GENERATION_FAILED", "Failed to generate post object form: " + e.getMessage());
        }
    }

    @Override
    public OssPostSignatureDTO getOssPostSignature(String bucketName, Long userId) {
        log.info("Service: 获取OSS POST签名, bucket: {}, userId: {}", bucketName, userId);

        try {
            // ✅ 权限验证：验证用户是否有权限访问该 bucket
            BucketConfig bucketConfig = validateAndGetBucket(userId, bucketName);

            // ✅ 获取 bucket 的 API Key 配置
            ApiKeyConfigDTO apiKeyConfig = getApiKeyConfigFromBucket(bucketConfig);

            // 调用签名服务生成签名（传入 API Key）
            return ossPostSignatureService.getOssPostSignature(bucketName, apiKeyConfig);

        } catch (Exception e) {
            log.error("Service: 获取OSS POST签名失败, bucket: {}, userId: {}", bucketName, userId, e);
            throw new StorageException("SIGNATURE_FAILED", "Failed to get OSS post signature: " + e.getMessage());
        }
    }

    @Override
    public CallbackUploadDTO generateCallbackUpload(CallbackUploadReqDTO request) {
        log.info("Service: Generate callback upload: {}", request.getCallbackUrl());

        try {
            // TODO: 处理上传回调
            // 1. 验证回调签名
            // 2. 更新上传记录状态
            // 3. 触发后续处理

            Map<String, String> callbackBodyMap = new java.util.HashMap<>();
            // TODO: 填充实际的回调Body内容
            String callbackBody = "{}";  // 临时使用空JSON对象
            return CallbackUploadDTO.builder()
                .callbackUrl(request.getCallbackUrl())
                .callbackBody(callbackBody)
                .build();

        } catch (Exception e) {
            log.error("Service: Failed to generate callback upload", e);
            throw new StorageException("CALLBACK_FAILED", "Failed to generate callback upload: " + e.getMessage());
        }
    }

    @Override
    public String createResumeUploadRecord(ResumeUploadReqDTO request) {
        log.info("Service: Creating resume upload record: {}/{}", request.getBucketName(), request.getObjectKey());

        try {
            // TODO: 创建断点续传记录
            return "record-" + System.currentTimeMillis();

        } catch (Exception e) {
            log.error("Service: Failed to create resume upload record", e);
            throw new StorageException("CREATE_FAILED", "Failed to create resume record: " + e.getMessage());
        }
    }

    @Override
    public ResumeUploadInfoDTO getResumeUploadRecord(String recordId) {
        log.debug("Service: Getting resume upload record: {}", recordId);

        try {
            // TODO: 获取断点续传记录
            return ResumeUploadInfoDTO.builder()
                .recordId(recordId)
                .build();

        } catch (Exception e) {
            log.error("Service: Failed to get resume upload record", e);
            throw new StorageException("QUERY_FAILED", "Failed to query resume record: " + e.getMessage());
        }
    }

    @Override
    public void deleteResumeUploadRecord(String recordId) {
        log.info("Service: Deleting resume upload record: {}", recordId);

        try {
            // TODO: 从数据库删除断点续传记录
            // uploadRecordMapper.deleteByRecordId(recordId);

        } catch (Exception e) {
            log.error("Service: Failed to delete resume upload record", e);
            throw new StorageException("DELETE_FAILED", "Failed to delete resume record: " + e.getMessage());
        }
    }

    @Override
    public UploadStrategy selectUploadStrategy(long fileSize) {
        log.debug("Service: Selecting upload strategy: fileSize={}", fileSize);

        // 小文件(<100MB): 简单上传
        if (fileSize < 100 * 1024 * 1024) {
            return UploadStrategy.SIMPLE_UPLOAD;
        }
        // 大文件(>=100MB): 分片上传
        else {
            return UploadStrategy.MULTIPART_UPLOAD;
        }
    }

    @Override
    public long calculatePartSize(long fileSize, int partCount) {
        log.debug("Service: Calculating part size: fileSize={}, partCount={}", fileSize, partCount);

        // 计算最优分片大小
        // 阿里云OSS: 100KB ~ 5GB
        long minPartSize = 100 * 1024;  // 100KB
        long maxPartSize = 5L * 1024 * 1024 * 1024;  // 5GB

        // 根据文件大小计算分片大小
        long partSize = fileSize / partCount;

        // 确保分片大小在合理范围内
        if (partSize < minPartSize) {
            partSize = minPartSize;
        }
        if (partSize > maxPartSize) {
            partSize = maxPartSize;
        }

        return partSize;
    }

    @Override
    public ValidationResult validateUploadRequest(SimpleUploadReqDTO request) {
        log.debug("Service: Validating upload request: {}", request.getBucketName());

        try {
            // 验证上传请求
            if (!StringUtils.hasText(request.getBucketName())) {
                return ValidationResult.builder()
                    .valid(false)
                    .errorCode("INVALID_BUCKET_NAME")
                    .errorMessage("Bucket name is required")
                    .build();
            }
            if (!StringUtils.hasText(request.getObjectKey())) {
                return ValidationResult.builder()
                    .valid(false)
                    .errorCode("INVALID_OBJECT_KEY")
                    .errorMessage("Object key is required")
                    .build();
            }
            if (request.getInputStream() == null) {
                return ValidationResult.builder()
                    .valid(false)
                    .errorCode("INVALID_INPUT_STREAM")
                    .errorMessage("Input stream is required")
                    .build();
            }

            return ValidationResult.builder()
                .valid(true)
                .build();

        } catch (Exception e) {
            log.error("Service: Failed to validate upload request", e);
            return ValidationResult.builder()
                .valid(false)
                .errorCode("VALIDATION_ERROR")
                .errorMessage(e.getMessage())
                .build();
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证并获取Bucket配置
     */
    private BucketConfig validateAndGetBucket(Long userId, String bucketName) {
        log.info("【权限验证】开始验证 bucket 访问权限");
        log.info("【权限验证】请求 userId: {}, bucketName: {}", userId, bucketName);

        BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
        if (bucketConfig == null) {
            throw new StorageException("BUCKET_NOT_FOUND", "Bucket not found: " + bucketName);
        }

        log.info("【权限验证】Bucket配置 - userId: {}, apiKeyId: {}, isDeleted: {}",
                bucketConfig.getUserId(), bucketConfig.getApiKeyId(), bucketConfig.getIsDeleted());

        if (bucketConfig.getIsDeleted()) {
            throw new StorageException("BUCKET_DELETED", "Bucket has been deleted");
        }

        // ✅ 通过 api_key_id 验证权限
        // 验证逻辑：该 apiKey 是否属于当前用户
        if (bucketConfig.getApiKeyId() != null) {
            com.duda.user.dto.userapikey.UserApiKeyDTO apiKeyDTO = userApiKeyRpc.getUserApiKeyById(bucketConfig.getApiKeyId());

            if (apiKeyDTO == null) {
                log.error("【权限验证】API Key不存在！apiKeyId: {}", bucketConfig.getApiKeyId());
                throw new StorageException("API_KEY_NOT_FOUND", "API Key not found: " + bucketConfig.getApiKeyId());
            }

            log.info("【权限验证】API Key信息 - apiKeyId: {}, apiKeyUserId: {}, 请求userId: {}",
                    bucketConfig.getApiKeyId(), apiKeyDTO.getUserId(), userId);

            // 验证 API Key 是否属于该用户
            if (!apiKeyDTO.getUserId().equals(userId)) {
                log.error("【权限验证】权限拒绝！API Key (id={}) 属于用户 {}，但请求来自用户 {}",
                        bucketConfig.getApiKeyId(), apiKeyDTO.getUserId(), userId);
                throw new StorageException("PERMISSION_DENIED", "No permission to access bucket");
            }
        } else {
            // 如果没有 api_key_id，使用旧的验证方式（兼容性）
            log.warn("【权限验证】Bucket没有配置apiKeyId，使用旧版权限验证");
            if (!bucketConfig.getUserId().equals(userId)) {
                log.error("【权限验证】权限拒绝！请求userId: {}, Bucket的userId: {}", userId, bucketConfig.getUserId());
                throw new StorageException("PERMISSION_DENIED", "No permission to access bucket");
            }
        }

        log.info("【权限验证】权限验证通过");
        return bucketConfig;
    }

    /**
     * 从 BucketConfig 构建ApiKeyConfigDTO（用于签名生成）
     */
    private ApiKeyConfigDTO getApiKeyConfigFromBucket(BucketConfig bucketConfig) {
        try {
            log.info("【API密钥配置】开始构建ApiKeyConfig，bucketName: {}, apiKeyId: {}",
                    bucketConfig.getBucketName(), bucketConfig.getApiKeyId());

            StorageType storageType = StorageType.fromCode(bucketConfig.getStorageType());

            // 通过 api_key_id 查询 API Key
            com.duda.user.dto.userapikey.UserApiKeyDTO apiKeyDTO =
                userApiKeyRpc.getUserApiKeyById(bucketConfig.getApiKeyId());

            if (apiKeyDTO == null) {
                throw new StorageException("API_KEY_NOT_FOUND",
                    "API密钥不存在，keyId: " + bucketConfig.getApiKeyId());
            }

            log.info("【API密钥配置】获取到API密钥: keyName={}, keyType={}, region={}",
                    apiKeyDTO.getKeyName(), apiKeyDTO.getKeyType(), apiKeyDTO.getRegion());

            // ✅ 使用明文 API Key（RPC 返回的已解密）
            ApiKeyConfigDTO apiKeyConfig = ApiKeyConfigDTO.builder()
                .storageType(storageType)
                .accessKeyId(apiKeyDTO.getPlainAccessKeyId())      // ✅ 明文
                .accessKeySecret(apiKeyDTO.getPlainAccessKeySecret()) // ✅ 明文
                .endpoint(bucketConfig.getEndpoint())
                .region(bucketConfig.getRegion())
                .build();

            log.info("【API密钥配置】ApiKeyConfig构建完成");
            return apiKeyConfig;

        } catch (Exception e) {
            log.error("【API密钥配置】构建ApiKeyConfig失败", e);
            throw new StorageException("API_KEY_CONFIG_FAILED",
                "Failed to build API key config: " + e.getMessage());
        }
    }

    /**
     * 获取存储适配器
     */
    private StorageService getStorageAdapter(Long userId, String bucketName) {
        BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);

        if (bucketConfig != null) {
            return getStorageAdapterFromConfig(bucketConfig);
        }

        // 如果数据库中没有配置,使用默认配置
        log.warn("Bucket config not found in database, using default config: {}", bucketName);

        ApiKeyConfigDTO apiKeyConfig = ApiKeyConfigDTO.builder()
            .storageType(StorageType.ALIYUN_OSS)
            .accessKeyId("")
            .accessKeySecret("")
            .endpoint("oss-cn-hangzhou.aliyuncs.com")
            .region("cn-hangzhou")
            .build();

        return storageAdapterFactory.createAdapter(StorageType.ALIYUN_OSS, apiKeyConfig);
    }

    /**
     * 根据Bucket配置创建存储适配器
     */
    private StorageService getStorageAdapterFromConfig(BucketConfig bucketConfig) {
        log.info("========================================");
        log.info("【存储适配器】开始创建存储适配器");
        log.info("【存储适配器】bucketName: {}", bucketConfig.getBucketName());
        log.info("【存储适配器】apiKeyId: {}", bucketConfig.getApiKeyId());

        StorageType storageType = StorageType.fromCode(bucketConfig.getStorageType());
        log.info("【存储适配器】storageType: {}", storageType);

        // 通过RPC调用获取API密钥信息（RPC已经解密，返回明文密钥）
        log.info("【存储适配器】准备调用RPC获取API密钥，keyId={}", bucketConfig.getApiKeyId());
        com.duda.user.dto.userapikey.UserApiKeyDTO apiKeyDTO = userApiKeyRpc.getUserApiKeyById(bucketConfig.getApiKeyId());

        log.info("【存储适配器】RPC调用完成，apiKeyDTO是否为null: {}", apiKeyDTO == null);

        if (apiKeyDTO == null) {
            log.error("【存储适配器】❌ API密钥DTO为null，keyId={}", bucketConfig.getApiKeyId());
            throw new StorageException("API_KEY_NOT_FOUND",
                "API密钥不存在，keyId: " + bucketConfig.getApiKeyId());
        }

        // 打印日志检查是否解密成功
        log.info("【API密钥调试】✅ apiKeyDTO不为null，开始检查字段值");
        log.info("【API密钥调试】1. apiKeyDTO.getId(): {}", apiKeyDTO.getId());
        log.info("【API密钥调试】2. apiKeyDTO.getKeyName(): {}", apiKeyDTO.getKeyName());

        // 检查加密字段
        log.info("【API密钥调试】3. getAccessKeyId()是否为null: {}", apiKeyDTO.getAccessKeyId() == null);
        if (apiKeyDTO.getAccessKeyId() != null) {
            log.info("【API密钥调试】   getAccessKeyId()长度: {}", apiKeyDTO.getAccessKeyId().length());
            log.info("【API密钥调试】   getAccessKeyId()前20字符: {}",
                apiKeyDTO.getAccessKeyId().substring(0, Math.min(20, apiKeyDTO.getAccessKeyId().length())));
        }

        log.info("【API密钥调试】4. getAccessKeySecret()是否为null: {}", apiKeyDTO.getAccessKeySecret() == null);
        if (apiKeyDTO.getAccessKeySecret() != null) {
            log.info("【API密钥调试】   getAccessKeySecret()长度: {}", apiKeyDTO.getAccessKeySecret().length());
        }

        // 检查明文字段（重点！）
        log.info("【API密钥调试】5. getPlainAccessKeyId()是否为null: {}", apiKeyDTO.getPlainAccessKeyId() == null);
        if (apiKeyDTO.getPlainAccessKeyId() != null) {
            log.info("【API密钥调试】   getPlainAccessKeyId()长度: {}", apiKeyDTO.getPlainAccessKeyId().length());
            log.info("【API密钥调试】   getPlainAccessKeyId()完整值: [{}]", apiKeyDTO.getPlainAccessKeyId());
            log.info("【API密钥调试】   getPlainAccessKeyId()前8字符: [{}]",
                apiKeyDTO.getPlainAccessKeyId().substring(0, Math.min(8, apiKeyDTO.getPlainAccessKeyId().length())));
        } else {
            log.error("【API密钥调试】❌ getPlainAccessKeyId()为null！！！");
        }

        log.info("【API密钥调试】6. getPlainAccessKeySecret()是否为null: {}", apiKeyDTO.getPlainAccessKeySecret() == null);
        if (apiKeyDTO.getPlainAccessKeySecret() != null) {
            log.info("【API密钥调试】   getPlainAccessKeySecret()长度: {}", apiKeyDTO.getPlainAccessKeySecret().length());
            log.info("【API密钥调试】   getPlainAccessKeySecret()前8字符: [{}]",
                apiKeyDTO.getPlainAccessKeySecret().substring(0, Math.min(8, apiKeyDTO.getPlainAccessKeySecret().length())));
            log.info("【API密钥调试】   getPlainAccessKeySecret()后8字符: [{}]",
                apiKeyDTO.getPlainAccessKeySecret().substring(Math.max(0, apiKeyDTO.getPlainAccessKeySecret().length() - 8)));
        } else {
            log.error("【API密钥调试】❌ getPlainAccessKeySecret()为null！！！");
        }

        // 使用RPC返回的明文密钥（已在user-provider中解密）
        ApiKeyConfigDTO apiKeyConfig = ApiKeyConfigDTO.builder()
            .storageType(storageType)
            .accessKeyId(apiKeyDTO.getPlainAccessKeyId())
            .accessKeySecret(apiKeyDTO.getPlainAccessKeySecret())
            .endpoint(bucketConfig.getEndpoint())
            .region(bucketConfig.getRegion())
            .build();

        log.info("【存储适配器】apiKeyConfig构建完成");
        log.info("【存储适配器】配置的AccessKeyId: {}", apiKeyConfig.getAccessKeyId());
        log.info("【存储适配器】配置的AccessKeySecret长度: {}",
            apiKeyConfig.getAccessKeySecret() != null ? apiKeyConfig.getAccessKeySecret().length() : "null");
        log.info("【存储适配器】endpoint: {}", apiKeyConfig.getEndpoint());
        log.info("【存储适配器】region: {}", apiKeyConfig.getRegion());
        log.info("========================================");

        return storageAdapterFactory.createAdapter(storageType, apiKeyConfig);
    }

    /**
     * 解密API密钥
     * 使用AES解密从数据库读取的加密API密钥
     */
    private String decryptApiKey(String encryptedKey) {
        if (!StringUtils.hasText(encryptedKey)) {
            return "";
        }
        try {
            return AesUtil.decrypt(encryptedKey, encryptionKey);
        } catch (Exception e) {
            log.error("解密API密钥失败", e);
            throw new StorageException("DECRYPTION_FAILED", "Failed to decrypt API key");
        }
    }

    /**
     * 构建对象元数据(简单上传)
     */
    private ObjectMetadata buildObjectMetadata(SimpleUploadReqDTO request, UploadResultDTO result, BucketConfig bucketConfig) {
        return ObjectMetadata.builder()
            .bucketName(request.getBucketName())
            .objectKey(request.getObjectKey())
            .fileSize(request.getMetadata().getContentLength())
            .fileName("")  // TODO: 从请求中提取
            .contentType(request.getMetadata().getContentType())
            .contentMd5("")  // TODO: 从result中获取
            .crc64(result.getCrc64())
            .storageClass(bucketConfig.getStorageClass())
            .objectType("NORMAL")
            .acl(bucketConfig.getAclType())
            .etag(result.getETag())
            .uploadIp("")  // TODO: 从请求获取
            .uploadTime(LocalDateTime.now())
            .status("active")
            .createdBy(request.getUserId())
            .build();
    }

    /**
     * 构建对象元数据(字节数组上传)
     */
    private ObjectMetadata buildObjectMetadataFromBytes(String bucketName, String objectKey,
                                                        byte[] data, ObjectMetadataDTO metadata, Long userId) {
        return ObjectMetadata.builder()
            .bucketName(bucketName)
            .objectKey(objectKey)
            .fileSize((long) data.length)
            .contentType(metadata != null ? metadata.getContentType() : null)
            .storageClass("STANDARD")
            .objectType("NORMAL")
            .acl("PRIVATE")
            .uploadTime(LocalDateTime.now())
            .status("active")
            .createdBy(userId)
            .build();
    }

    /**
     * 构建对象元数据(分片上传)
     */
    private ObjectMetadata buildObjectMetadataFromMultipart(CompleteMultipartUploadReqDTO request,
                                                           UploadResultDTO result, Long userId) {
        return ObjectMetadata.builder()
            .bucketName(request.getBucketName())
            .objectKey(request.getObjectKey())
            .fileSize(result.getFileSize())
            .etag(result.getETag())
            .uploadId(request.getUploadId())
            .partCount(request.getPartETags().size())
            .storageClass("STANDARD")
            .objectType("NORMAL")
            .acl("PRIVATE")
            .uploadTime(LocalDateTime.now())
            .status("active")
            .createdBy(userId)
            .build();
    }

    /**
     * 保存上传记录
     */
    private void saveUploadRecord(SimpleUploadReqDTO request, UploadResultDTO result,
                                String uploadStatus, String errorMessage, LocalDateTime startTime) {
        try {
            UploadRecord record = UploadRecord.builder()
                .bucketName(request.getBucketName())
                .objectKey(request.getObjectKey())
                .userId(request.getUserId())
                .userShard((int)(request.getUserId() % 100))
                .fileSize(request.getContentLength() != null ? request.getContentLength() :
                         (request.getMetadata() != null ? request.getMetadata().getContentLength() : 0))
                .contentType(request.getContentType() != null ? request.getContentType() :
                            (request.getMetadata() != null ? request.getMetadata().getContentType() : null))
                .uploadMethod("simple")
                .uploadStatus(uploadStatus)
                .startTime(startTime)
                .completeTime("COMPLETED".equals(uploadStatus) ? LocalDateTime.now() : null)
                .errorMessage(errorMessage)
                .build();

            uploadRecordMapper.insert(record);
        } catch (Exception e) {
            log.error("Failed to save upload record", e);
        }
    }

    /**
     * 更新Bucket使用统计
     */
    private void updateBucketUsage(String bucketName, Long fileSize) {
        try {
            bucketConfigMapper.updateUsage(bucketName, fileSize, 1);
        } catch (Exception e) {
            log.error("Failed to update bucket usage", e);
        }
    }

    /**
     * 保存访问日志
     */
    private void saveAccessLog(String bucketName, String objectKey, String operation,
                              Long userId, Long fileSize, String resultStatus,
                              String errorMessage, LocalDateTime startTime) {
        try {
            FileAccessLog log = FileAccessLog.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .operation(operation)
                .userId(userId)
                .fileSize(fileSize)
                .resultStatus(resultStatus)
                .errorMessage(errorMessage)
                .startTime(startTime)
                .endTime(LocalDateTime.now())
                .durationMs(java.time.Duration.between(startTime, LocalDateTime.now()).toMillis())
                .build();

            fileAccessLogMapper.insert(log);
        } catch (Exception e) {
            log.error("Failed to save access log", e);
        }
    }
}
