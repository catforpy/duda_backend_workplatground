package com.duda.file.service;

import com.duda.file.dto.upload.*;
import com.duda.file.dto.object.ObjectMetadataDTO;
import com.duda.file.common.exception.StorageException;

import java.io.InputStream;
import java.util.Map;

/**
 * 上传服务接口
 * 负责文件上传相关业务
 *
 * @author duda
 * @date 2025-03-13
 */
public interface UploadService {

    // ==================== 简单上传 ====================

    /**
     * 简单上传(适用于小文件,通常<5GB)
     *
     * @param request 上传请求
     * @return 上传结果
     * @throws StorageException 存储异常
     */
    UploadResultDTO simpleUpload(SimpleUploadReqDTO request) throws StorageException;

    /**
     * 字节数组上传
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param data 字节数组
     * @param metadata 元数据
     * @return 上传结果
     * @throws StorageException 存储异常
     */
    UploadResultDTO uploadBytes(String bucketName, String objectKey, byte[] data, ObjectMetadataDTO metadata) throws StorageException;

    // ==================== 分片上传 ====================

    /**
     * 初始化分片上传
     *
     * @param request 初始化请求
     * @return 上传ID
     * @throws StorageException 存储异常
     */
    String initiateMultipartUpload(InitiateMultipartUploadReqDTO request) throws StorageException;

    /**
     * 上传分片
     *
     * @param request 上传分片请求
     * @return 分片ETag
     * @throws StorageException 存储异常
     */
    UploadPartResultDTO uploadPart(UploadPartReqDTO request) throws StorageException;

    /**
     * 完成分片上传
     *
     * @param request 完成请求
     * @return 上传结果
     * @throws StorageException 存储异常
     */
    UploadResultDTO completeMultipartUpload(CompleteMultipartUploadReqDTO request) throws StorageException;

    /**
     * 取消分片上传
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param uploadId 上传ID
     * @throws StorageException 存储异常
     */
    void abortMultipartUpload(String bucketName, String objectKey, String uploadId) throws StorageException;

    /**
     * 列出已上传的分片
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param uploadId 上传ID
     * @return 分片列表
     */
    ListPartsResultDTO listParts(String bucketName, String objectKey, String uploadId);

    // ==================== 追加上传 ====================

    /**
     * 追加上传(适用于日志文件等场景)
     *
     * @param request 追加上传请求
     * @return 追加位置
     * @throws StorageException 存储异常
     */
    Long appendObject(AppendObjectReqDTO request) throws StorageException;

    // ==================== 客户端直传 ====================

    /**
     * 获取STS临时凭证(用于客户端直传)
     *
     * @param request STS请求
     * @return STS凭证
     */
    STSCredentialsDTO getSTSForClientUpload(GetSTSReqDTO request);

    /**
     * 生成PostObject表单(用于Web表单上传)
     *
     * @param request 表单请求
     * @return 表单数据(包含URL和表单字段)
     */
    Map<String, String> generatePostObjectForm(PostObjectFormReqDTO request);

    /**
     * 生成预签名URL(用于客户端上传/下载)
     *
     * @param request 预签名请求
     * @return 预签名URL
     */
    String generatePresignedUrl(PresignedUrlReqDTO request);

    /**
     * 生成带回调的上传凭证
     *
     * @param request 回调上传请求
     * @return 上传凭证信息
     */
    CallbackUploadDTO generateCallbackUpload(CallbackUploadReqDTO request);

    // ==================== 断点续传 ====================

    /**
     * 创建断点续传记录
     *
     * @param request 断点续传请求
     * @return 断点续传ID
     */
    String createResumeUploadRecord(ResumeUploadReqDTO request);

    /**
     * 获取断点续传记录
     *
     * @param recordId 断点续传ID
     * @return 断点续传信息
     */
    ResumeUploadInfoDTO getResumeUploadRecord(String recordId);

    /**
     * 删除断点续传记录
     *
     * @param recordId 断点续传ID
     */
    void deleteResumeUploadRecord(String recordId);

    // ==================== 上传策略 ====================

    /**
     * 根据文件大小选择合适的上传方式
     *
     * @param fileSize 文件大小
     * @return 推荐的上传策略
     */
    UploadStrategy selectUploadStrategy(long fileSize);

    /**
     * 计算分片大小
     *
     * @param fileSize 文件大小
     * @param partCount 分片数量
     * @return 分片大小
     */
    long calculatePartSize(long fileSize, int partCount);

    /**
     * 验证上传请求
     *
     * @param request 上传请求
     * @return 验证结果
     */
    ValidationResult validateUploadRequest(SimpleUploadReqDTO request);
}
