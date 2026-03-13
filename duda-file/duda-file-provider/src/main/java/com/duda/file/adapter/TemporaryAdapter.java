package com.duda.file.adapter;

import com.duda.file.dto.bucket.BucketDTO;
import com.duda.file.dto.download.DownloadResultDTO;
import com.duda.file.dto.object.ObjectDTO;
import com.duda.file.dto.object.ObjectMetadataDTO;
import com.duda.file.dto.upload.UploadResultDTO;
import com.duda.file.enums.StorageType;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 临时适配器占位符
 * TODO: 实现真实的云存储适配器
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
public class TemporaryAdapter implements StorageService {

    @Override
    public StorageType getStorageType() {
        return StorageType.ALIYUN_OSS;
    }

    // ==================== Bucket操作 ====================

    @Override
    public BucketDTO createBucket(String bucketName, String region, Map<String, Object> config) {
        log.error("临时适配器: createBucket方法未实现");
        throw new RuntimeException("临时适配器：创建Bucket功能未实现，请先实现阿里云OSS适配器");
    }

    @Override
    public void deleteBucket(String bucketName) {
        log.warn("临时适配器: deleteBucket方法未实现");
    }

    @Override
    public boolean doesBucketExist(String bucketName) {
        log.warn("临时适配器: doesBucketExist方法未实现，返回false");
        return false;
    }

    @Override
    public BucketDTO getBucketInfo(String bucketName) {
        log.warn("临时适配器: getBucketInfo方法未实现，返回空BucketDTO");
        return BucketDTO.builder().build();
    }

    @Override
    public List<BucketDTO> listBuckets() {
        log.warn("临时适配器: listBuckets方法未实现，返回空列表");
        return List.of();
    }

    @Override
    public void setBucketAcl(String bucketName, String acl) {
        log.warn("临时适配器: setBucketAcl方法未实现");
    }

    @Override
    public String getBucketAcl(String bucketName) {
        log.warn("临时适配器: getBucketAcl方法未实现，返回空字符串");
        return "";
    }

    @Override
    public String getBucketLocation(String bucketName) {
        log.warn("临时适配器: getBucketLocation方法未实现，返回默认位置");
        return "oss-cn-hangzhou.aliyuncs.com";
    }

    // ==================== Object操作 ====================

    @Override
    public UploadResultDTO uploadObject(String bucketName, String objectKey,
                                         InputStream inputStream, ObjectMetadataDTO metadata) {
        log.error("临时适配器: uploadObject方法未实现");
        throw new RuntimeException("临时适配器：上传功能未实现，请先实现阿里云OSS适配器");
    }

    @Override
    public DownloadResultDTO downloadObject(String bucketName, String objectKey) {
        log.error("临时适配器: downloadObject方法未实现");
        throw new RuntimeException("临时适配器：下载功能未实现，请先实现阿里云OSS适配器");
    }

    @Override
    public void deleteObject(String bucketName, String objectKey) {
        log.warn("临时适配器: deleteObject方法未实现");
    }

    @Override
    public int deleteObjects(String bucketName, List<String> objectKeys) {
        log.warn("临时适配器: deleteObjects方法未实现，返回0");
        return 0;
    }

    @Override
    public boolean doesObjectExist(String bucketName, String objectKey) {
        log.warn("临时适配器: doesObjectExist方法未实现，返回false");
        return false;
    }

    @Override
    public ObjectDTO getObjectInfo(String bucketName, String objectKey) {
        log.warn("临时适配器: getObjectInfo方法未实现，返回空ObjectDTO");
        return ObjectDTO.builder().build();
    }

    @Override
    public ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey) {
        log.warn("临时适配器: getObjectMetadata方法未实现，返回空ObjectMetadataDTO");
        return ObjectMetadataDTO.builder().build();
    }

    @Override
    public void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata) {
        log.warn("临时适配器: setObjectMetadata方法未实现");
    }

    @Override
    public void copyObject(String sourceBucketName, String sourceObjectKey,
                          String destinationBucketName, String destinationObjectKey) {
        log.warn("临时适配器: copyObject方法未实现");
    }

    @Override
    public List<ObjectDTO> listObjects(String bucketName, String prefix,
                                       Integer maxKeys, String marker, String delimiter) {
        log.warn("临时适配器: listObjects方法未实现，返回空列表");
        return List.of();
    }

    @Override
    public void setObjectAcl(String bucketName, String objectKey, String acl) {
        log.warn("临时适配器: setObjectAcl方法未实现");
    }

    @Override
    public String getObjectAcl(String bucketName, String objectKey) {
        log.warn("临时适配器: getObjectAcl方法未实现，返回空字符串");
        return "";
    }

    // ==================== 签名URL操作 ====================

    @Override
    public String generatePresignedUrl(String bucketName, String objectKey,
                                       int expiration, String method) {
        log.warn("临时适配器: generatePresignedUrl方法未实现，返回空URL");
        return "";
    }

    @Override
    public Map<String, String> generateUploadFormData(String bucketName, String objectKey,
                                                        int expiration) {
        log.warn("临时适配器: generateUploadFormData方法未实现，返回空Map");
        return Map.of();
    }

    // ==================== 分片上传操作 ====================

    @Override
    public String initiateMultipartUpload(String bucketName, String objectKey, ObjectMetadataDTO metadata) {
        log.error("临时适配器: initiateMultipartUpload方法未实现");
        throw new RuntimeException("临时适配器：分片上传功能未实现");
    }

    @Override
    public String uploadPart(String bucketName, String objectKey, String uploadId,
                           int partNumber, InputStream inputStream, long partSize) {
        log.error("临时适配器: uploadPart方法未实现");
        throw new RuntimeException("临时适配器：分片上传功能未实现");
    }

    @Override
    public UploadResultDTO completeMultipartUpload(String bucketName, String objectKey,
                                                   String uploadId, Map<Integer, String> partETags) {
        log.error("临时适配器: completeMultipartUpload方法未实现");
        throw new RuntimeException("临时适配器：分片上传完成功能未实现");
    }

    @Override
    public void abortMultipartUpload(String bucketName, String objectKey, String uploadId) {
        log.warn("临时适配器: abortMultipartUpload方法未实现");
    }

    @Override
    public List<Map<String, Object>> listParts(String bucketName, String objectKey, String uploadId) {
        log.warn("临时适配器: listParts方法未实现，返回空列表");
        return List.of();
    }

    @Override
    public List<Map<String, Object>> listMultipartUploads(String bucketName, String prefix, Integer maxUploads) {
        log.warn("临时适配器: listMultipartUploads方法未实现，返回空列表");
        return List.of();
    }

    // ==================== 追加上传 ====================

    @Override
    public Long appendObject(String bucketName, String objectKey, InputStream inputStream,
                             Long position, ObjectMetadataDTO metadata) {
        log.error("临时适配器: appendObject方法未实现");
        throw new RuntimeException("临时适配器：追加上传功能未实现");
    }

    // ==================== 其他操作 ====================

    @Override
    public void restoreObject(String bucketName, String objectKey, int days) {
        log.warn("临时适配器: restoreObject方法未实现");
    }

    @Override
    public void createSymlink(String bucketName, String symlinkKey, String targetKey) {
        log.warn("临时适配器: createSymlink方法未实现");
    }

    @Override
    public String getSymlink(String bucketName, String symlinkKey) {
        log.warn("临时适配器: getSymlink方法未实现，返回空字符串");
        return "";
    }
}
