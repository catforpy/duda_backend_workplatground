package com.duda.file.api.service.impl;

import com.duda.file.dto.upload.*;
import com.duda.file.dto.object.ObjectMetadataDTO;
import com.duda.file.api.service.FileUploadService;
import com.duda.file.rpc.IUploadRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 上传本地服务实现
 *
 * @author DudaNexus
 * @since 2026-03-18
 */
@Slf4j
@Service("fileUploadServiceImpl")
public class FileUploadServiceImpl implements FileUploadService {

    @DubboReference(version = "1.0.0", group = "DUDA_FILE_GROUP", check = false, timeout = 300000)
    private IUploadRpc uploadRpc;

    @Override
    public UploadResultDTO simpleUpload(SimpleUploadReqDTO request) {
        log.info("【API Service】Simple upload: {}", request.getBucketName());
        return uploadRpc.simpleUpload(request);
    }

    @Override
    public UploadResultDTO uploadBytes(String bucketName, String objectKey, byte[] data, ObjectMetadataDTO metadata, Long userId) {
        log.info("【API Service】Upload bytes: {}/{}, userId={}", bucketName, objectKey, userId);
        return uploadRpc.uploadBytes(bucketName, objectKey, data, metadata, userId);
    }

    @Override
    public String initiateMultipartUpload(InitiateMultipartUploadReqDTO request) {
        log.info("【API Service】Init multipart upload: {}", request.getBucketName());
        return uploadRpc.initiateMultipartUpload(request);
    }

    @Override
    public UploadPartResultDTO uploadPart(UploadPartReqDTO request) {
        log.info("【API Service】Upload part: {}", request.getBucketName());
        return uploadRpc.uploadPart(request);
    }

    @Override
    public UploadResultDTO completeMultipartUpload(CompleteMultipartUploadReqDTO request) {
        log.info("【API Service】Complete multipart upload: {}", request.getBucketName());
        return uploadRpc.completeMultipartUpload(request);
    }

    @Override
    public void abortMultipartUpload(String bucketName, String objectKey, String uploadId) {
        log.info("【API Service】Abort multipart upload: {}", bucketName);
        uploadRpc.abortMultipartUpload(bucketName, objectKey, uploadId);
    }

    @Override
    public ListPartsResultDTO listParts(String bucketName, String objectKey, String uploadId) {
        log.info("【API Service】List parts: {}", bucketName);
        return uploadRpc.listParts(bucketName, objectKey, uploadId);
    }

    @Override
    public Long appendObject(AppendObjectReqDTO request) {
        log.info("【API Service】Append object: {}", request.getBucketName());
        return uploadRpc.appendObject(request);
    }

    @Override
    public STSCredentialsDTO getSTSForClientUpload(GetSTSReqDTO request) {
        log.info("【API Service】Get STS for client upload");
        return uploadRpc.getSTSForClientUpload(request);
    }

    @Override
    public Map<String, String> generatePostObjectForm(PostObjectFormReqDTO request) {
        log.info("【API Service】Generate post object form");
        return uploadRpc.generatePostObjectForm(request);
    }

    @Override
    public OssPostSignatureDTO getOssPostSignature(String bucketName, Long userId) {
        log.info("【API Service】Get OSS post signature: {}, userId: {}", bucketName, userId);
        return uploadRpc.getOssPostSignature(bucketName, userId);
    }

    @Override
    public String generatePresignedUrl(PresignedUrlReqDTO request) {
        log.info("【API Service】Generate presigned URL");
        return uploadRpc.generatePresignedUrl(request);
    }

    @Override
    public CallbackUploadDTO generateCallbackUpload(CallbackUploadReqDTO request) {
        log.info("【API Service】Generate callback upload");
        return uploadRpc.generateCallbackUpload(request);
    }

    @Override
    public String createResumeUploadRecord(ResumeUploadReqDTO request) {
        log.info("【API Service】Create resume upload record");
        return uploadRpc.createResumeUploadRecord(request);
    }

    @Override
    public ResumeUploadInfoDTO getResumeUploadRecord(String recordId) {
        log.info("【API Service】Get resume upload record: {}", recordId);
        return uploadRpc.getResumeUploadRecord(recordId);
    }

    @Override
    public void deleteResumeUploadRecord(String recordId) {
        log.info("【API Service】Delete resume upload record: {}", recordId);
        uploadRpc.deleteResumeUploadRecord(recordId);
    }

    @Override
    public UploadStrategy selectUploadStrategy(long fileSize) {
        return uploadRpc.selectUploadStrategy(fileSize);
    }

    @Override
    public long calculatePartSize(long fileSize, int partCount) {
        return uploadRpc.calculatePartSize(fileSize, partCount);
    }

    @Override
    public ValidationResult validateUploadRequest(SimpleUploadReqDTO request) {
        return uploadRpc.validateUploadRequest(request);
    }
}
