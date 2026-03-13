package com.duda.file.service;

import com.duda.file.dto.UploadRequest;
import com.duda.file.dto.UploadResult;
import com.duda.file.dto.FileInfo;
import com.duda.file.dto.PresignedUrlResult;
import com.duda.file.dto.STSCredential;
import com.duda.file.dto.bucket.BucketDTO;
import com.duda.file.dto.object.ObjectMetadataDTO;
import com.duda.file.dto.upload.UploadResultDTO;
import com.duda.file.dto.upload.SimpleUploadReqDTO;

import java.io.InputStream;
import java.util.List;

/**
 * 存储服务统一抽象接口
 * 所有云存储适配器必须实现此接口
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
public interface StorageService {

    /**
     * 获取存储类型
     */
    String getStorageType();

    /**
     * 上传文件
     *
     * @param request 上传请求
     * @return 上传结果
     */
    UploadResult uploadFile(UploadRequest request);

    /**
     * 下载文件
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @return 文件输入流
     */
    InputStream downloadFile(String bucketName, String objectKey);

    /**
     * 删除文件
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     */
    void deleteFile(String bucketName, String objectKey);

    /**
     * 批量删除文件
     *
     * @param bucketName Bucket名称
     * @param objectKeys 对象键列表
     * @return 删除结果列表
     */
    List<String> batchDeleteFiles(String bucketName, List<String> objectKeys);

    /**
     * 复制文件
     *
     * @param sourceBucket 源存储空间
     * @param sourceKey 源对象键
     * @param targetBucket 目标存储空间
     * @param targetKey 目标对象键
     */
    void copyFile(String sourceBucket, String sourceKey,
                  String targetBucket, String targetKey);

    /**
     * 移动文件
     *
     * @param sourceBucket 源存储空间
     * @param sourceKey 源对象键
     * @param targetBucket 目标存储空间
     * @param targetKey 目标对象键
     */
    void moveFile(String sourceBucket, String sourceKey,
                  String targetBucket, String targetKey);

    /**
     * 获取文件信息
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @return 文件元信息
     */
    FileInfo getFileInfo(String bucketName, String objectKey);

    /**
     * 生成预签名URL（用于前端直传或下载）
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @param expireSeconds 过期时间（秒）
     * @param method HTTP方法（GET/PUT/DELETE）
     * @return 预签名URL
     */
    PresignedUrlResult generatePresignedUrl(String bucketName, String objectKey,
                                            Integer expireSeconds, String method);

    /**
     * 检查文件是否存在
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @return 是否存在
     */
    boolean doesObjectExist(String bucketName, String objectKey);

    /**
     * 获取文件URL
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @param expireSeconds 过期时间（秒）
     * @return 文件URL
     */
    String getFileUrl(String bucketName, String objectKey, Integer expireSeconds);
}
