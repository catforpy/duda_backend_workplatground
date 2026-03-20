package com.duda.file.rpc;

import com.duda.file.dto.download.DownloadReqDTO;
import com.duda.file.dto.download.DownloadResultDTO;
import com.duda.file.service.DownloadService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.InputStream;

/**
 * Download RPC 实现类
 * 对外提供 Dubbo RPC 服务，注册到 Nacos
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@DubboService(version = "1.0.0", group = "DUDA_FILE_GROUP", timeout = 30000)
public class DownloadRpcImpl implements IDownloadRpc {

    @jakarta.annotation.Resource
    private com.duda.file.service.impl.DownloadServiceImpl downloadServiceImpl;
    private DownloadService downloadService;

    @Override
    public DownloadResultDTO download(DownloadReqDTO request) {
        log.info("【RPC】Download: bucket={}, object={}",
            request.getBucketName(), request.getObjectKey());
        return downloadServiceImpl.download(request);
    }

    @Override
    public String getDownloadUrl(String bucketName, String objectKey, Integer expiration) {
        log.info("【RPC】Get download URL: bucket={}, object={}", bucketName, objectKey);
        return downloadServiceImpl.getDownloadUrl(bucketName, objectKey, expiration);
    }

    @Override
    public Boolean checkPermission(String bucketName, String objectKey, Long userId) {
        log.info("【RPC】Check permission: bucket={}, object={}, userId={}",
            bucketName, objectKey, userId);
        return downloadServiceImpl.checkPermission(bucketName, objectKey, userId);
    }

    @Override
    public InputStream getFileStream(String bucketName, String objectKey) {
        log.info("【RPC】Get file stream: bucket={}, object={}", bucketName, objectKey);
        return downloadServiceImpl.getFileStream(bucketName, objectKey);
    }

    @Override
    public byte[] getFileBytes(String bucketName, String objectKey) {
        log.info("【RPC】Get file bytes: bucket={}, object={}", bucketName, objectKey);
        return downloadServiceImpl.getFileBytes(bucketName, objectKey);
    }

    @Override
    public Long getFileSize(String bucketName, String objectKey) {
        log.info("【RPC】Get file size: bucket={}, object={}", bucketName, objectKey);
        return downloadServiceImpl.getFileSize(bucketName, objectKey);
    }

    @Override
    public String getContentType(String bucketName, String objectKey) {
        log.info("【RPC】Get content type: bucket={}, object={}", bucketName, objectKey);
        return downloadServiceImpl.getContentType(bucketName, objectKey);
    }

    @Override
    public Boolean validateDownloadPermission(String bucketName, String objectKey, Long userId) {
        log.info("【RPC】Validate download permission: bucket={}, object={}, userId={}",
            bucketName, objectKey, userId);
        return downloadServiceImpl.validateDownloadPermission(bucketName, objectKey, userId);
    }
}
