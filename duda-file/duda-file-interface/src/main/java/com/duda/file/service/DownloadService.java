package com.duda.file.service;

import com.duda.file.dto.download.DownloadReqDTO;
import com.duda.file.dto.download.DownloadResultDTO;
import com.duda.file.common.exception.StorageException;

/**
 * 下载服务接口
 * 负责文件下载相关业务
 *
 * @author duda
 * @date 2025-03-13
 */
public interface DownloadService {

    /**
     * 下载文件
     *
     * @param request 下载请求
     * @return 下载结果
     * @throws StorageException 存储异常
     */
    DownloadResultDTO download(DownloadReqDTO request) throws StorageException;

    /**
     * 获取下载URL
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param expiration 过期时间(秒)
     * @return 预签名URL
     */
    String getDownloadUrl(String bucketName, String objectKey, Integer expiration);

    /**
     * 检查权限
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param userId 用户ID
     * @return 是否有权限
     */
    Boolean checkPermission(String bucketName, String objectKey, Long userId);
}
