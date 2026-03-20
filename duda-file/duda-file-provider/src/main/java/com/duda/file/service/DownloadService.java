package com.duda.file.service;

import com.duda.file.dto.download.DownloadReqDTO;
import com.duda.file.dto.download.DownloadResultDTO;

import java.io.InputStream;

/**
 * Download Service 接口
 * 内部业务逻辑接口
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
public interface DownloadService {

    DownloadResultDTO download(DownloadReqDTO request);

    String getDownloadUrl(String bucketName, String objectKey, Integer expiration);

    Boolean checkPermission(String bucketName, String objectKey, Long userId);

    InputStream getFileStream(String bucketName, String objectKey);

    byte[] getFileBytes(String bucketName, String objectKey);

    Long getFileSize(String bucketName, String objectKey);

    String getContentType(String bucketName, String objectKey);

    Boolean validateDownloadPermission(String bucketName, String objectKey, Long userId);
}

