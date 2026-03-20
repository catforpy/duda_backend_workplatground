package com.duda.file.api.service;

import com.duda.file.dto.download.DownloadReqDTO;
import com.duda.file.dto.download.DownloadResultDTO;

import java.io.InputStream;

/**
 * Download 服务接口
 * API 层服务接口，转发到 RPC
 *
 * @author DudaNexus
 * @since 2026-03-19
 */
public interface DownloadService {

    /**
     * 下载文件
     */
    DownloadResultDTO download(DownloadReqDTO request);

    /**
     * 获取下载URL
     */
    String getDownloadUrl(String bucketName, String objectKey, Integer expiration);

    /**
     * 检查权限
     */
    Boolean checkPermission(String bucketName, String objectKey, Long userId);

    /**
     * 获取文件流
     */
    InputStream getFileStream(String bucketName, String objectKey);

    /**
     * 获取文件字节数组
     */
    byte[] getFileBytes(String bucketName, String objectKey);

    /**
     * 获取文件大小
     */
    Long getFileSize(String bucketName, String objectKey);

    /**
     * 获取文件类型
     */
    String getContentType(String bucketName, String objectKey);

    /**
     * 验证下载权限
     */
    Boolean validateDownloadPermission(String bucketName, String objectKey, Long userId);
}
