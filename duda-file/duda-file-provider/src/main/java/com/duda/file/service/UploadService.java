package com.duda.file.service;

import com.duda.file.dto.upload.*;
import com.duda.file.dto.object.ObjectMetadataDTO;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Upload Service 接口
 * 内部业务逻辑接口
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
public interface UploadService {

    // ==================== 简单上传 ====================

    UploadResultDTO simpleUpload(SimpleUploadReqDTO request);

    UploadResultDTO uploadBytes(String bucketName, String objectKey, byte[] data, ObjectMetadataDTO metadata, Long userId);

    // ==================== 分片上传 ====================

    String initiateMultipartUpload(InitiateMultipartUploadReqDTO request);

    UploadPartResultDTO uploadPart(UploadPartReqDTO request);

    UploadResultDTO completeMultipartUpload(CompleteMultipartUploadReqDTO request);

    void abortMultipartUpload(String bucketName, String objectKey, String uploadId);

    ListPartsResultDTO listParts(String bucketName, String objectKey, String uploadId);

    // ==================== 追加上传 ====================

    Long appendObject(AppendObjectReqDTO request);

    // ==================== 客户端直传 ====================

    STSCredentialsDTO getSTSForClientUpload(GetSTSReqDTO request);

    Map<String, String> generatePostObjectForm(PostObjectFormReqDTO request);

    OssPostSignatureDTO getOssPostSignature(String bucketName, Long userId);

    String generatePresignedUrl(PresignedUrlReqDTO request);

    CallbackUploadDTO generateCallbackUpload(CallbackUploadReqDTO request);

    // ==================== 断点续传 ====================

    String createResumeUploadRecord(ResumeUploadReqDTO request);

    ResumeUploadInfoDTO getResumeUploadRecord(String recordId);

    void deleteResumeUploadRecord(String recordId);

    // ==================== 上传策略 ====================

    UploadStrategy selectUploadStrategy(long fileSize);

    long calculatePartSize(long fileSize, int partCount);

    ValidationResult validateUploadRequest(SimpleUploadReqDTO request);
}

