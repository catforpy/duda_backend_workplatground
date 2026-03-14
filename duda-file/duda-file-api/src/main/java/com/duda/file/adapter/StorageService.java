package com.duda.file.adapter;

import com.duda.file.common.exception.StorageException;
import com.duda.file.dto.bucket.BucketDTO;
import com.duda.file.dto.object.ObjectDTO;
import com.duda.file.dto.object.ObjectMetadataDTO;
import com.duda.file.dto.upload.UploadResultDTO;
import com.duda.file.dto.download.DownloadResultDTO;
import com.duda.file.enums.StorageType;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 统一存储服务接口
 * 使用适配器模式屏蔽不同云厂商的差异
 * <p>
 * 职责：
 * 1. 定义统一的存储服务操作接口
 * 2. 屏蔽Aliyun OSS、Tencent COS、Qiniu Kodo等云厂商的API差异
 * 3. 为上层业务服务提供统一的访问入口
 * <p>
 * 实现类：
 * - AliyunOSSAdapter: 阿里云OSS适配器
 * - TencentCOSAdapter: 腾讯云COS适配器
 * - QiniuKodoAdapter: 七牛云Kodo适配器
 * - MinIOAdapter: MinIO适配器
 *
 * @author duda
 * @date 2025-03-13
 */
public interface StorageService {

    /**
     * 获取存储类型
     *
     * @return 存储类型
     */
    StorageType getStorageType();

    // ==================== Bucket操作 ====================

    /**
     * 创建存储空间(Bucket)
     *
     * @param bucketName 存储空间名称
     * @param region 区域
     * @param config 配置参数
     * @return Bucket信息
     * @throws StorageException 存储异常
     */
    BucketDTO createBucket(String bucketName, String region, Map<String, Object> config) throws StorageException;

    /**
     * 删除存储空间
     *
     * @param bucketName 存储空间名称
     * @throws StorageException 存储异常
     */
    void deleteBucket(String bucketName) throws StorageException;

    /**
     * 判断存储空间是否存在
     *
     * @param bucketName 存储空间名称
     * @return 是否存在
     */
    boolean doesBucketExist(String bucketName);

    /**
     * 获取存储空间信息
     *
     * @param bucketName 存储空间名称
     * @return Bucket信息
     */
    BucketDTO getBucketInfo(String bucketName);

    /**
     * 列出所有存储空间
     *
     * @return Bucket列表
     */
    List<BucketDTO> listBuckets();

    /**
     * 设置存储空间ACL
     *
     * @param bucketName 存储空间名称
     * @param acl ACL值
     */
    void setBucketAcl(String bucketName, String acl);

    /**
     * 获取存储空间ACL
     *
     * @param bucketName 存储空间名称
     * @return ACL值
     */
    String getBucketAcl(String bucketName);

    /**
     * 获取存储空间所在区域
     *
     * @param bucketName 存储空间名称
     * @return 区域
     */
    String getBucketLocation(String bucketName);

    // ==================== Object操作 ====================

    /**
     * 上传对象(简单上传)
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param inputStream 输入流
     * @param metadata 元数据
     * @return 上传结果
     * @throws StorageException 存储异常
     */
    UploadResultDTO uploadObject(String bucketName, String objectKey, InputStream inputStream, ObjectMetadataDTO metadata) throws StorageException;

    /**
     * 下载对象
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @return 下载结果
     * @throws StorageException 存储异常
     */
    DownloadResultDTO downloadObject(String bucketName, String objectKey) throws StorageException;

    /**
     * 删除对象
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @throws StorageException 存储异常
     */
    void deleteObject(String bucketName, String objectKey) throws StorageException;

    /**
     * 批量删除对象
     *
     * @param bucketName 存储空间名称
     * @param objectKeys 对象键列表
     * @return 删除成功的对象数量
     * @throws StorageException 存储异常
     */
    int deleteObjects(String bucketName, List<String> objectKeys) throws StorageException;

    /**
     * 判断对象是否存在
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @return 是否存在
     */
    boolean doesObjectExist(String bucketName, String objectKey);

    /**
     * 获取对象信息
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @return 对象信息
     */
    ObjectDTO getObjectInfo(String bucketName, String objectKey);

