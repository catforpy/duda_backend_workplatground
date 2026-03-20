package com.duda.file.api.service;

import com.duda.file.dto.upload.*;
import com.duda.file.dto.object.ObjectMetadataDTO;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 上传本地服务接口
 *
 * @author DudaNexus
 * @since 2026-03-18
 */
public interface FileUploadService {

    UploadResultDTO simpleUpload(SimpleUploadReqDTO request);

    UploadResultDTO uploadBytes(String bucketName, String objectKey, byte[] data, ObjectMetadataDTO metadata, Long userId);

    String initiateMultipartUpload(InitiateMultipartUploadReqDTO request);

    UploadPartResultDTO uploadPart(UploadPartReqDTO request);

    UploadResultDTO completeMultipartUpload(CompleteMultipartUploadReqDTO request);

    void abortMultipartUpload(String bucketName, String objectKey, String uploadId);

    ListPartsResultDTO listParts(String bucketName, String objectKey, String uploadId);

    Long appendObject(AppendObjectReqDTO request);

    STSCredentialsDTO getSTSForClientUpload(GetSTSReqDTO request);

    Map<String, String> generatePostObjectForm(PostObjectFormReqDTO request);

    OssPostSignatureDTO getOssPostSignature(String bucketName, Long userId);

    String generatePresignedUrl(PresignedUrlReqDTO request);

    CallbackUploadDTO generateCallbackUpload(CallbackUploadReqDTO request);

    String createResumeUploadRecord(ResumeUploadReqDTO request);

    ResumeUploadInfoDTO getResumeUploadRecord(String recordId);

    void deleteResumeUploadRecord(String recordId);

    UploadStrategy selectUploadStrategy(long fileSize);

    long calculatePartSize(long fileSize, int partCount);

    ValidationResult validateUploadRequest(SimpleUploadReqDTO request);
}
