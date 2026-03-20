package com.duda.file.service.impl;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.duda.file.provider.entity.BucketConfig;
import com.duda.file.provider.entity.ObjectMetadata;
import com.duda.file.provider.mapper.BucketConfigMapper;
import com.duda.file.provider.mapper.ObjectMetadataMapper;
import com.duda.file.service.OSSSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * OSS元数据同步服务实现
 *
 * @author DudaNexus
 * @since 2026-03-19
 */
@Slf4j
@Service
public class OSSSyncServiceImpl implements OSSSyncService {

    @Autowired
    private ObjectMetadataMapper objectMetadataMapper;

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    /**
     * 每次批量查询的对象数量
     */
    private static final int MAX_KEYS_PER_BATCH = 100;

    /**
     * 同步指定Bucket的对象元数据到数据库
     *
     * @param bucketName Bucket名称
     * @return 同步的对象数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncBucketObjects(String bucketName) {
        log.info("开始同步Bucket对象元数据: {}", bucketName);

        try {
            // 1. 检查Bucket配置是否存在
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                log.warn("Bucket配置不存在: {}", bucketName);
                return 0;
            }

            // 2. 根据Bucket配置创建OSS客户端
            OSS ossClient = createOSSClient(bucketConfig);

            int totalSynced = 0;
            int totalInserted = 0;
            int totalUpdated = 0;
            int totalSkipped = 0;

            // 3. 分批列出所有对象
            String nextMarker = null;
            ObjectListing objectListing;
            do {
                // 调用OSS listObjects接口
                ListObjectsRequest listRequest = new ListObjectsRequest(bucketName);
                listRequest.setMarker(nextMarker);
                listRequest.setMaxKeys(MAX_KEYS_PER_BATCH);
                objectListing = ossClient.listObjects(listRequest);

                // 处理当前批次的对象
                List<OSSObjectSummary> summaries = objectListing.getObjectSummaries();
                for (OSSObjectSummary summary : summaries) {
                    try {
                        int result = syncOneObject(bucketName, summary);
                        totalSynced++;

                        if (result == 1) {
                            totalInserted++;
                        } else if (result == 2) {
                            totalUpdated++;
                        } else {
                            totalSkipped++;
                        }
                    } catch (Exception e) {
                        log.error("同步对象失败: {}/{}", bucketName, summary.getKey(), e);
                    }
                }

                // 获取下一批的标记
                nextMarker = objectListing.getNextMarker();

                log.debug("已同步 {} 个对象 (新增: {}, 更新: {}, 跳过: {})",
                    totalSynced, totalInserted, totalUpdated, totalSkipped);

            } while (objectListing.isTruncated());

            // 关闭OSS客户端
            ossClient.shutdown();

            log.info("Bucket {} 同步完成: 总计={}, 新增={}, 更新={}, 跳过={}",
                bucketName, totalSynced, totalInserted, totalUpdated, totalSkipped);

            return totalSynced;

        } catch (Exception e) {
            log.error("同步Bucket对象元数据失败: {}", bucketName, e);
            throw new RuntimeException("同步失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步所有Bucket的对象元数据到数据库
     *
     * @return 同步的对象总数
     */
    @Override
    public int syncAllBuckets() {
        log.info("开始同步所有Bucket的对象元数据");

        try {
            // 1. 获取所有激活的Bucket配置
            List<BucketConfig> buckets = bucketConfigMapper.selectActiveBuckets();
            if (buckets == null || buckets.isEmpty()) {
                log.warn("没有找到激活的Bucket配置");
                return 0;
            }

            int totalCount = 0;
            Map<String, Integer> results = new HashMap<>();

            // 2. 逐个同步Bucket
            for (BucketConfig bucket : buckets) {
                try {
                    int count = syncBucketObjects(bucket.getBucketName());
                    totalCount += count;
                    results.put(bucket.getBucketName(), count);
                } catch (Exception e) {
                    log.error("同步Bucket失败: {}", bucket.getBucketName(), e);
                    results.put(bucket.getBucketName(), -1);
                }
            }

            log.info("所有Bucket同步完成: 总数={}, 结果={}", totalCount, results);
            return totalCount;

        } catch (Exception e) {
            log.error("同步所有Bucket对象元数据失败", e);
            throw new RuntimeException("同步失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步指定Bucket的指定前缀的对象
     *
     * @param bucketName Bucket名称
     * @param prefix 对象前缀
     * @return 同步的对象数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncBucketObjectsByPrefix(String bucketName, String prefix) {
        log.info("开始同步Bucket对象元数据: {} (前缀: {})", bucketName, prefix);

        try {
            // 1. 检查Bucket配置是否存在
            BucketConfig bucketConfig = bucketConfigMapper.selectByBucketName(bucketName);
            if (bucketConfig == null) {
                log.warn("Bucket配置不存在: {}", bucketName);
                return 0;
            }

            // 2. 根据Bucket配置创建OSS客户端
            OSS ossClient = createOSSClient(bucketConfig);

            int totalSynced = 0;
            int totalInserted = 0;
            int totalUpdated = 0;
            int totalSkipped = 0;

            // 3. 分批列出指定前缀的对象
            String nextMarker = null;
            ObjectListing objectListing;
            do {
                // 调用OSS listObjects接口，指定前缀
                ListObjectsRequest listRequest = new ListObjectsRequest(bucketName);
                listRequest.setPrefix(prefix);
                listRequest.setMarker(nextMarker);
                listRequest.setMaxKeys(MAX_KEYS_PER_BATCH);
                objectListing = ossClient.listObjects(listRequest);

                // 处理当前批次的对象
                List<OSSObjectSummary> summaries = objectListing.getObjectSummaries();
                for (OSSObjectSummary summary : summaries) {
                    try {
                        int result = syncOneObject(bucketName, summary);
                        totalSynced++;

                        if (result == 1) {
                            totalInserted++;
                        } else if (result == 2) {
                            totalUpdated++;
                        } else {
                            totalSkipped++;
                        }
                    } catch (Exception e) {
                        log.error("同步对象失败: {}/{}", bucketName, summary.getKey(), e);
                    }
                }

                // 获取下一批的标记
                nextMarker = objectListing.getNextMarker();

                log.debug("已同步 {} 个对象 (新增: {}, 更新: {}, 跳过: {})",
                    totalSynced, totalInserted, totalUpdated, totalSkipped);

            } while (objectListing.isTruncated());

            // 关闭OSS客户端
            ossClient.shutdown();

            log.info("Bucket {} (前缀: {}) 同步完成: 总计={}, 新增={}, 更新={}, 跳过={}",
                bucketName, prefix, totalSynced, totalInserted, totalUpdated, totalSkipped);

            return totalSynced;

        } catch (Exception e) {
            log.error("同步Bucket对象元数据失败: {} (前缀: {})", bucketName, prefix, e);
            throw new RuntimeException("同步失败: " + e.getMessage(), e);
        }
    }

    /**
     * 同步单个对象元数据
     *
     * @param bucketName Bucket名称
     * @param summary OSS对象摘要
     * @return 0-跳过, 1-新增, 2-更新
     */
    private int syncOneObject(String bucketName, OSSObjectSummary summary) {
        String objectKey = summary.getKey();

        // 1. 查询数据库中是否已存在
        ObjectMetadata existing = objectMetadataMapper.selectByBucketAndKey(bucketName, objectKey);

        // 2. 判断是否需要更新
        boolean needInsert = (existing == null);
        boolean needUpdate = false;

        if (!needInsert && existing != null) {
            // 比较最后修改时间，以OSS为准
            LocalDateTime ossLastModified = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(summary.getLastModified().getTime()),
                ZoneId.systemDefault()
            );

            // 如果OSS的修改时间比数据库新，需要更新
            if (ossLastModified.isAfter(existing.getUpdatedTime())) {
                needUpdate = true;
            }
        }

        // 3. 跳过无需更新的对象
        if (!needInsert && !needUpdate) {
            return 0;
        }

        // 4. 构建元数据对象
        ObjectMetadata metadata = buildMetadataFromOSS(bucketName, summary);

        // 5. 插入或更新
        if (needInsert) {
            objectMetadataMapper.insert(metadata);
            log.debug("新增对象元数据: {}/{}", bucketName, objectKey);
            return 1;
        } else {
            metadata.setId(existing.getId());
            objectMetadataMapper.update(metadata);
            log.debug("更新对象元数据: {}/{}", bucketName, objectKey);
            return 2;
        }
    }

    /**
     * 从OSS对象摘要构建数据库元数据对象
     *
     * @param bucketName Bucket名称
     * @param summary OSS对象摘要
     * @return 数据库元数据对象
     */
    private ObjectMetadata buildMetadataFromOSS(String bucketName, OSSObjectSummary summary) {
        ObjectMetadata metadata = new ObjectMetadata();

        // 基本信息
        metadata.setBucketName(bucketName);
        metadata.setObjectKey(summary.getKey());
        metadata.setFileSize(summary.getSize());
        metadata.setEtag(summary.getETag());

        // 存储类型
        if (summary.getStorageClass() != null) {
            metadata.setStorageClass(summary.getStorageClass().toString());
        } else {
            metadata.setStorageClass("STANDARD");
        }

        // 对象类型
        metadata.setObjectType("NORMAL");

        // 判断是否为目录（以/结尾）
        metadata.setIsDirectory(summary.getKey().endsWith("/"));
        metadata.setIsSymlink(false);

        // 时间信息
        LocalDateTime lastModified = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(summary.getLastModified().getTime()),
            ZoneId.systemDefault()
        );
        metadata.setUpdatedTime(lastModified);
        metadata.setUploadTime(lastModified);
        metadata.setCreatedTime(lastModified);

        // 状态
        metadata.setStatus("active");

        return metadata;
    }

    /**
     * 根据Bucket配置创建OSS客户端
     *
     * @param bucketConfig Bucket配置
     * @return OSS客户端实例
     */
    private OSS createOSSClient(BucketConfig bucketConfig) {
        // 解密密钥
        String accessKeyId = bucketConfig.getAccessKeyId();
        String accessKeySecret = bucketConfig.getAccessKeySecret();

        // 构建endpoint
        String endpoint = buildEndpoint(bucketConfig.getEndpoint(), bucketConfig.getRegion());

        // 创建CredentialsProvider
        DefaultCredentialProvider credentialsProvider =
            new DefaultCredentialProvider(accessKeyId, accessKeySecret);

        // 创建ClientBuilderConfiguration
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        // 创建OSS客户端
        return new OSSClientBuilder().build(endpoint, credentialsProvider, clientBuilderConfiguration);
    }

    /**
     * 构建endpoint URL
     *
     * @param endpoint Endpoint
     * @param region Region
     * @return 完整的endpoint URL
     */
    private String buildEndpoint(String endpoint, String region) {
        if (endpoint != null && !endpoint.isEmpty()) {
            return endpoint;
        }

        // 如果region为空，使用默认值
        if (region == null || region.isEmpty()) {
            region = "oss-cn-hangzhou";
        }

        // 清理region中的oss-前缀（如果存在）
        String cleanRegion = region;
        if (region.startsWith("oss-")) {
            cleanRegion = region.substring(4);
            log.warn("Region字段包含'oss-'前缀,已自动去除: {} -> {}", region, cleanRegion);
        }
        return "https://oss-" + cleanRegion + ".aliyuncs.com";
    }
}
