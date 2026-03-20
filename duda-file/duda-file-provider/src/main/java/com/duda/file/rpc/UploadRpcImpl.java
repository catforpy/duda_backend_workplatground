package com.duda.file.rpc;

import com.duda.file.dto.upload.*;
import com.duda.file.service.UploadService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Upload RPC 实现类
 * 对外提供 Dubbo RPC 服务，注册到 Nacos
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@DubboService(version = "1.0.0", group = "DUDA_FILE_GROUP", timeout = 300000)
public class UploadRpcImpl implements IUploadRpc {

    @jakarta.annotation.Resource
    private com.duda.file.service.impl.UploadServiceImpl uploadServiceImpl;

    @Override
    public UploadResultDTO simpleUpload(SimpleUploadReqDTO request) {
        log.info("【RPC】Simple upload: bucket={}, object={}",
            request.getBucketName(), request.getObjectKey());
        return uploadServiceImpl.simpleUpload(request);
    }

    @Override
    public UploadResultDTO uploadBytes(String bucketName, String objectKey, byte[] data, com.duda.file.dto.object.ObjectMetadataDTO metadata, Long userId) {
        log.info("【RPC】Upload bytes: bucket={}, object={}, userId={}", bucketName, objectKey, userId);
        return uploadServiceImpl.uploadBytes(bucketName, objectKey, data, metadata, userId);
    }

    @Override
    public String initiateMultipartUpload(InitiateMultipartUploadReqDTO request) {
        log.info("【RPC】Initiate multipart upload: bucket={}", request.getBucketName());
        return uploadServiceImpl.initiateMultipartUpload(request);
    }

    @Override
    public UploadPartResultDTO uploadPart(UploadPartReqDTO request) {
        log.info("【RPC】Upload part: bucket={}, uploadId={}",
            request.getBucketName(), request.getUploadId());
        return uploadServiceImpl.uploadPart(request);
    }

    @Override
    public UploadResultDTO completeMultipartUpload(CompleteMultipartUploadReqDTO request) {
        log.info("【RPC】Complete multipart upload: bucket={}", request.getBucketName());
        return uploadServiceImpl.completeMultipartUpload(request);
    }

    @Override
    public void abortMultipartUpload(String bucketName, String objectKey, String uploadId) {
        log.info("【RPC】Abort multipart upload: bucket={}, uploadId={}", bucketName, uploadId);
        uploadServiceImpl.abortMultipartUpload(bucketName, objectKey, uploadId);
    }

    @Override
    public ListPartsResultDTO listParts(String bucketName, String objectKey, String uploadId) {
        log.info("【RPC】List parts: bucket={}, uploadId={}", bucketName, uploadId);
        return uploadServiceImpl.listParts(bucketName, objectKey, uploadId);
    }

    @Override
    public STSCredentialsDTO getSTSForClientUpload(GetSTSReqDTO request) {
        log.info("【RPC】Get STS for client upload: bucket={}", request.getBucketName());
        return uploadServiceImpl.getSTSForClientUpload(request);
    }

    @Override
    public Map<String, String> generatePostObjectForm(PostObjectFormReqDTO request) {
        log.info("【RPC】Generate post object form: bucket={}", request.getBucketName());
        return uploadServiceImpl.generatePostObjectForm(request);
    }

    @Override
    public OssPostSignatureDTO getOssPostSignature(String bucketName, Long userId) {
        log.info("【RPC】Get OSS post signature: bucket={}, userId={}", bucketName, userId);
        return uploadServiceImpl.getOssPostSignature(bucketName, userId);
    }

    @Override
    public String generatePresignedUrl(PresignedUrlReqDTO request) {
        log.info("【RPC】Generate presigned URL: bucket={}", request.getBucketName());
        return uploadServiceImpl.generatePresignedUrl(request);
    }

    @Override
    public String createResumeUploadRecord(ResumeUploadReqDTO request) {
        log.info("【RPC】Create resume upload record");
        return uploadServiceImpl.createResumeUploadRecord(request);
    }

    @Override
    public ResumeUploadInfoDTO getResumeUploadRecord(String recordId) {
        log.info("【RPC】Get resume upload record: recordId={}", recordId);
        return uploadServiceImpl.getResumeUploadRecord(recordId);
    }

    @Override
    public void deleteResumeUploadRecord(String recordId) {
        log.info("【RPC】Delete resume upload record: recordId={}", recordId);
        uploadServiceImpl.deleteResumeUploadRecord(recordId);
    }

    @Override
    public UploadStrategy selectUploadStrategy(long fileSize) {
        log.info("【RPC】Select upload strategy: fileSize={}", fileSize);
        return uploadServiceImpl.selectUploadStrategy(fileSize);
    }

    @Override
    public ValidationResult validateUploadRequest(SimpleUploadReqDTO request) {
        log.info("【RPC】Validate upload request: bucket={}", request.getBucketName());
        return uploadServiceImpl.validateUploadRequest(request);
    }

    @Override
    public Long appendObject(AppendObjectReqDTO request) {
        log.info("【RPC】Append object: bucket={}, object={}",
            request.getBucketName(), request.getObjectKey());
        return uploadServiceImpl.appendObject(request);
    }

    @Override
    public CallbackUploadDTO generateCallbackUpload(CallbackUploadReqDTO request) {
        log.info("【RPC】Generate callback upload: callbackUrl={}", request.getCallbackUrl());
        return uploadServiceImpl.generateCallbackUpload(request);
    }

    @Override
    public long calculatePartSize(long fileSize, int partCount) {
        log.info("【RPC】Calculate part size: fileSize={}, partCount={}", fileSize, partCount);
        return uploadServiceImpl.calculatePartSize(fileSize, partCount);
    }
}
