package com.duda.file.rpc;

import com.duda.file.dto.upload.*;
import com.duda.file.dto.object.ObjectMetadataDTO;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Upload RPC 接口
 * 对外提供 Dubbo RPC 服务
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
public interface IUploadRpc {

    // ==================== 简单上传 ====================

    /**
     * 简单上传(适用于小文件,通常<5GB)
     */
    UploadResultDTO simpleUpload(SimpleUploadReqDTO request);

    /**
     * 字节数组上传
     */
    UploadResultDTO uploadBytes(String bucketName, String objectKey, byte[] data, ObjectMetadataDTO metadata, Long userId);

    // ==================== 分片上传 ====================

    /**
     * 初始化分片上传
     */
    String initiateMultipartUpload(InitiateMultipartUploadReqDTO request);

    /**
     * 上传分片
     */
    UploadPartResultDTO uploadPart(UploadPartReqDTO request);

    /**
     * 完成分片上传
     */
    UploadResultDTO completeMultipartUpload(CompleteMultipartUploadReqDTO request);

    /**
     * 取消分片上传
     */
    void abortMultipartUpload(String bucketName, String objectKey, String uploadId);

    /**
     * 列出已上传的分片
     */
    ListPartsResultDTO listParts(String bucketName, String objectKey, String uploadId);

    // ==================== 追加上传 ====================

    /**
     * 追加上传(适用于日志文件等场景)
     */
    Long appendObject(AppendObjectReqDTO request);

    // ==================== 客户端直传 ====================

    /**
     * 获取STS临时凭证(用于客户端直传)
     */
    STSCredentialsDTO getSTSForClientUpload(GetSTSReqDTO request);

    /**
     * 生成PostObject表单(用于Web表单上传)
     */
    Map<String, String> generatePostObjectForm(PostObjectFormReqDTO request);

    /**
     * 获取OSS POST签名（用于表单直传）
     * <p>完全遵循阿里云官方文档的POST签名方案</p>
     *
     * @param bucketName Bucket名称
     * @param userId 用户ID（用于权限验证）
     * @return POST签名响应
     */
    OssPostSignatureDTO getOssPostSignature(String bucketName, Long userId);

    /**
     * 生成预签名URL(用于客户端上传/下载)
     */
    String generatePresignedUrl(PresignedUrlReqDTO request);

    /**
     * 生成带回调的上传凭证
     */
    CallbackUploadDTO generateCallbackUpload(CallbackUploadReqDTO request);

    // ==================== 断点续传 ====================

    /**
     * 创建断点续传记录
     */
    String createResumeUploadRecord(ResumeUploadReqDTO request);

    /**
     * 获取断点续传记录
     */
    ResumeUploadInfoDTO getResumeUploadRecord(String recordId);

    /**
     * 删除断点续传记录
     */
    void deleteResumeUploadRecord(String recordId);

    // ==================== 上传策略 ====================

    /**
     * 根据文件大小选择合适的上传方式
     */
    UploadStrategy selectUploadStrategy(long fileSize);

    /**
     * 计算分片大小
     */
    long calculatePartSize(long fileSize, int partCount);

    /**
     * 验证上传请求
     */
    ValidationResult validateUploadRequest(SimpleUploadReqDTO request);
}
