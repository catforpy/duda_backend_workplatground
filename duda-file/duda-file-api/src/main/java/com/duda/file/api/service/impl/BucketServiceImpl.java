package com.duda.file.api.service.impl;

import com.duda.file.dto.bucket.*;
import com.duda.file.rpc.IBucketRpc;
import com.duda.file.api.service.BucketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bucket 本地服务实现
 * 通过 Dubbo RPC 调用 Provider 服务
 *
 * @author DudaNexus
 * @since 2026-03-18
 */
@Slf4j
@Service("bucketApiServiceImpl")
public class BucketServiceImpl implements BucketService {

    @DubboReference(
        version = "1.0.0",
        group = "DUDA_FILE_GROUP",
        check = false,
        timeout = 30000
    )
    private IBucketRpc bucketRpc;

    @Override
    public BucketDTO createBucket(CreateBucketReqDTO request) {
        log.info("【API Service】Creating bucket: {}", request.getBucketName());
        return bucketRpc.createBucket(request);
    }

    @Override
    public void deleteBucket(String bucketName, Long userId) {
        log.info("【API Service】Deleting bucket: {}", bucketName);
        bucketRpc.deleteBucket(bucketName, userId);
    }

    @Override
    public BucketDTO getBucketInfo(String bucketName) {
        log.info("【API Service】Getting bucket info: {}", bucketName);
        return bucketRpc.getBucketInfo(bucketName);
    }

    @Override
    public Boolean doesBucketExist(String bucketName) {
        log.info("【API Service】Checking bucket existence: {}", bucketName);
        // TODO: 待实现
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public List<BucketDTO> listBuckets(Long userId, String keyName) {
        log.info("【API Service】Listing buckets for user: {}, keyName: {}", userId, keyName);
        return bucketRpc.listBuckets(userId, keyName);
    }

    @Override
    public List<BucketDTO> listAllOssBuckets() {
        log.info("【API Service】Listing all OSS buckets");
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public void setBucketAcl(String bucketName, com.duda.file.enums.AclType aclType) {
        log.info("【API Service】Setting bucket ACL: {}, aclType: {}", bucketName, aclType);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public com.duda.file.enums.AclType getBucketAcl(String bucketName) {
        log.info("【API Service】Getting bucket ACL: {}", bucketName);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public void setBucketTags(String bucketName, Map<String, String> tags) {
        log.info("【API Service】Setting bucket tags: {}", bucketName);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public Map<String, String> getBucketTags(String bucketName) {
        log.info("【API Service】Getting bucket tags: {}", bucketName);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public String getBucketLocation(String bucketName) {
        log.info("【API Service】Getting bucket location: {}", bucketName);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public void updateBucketQuota(String bucketName, Long maxSize, Integer maxCount) {
        log.info("【API Service】Updating bucket quota: {}", bucketName);
        throw new UnsupportedOperationException("此功能正在开发中");
    }

    @Override
    public BucketStatisticsDTO getBucketStatistics(String bucketName) {
        log.info("【API Service】Getting bucket statistics: {}", bucketName);
        return bucketRpc.getBucketStatistics(bucketName);
    }

    @Override
    public Map<String, Object> getBucketCapacity(String bucketName) {
        log.info("【API Service】Getting bucket capacity: {}", bucketName);

        BucketStatisticsDTO statistics = bucketRpc.getBucketStatistics(bucketName);

        Map<String, Object> capacity = new HashMap<>();
        capacity.put("bucketName", bucketName);
        capacity.put("storageUsed", statistics.getStorageUsed());
        capacity.put("fileCount", statistics.getFileCount());
        capacity.put("storageQuota", statistics.getStorageQuota());
        capacity.put("maxFileCount", null);
        capacity.put("storageUsedPercent", statistics.getUsagePercentage());
        capacity.put("fileCountUsedPercent", null);

        return capacity;
    }
}