    /**
     * 获取对象元数据
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @return 对象元数据
     */
    ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey);

    /**
     * 设置对象元数据
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param metadata 元数据
     */
    void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata);

    /**
     * 复制对象
     *
     * @param sourceBucketName 源存储空间
     * @param sourceObjectKey 源对象键
     * @param destinationBucketName 目标存储空间
     * @param destinationObjectKey 目标对象键
     * @throws StorageException 存储异常
     */
    void copyObject(String sourceBucketName, String sourceObjectKey, String destinationBucketName, String destinationObjectKey) throws StorageException;

    /**
     * 列出对象
     *
     * @param bucketName 存储空间名称
     * @param prefix 前缀
     * @param maxKeys 最大对象数量
     * @param marker 分页标记
     * @param delimiter 分隔符
     * @return 对象列表
     */
    List<ObjectDTO> listObjects(String bucketName, String prefix, Integer maxKeys, String marker, String delimiter);

    /**
     * 设置对象ACL
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param acl ACL值
     */
    void setObjectAcl(String bucketName, String objectKey, String acl);

    /**
     * 获取对象ACL
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @return ACL值
     */
    String getObjectAcl(String bucketName, String objectKey);

    // ==================== 签名URL操作 ====================

    /**
     * 生成预签名URL
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param expiration 过期时间(秒)
     * @param method HTTP方法
     * @return 预签名URL
     */
    String generatePresignedUrl(String bucketName, String objectKey, int expiration, String method);

    /**
     * 生成上传表单
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param expiration 过期时间(秒)
     * @return 表单数据
     */
    Map<String, String> generateUploadFormData(String bucketName, String objectKey, int expiration);

    // ==================== 分片上传操作 ====================

    /**
     * 初始化分片上传
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param metadata 元数据
     * @return 上传ID
     */
    String initiateMultipartUpload(String bucketName, String objectKey, ObjectMetadataDTO metadata);

    /**
     * 上传分片
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param uploadId 上传ID
     * @param partNumber 分片号(从1开始)
     * @param inputStream 输入流
     * @param partSize 分片大小
     * @return 分片ETag
     */
    String uploadPart(String bucketName, String objectKey, String uploadId, int partNumber, InputStream inputStream, long partSize);

    /**
     * 完成分片上传
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param uploadId 上传ID
     * @param partETags 分片ETag列表(partNumber, eTag)
     * @return 完成结果
     */
    UploadResultDTO completeMultipartUpload(String bucketName, String objectKey, String uploadId, Map<Integer, String> partETags);

    /**
     * 取消分片上传
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param uploadId 上传ID
     */
    void abortMultipartUpload(String bucketName, String objectKey, String uploadId);

    /**
     * 列出已上传的分片
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param uploadId 上传ID
     * @return 分片列表(partNumber, eTag, size)
     */
    List<Map<String, Object>> listParts(String bucketName, String objectKey, String uploadId);

    /**
     * 列出未完成的分片上传
     *
     * @param bucketName 存储空间名称
     * @param prefix 前缀
     * @param maxUploads 最大上传数量
     * @return 分片上传列表
     */
    List<Map<String, Object>> listMultipartUploads(String bucketName, String prefix, Integer maxUploads);

    // ==================== 追加上传 ====================

    /**
     * 追加上传
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param inputStream 输入流
     * @param position 追加位置
     * @param metadata 元数据
     * @return 下一次追加位置
     * @throws StorageException 存储异常
     */
    Long appendObject(String bucketName, String objectKey, InputStream inputStream, Long position, ObjectMetadataDTO metadata) throws StorageException;

    // ==================== 其他操作 ====================

    /**
     * 恢复归档对象
     *
     * @param bucketName 存储空间名称
     * @param objectKey 对象键
     * @param days 恢复天数
     */
    void restoreObject(String bucketName, String objectKey, int days);

    /**
     * 创建软链接
     *
     * @param bucketName 存储空间名称
     * @param symlinkKey 软链接键
     * @param targetKey 目标对象键
     */
    void createSymlink(String bucketName, String symlinkKey, String targetKey);

    /**
     * 获取软链接目标
     *
     * @param bucketName 存储空间名称
     * @param symlinkKey 软链接键
     * @return 目标对象键
     */
    String getSymlink(String bucketName, String symlinkKey);

    // ==================== Bucket高级配置管理 ====================

    /**
     * 设置Bucket生命周期规则
     *
     * @param bucketName Bucket名称
     * @param config 生命周期配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketLifecycleResultDTO setBucketLifecycle(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketLifecycleReqDTO config
    ) throws StorageException;

    /**
     * 设置Bucket CORS规则
     *
     * @param bucketName Bucket名称
     * @param config CORS配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketCORSResultDTO setBucketCORS(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketCORSReqDTO config
    ) throws StorageException;

    /**
     * 设置Bucket防盗链规则
     *
     * @param bucketName Bucket名称
     * @param config 防盗链配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketRefererResultDTO setBucketReferer(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketRefererReqDTO config
    ) throws StorageException;

    /**
     * 设置Bucket策略
     *
     * @param bucketName Bucket名称
     * @param config Policy配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketPolicyResultDTO setBucketPolicy(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketPolicyReqDTO config
    ) throws StorageException;

    /**
     * 设置Bucket跨区域复制
     *
     * @param bucketName Bucket名称
     * @param config 复制配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketReplicationResultDTO setBucketReplication(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketReplicationReqDTO config
    ) throws StorageException;

    /**
     * 设置Bucket版本控制
     *
     * @param bucketName Bucket名称
     * @param config 版本控制配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketVersioningResultDTO setBucketVersioning(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketVersioningReqDTO config
    ) throws StorageException;

    /**
     * 设置Bucket静态网站托管
     *
     * @param bucketName Bucket名称
     * @param config 网站托管配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketWebsiteResultDTO setBucketWebsite(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketWebsiteReqDTO config
    ) throws StorageException;

    /**
     * 设置Bucket日志转存
     *
     * @param bucketName Bucket名称
     * @param config 日志转存配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketLoggingResultDTO setBucketLogging(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketLoggingReqDTO config
    ) throws StorageException;

    /**
     * 设置Bucket合规保留策略(WORM)
     *
     * @param bucketName Bucket名称
     * @param config WORM配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketWORMResultDTO setBucketWORM(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketWORMReqDTO config
    ) throws StorageException;

    /**
     * 设置Bucket访问跟踪
     *
     * @param bucketName Bucket名称
     * @param config 访问跟踪配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketAccessMonitorResultDTO setBucketAccessMonitor(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketAccessMonitorReqDTO config
    ) throws StorageException;

    /**
     * 设置Bucket存储空间清单
     *
     * @param bucketName Bucket名称
     * @param config 清单配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketInventoryResultDTO setBucketInventory(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketInventoryReqDTO config
    ) throws StorageException;

    /**
     * 设置Bucket传输加速
     *
     * @param bucketName Bucket名称
     * @param config 传输加速配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.bucket.SetBucketTransferAccelerationResultDTO setBucketTransferAcceleration(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketTransferAccelerationReqDTO config
    ) throws StorageException;

    // ==================== 对象标签管理 ====================

    /**
     * 设置对象标签
     *
     * @param bucketName Bucket名称
     * @param config 对象标签配置
     * @return 设置结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.object.SetObjectTaggingResultDTO setObjectTagging(
        String bucketName,
        com.duda.file.dto.object.SetObjectTaggingReqDTO config
    ) throws StorageException;

    /**
     * 获取对象标签
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @return 对象标签结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.object.GetObjectTaggingResultDTO getObjectTagging(
        String bucketName,
        String objectKey
    ) throws StorageException;

    /**
     * 获取对象标签(指定版本)
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @param versionId 版本ID
     * @return 对象标签结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.object.GetObjectTaggingResultDTO getObjectTagging(
        String bucketName,
        String objectKey,
        String versionId
    ) throws StorageException;

    /**
     * 删除对象标签
     *
     * @param bucketName Bucket名称
     * @param objectKey 对象键
     * @throws StorageException 存储异常
     */
    void deleteObjectTagging(String bucketName, String objectKey) throws StorageException;

    // ==================== 版本控制管理 ====================

    /**
     * 列出对象版本
     *
     * @param bucketName Bucket名称
     * @param config 列出版本请求配置
     * @return 对象版本列表结果
     * @throws StorageException 存储异常
     */
    com.duda.file.dto.object.ListVersionsResultDTO listVersions(
        String bucketName,
        com.duda.file.dto.object.ListVersionsReqDTO config
    ) throws StorageException;
}
