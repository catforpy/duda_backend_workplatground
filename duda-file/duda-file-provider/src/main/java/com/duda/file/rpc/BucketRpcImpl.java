package com.duda.file.rpc;

import com.duda.file.dto.bucket.*;
import com.duda.file.service.BucketService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

/**
 * Bucket RPC 实现类
 * 对外提供 Dubbo RPC 服务，注册到 Nacos
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@DubboService(version = "1.0.0", group = "DUDA_FILE_GROUP", timeout = 30000)
public class BucketRpcImpl implements IBucketRpc {

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.beans.factory.annotation.Qualifier("bucketServiceImpl")
    private BucketService bucketService;

    @Override
    public BucketDTO createBucket(CreateBucketReqDTO request) {
        log.info("【RPC】Creating bucket: {}", request.getBucketName());
        return bucketService.createBucket(request);
    }

    @Override
    public void deleteBucket(String bucketName, Long userId) {
        log.info("【RPC】Deleting bucket: {}", bucketName);
        bucketService.deleteBucket(bucketName, userId);
    }

    @Override
    public BucketDTO getBucketInfo(String bucketName) {
        log.info("【RPC】Getting bucket info: {}", bucketName);
        return bucketService.getBucketInfo(bucketName);
    }

    @Override
    public boolean bucketExists(String bucketName) {
        log.info("【RPC】Checking bucket existence: {}", bucketName);
        return bucketService.bucketExists(bucketName);
    }

    @Override
    public List<BucketDTO> listBuckets(Long userId, String keyName) {
        log.info("【RPC】Listing buckets for user: {}, keyName: {}", userId, keyName);
        return bucketService.listBuckets(userId, keyName);
    }

    // TODO: 需要创建 UpdateBucketConfigReqDTO 和 BucketConfigDTO
    // @Override
    // public void updateBucketConfig(String bucketName, UpdateBucketConfigReqDTO request) {
    //     log.info("【RPC】Updating bucket config: {}", bucketName);
    //     bucketService.updateBucketConfig(bucketName, request);
    // }
    //
    // @Override
    // public BucketConfigDTO getBucketConfig(String bucketName) {
    //     log.info("【RPC】Getting bucket config: {}", bucketName);
    //     return bucketService.getBucketConfig(bucketName);
    // }

    @Override
    public void setDefaultBucket(String bucketName, Long userId) {
        log.info("【RPC】Setting default bucket: {}", bucketName);
        bucketService.setDefaultBucket(bucketName, userId);
    }

    @Override
    public BucketDTO getDefaultBucket(Long userId) {
        log.info("【RPC】Getting default bucket for user: {}", userId);
        return bucketService.getDefaultBucket(userId);
    }

    @Override
    public BucketStatisticsDTO getBucketStatistics(String bucketName) {
        log.info("【RPC】Getting bucket statistics: {}", bucketName);
        return bucketService.getBucketStatistics(bucketName);
    }

    @Override
    public Boolean doesBucketExist(String bucketName) {
        log.info("【RPC】Checking if bucket exists: {}", bucketName);
        return bucketService.bucketExists(bucketName);
    }

    @Override
    public void setBucketAcl(String bucketName, com.duda.file.enums.AclType aclType) {
        log.info("【RPC】Setting bucket ACL: {}, aclType: {}", bucketName, aclType);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public com.duda.file.enums.AclType getBucketAcl(String bucketName) {
        log.info("【RPC】Getting bucket ACL: {}", bucketName);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public void setBucketTags(String bucketName, java.util.Map<String, String> tags) {
        log.info("【RPC】Setting bucket tags: {}", bucketName);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public java.util.Map<String, String> getBucketTags(String bucketName) {
        log.info("【RPC】Getting bucket tags: {}", bucketName);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public String getBucketLocation(String bucketName) {
        log.info("【RPC】Getting bucket location: {}", bucketName);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public void updateBucketQuota(String bucketName, Long maxSize, Integer maxCount) {
        log.info("【RPC】Updating bucket quota: {}", bucketName);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public java.util.List<BucketDTO> listAllOssBuckets() {
        log.info("【RPC】Listing all OSS buckets");
        throw new UnsupportedOperationException("此功能正在开发中");
    }
}
