package com.duda.file.api.service.impl;

import com.duda.file.dto.download.DownloadReqDTO;
import com.duda.file.dto.download.DownloadResultDTO;
import com.duda.file.api.service.DownloadService;
import com.duda.file.rpc.IDownloadRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Download 本地服务实现
 * 转发调用到 RPC 服务
 *
 * @author DudaNexus
 * @since 2026-03-19
 */
@Slf4j
@Service("downloadApiServiceImpl")
public class DownloadServiceImpl implements DownloadService {

    @DubboReference(version = "1.0.0", group = "DUDA_FILE_GROUP", check = false, timeout = 30000)
    private IDownloadRpc downloadRpc;

    @Override
    public DownloadResultDTO download(DownloadReqDTO request) {
        log.info("【API Service】Download: {}/{}", request.getBucketName(), request.getObjectKey());
        return downloadRpc.download(request);
    }

    @Override
    public String getDownloadUrl(String bucketName, String objectKey, Integer expiration) {
        log.info("【API Service】Get download URL: {}/{}", bucketName, objectKey);
        return downloadRpc.getDownloadUrl(bucketName, objectKey, expiration);
    }

    @Override
    public Boolean checkPermission(String bucketName, String objectKey, Long userId) {
        log.debug("【API Service】Check permission: {}/{}", bucketName, objectKey);
        return downloadRpc.checkPermission(bucketName, objectKey, userId);
    }

    @Override
    public InputStream getFileStream(String bucketName, String objectKey) {
        log.info("【API Service】Get file stream: {}/{}", bucketName, objectKey);
        return downloadRpc.getFileStream(bucketName, objectKey);
    }

    @Override
    public byte[] getFileBytes(String bucketName, String objectKey) {
        log.info("【API Service】Get file bytes: {}/{}", bucketName, objectKey);
        return downloadRpc.getFileBytes(bucketName, objectKey);
    }

    @Override
    public Long getFileSize(String bucketName, String objectKey) {
        log.debug("【API Service】Get file size: {}/{}", bucketName, objectKey);
        return downloadRpc.getFileSize(bucketName, objectKey);
    }

    @Override
    public String getContentType(String bucketName, String objectKey) {
        log.debug("【API Service】Get content type: {}/{}", bucketName, objectKey);
        return downloadRpc.getContentType(bucketName, objectKey);
    }

    @Override
    public Boolean validateDownloadPermission(String bucketName, String objectKey, Long userId) {
        log.debug("【API Service】Validate download permission: {}/{}", bucketName, objectKey);
        return downloadRpc.validateDownloadPermission(bucketName, objectKey, userId);
    }
}
