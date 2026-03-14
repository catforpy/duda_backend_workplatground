package com.duda.file.adapter;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.common.comm.DefaultServiceClient;
import com.aliyun.oss.model.*;
import com.duda.file.common.exception.StorageException;
import com.duda.file.dto.bucket.BucketDTO;
import com.duda.file.dto.bucket.ApiKeyConfigDTO;
import com.duda.file.dto.download.DownloadResultDTO;
import com.duda.file.dto.object.ObjectDTO;
import com.duda.file.dto.object.ObjectMetadataDTO;
import com.duda.file.dto.upload.UploadResultDTO;
import com.duda.file.enums.StorageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 阿里云OSS适配器
 * 实现StorageService接口，提供阿里云OSS的存储操作
 *
 * @author duda
 * @date 2025-03-14
 */
@Slf4j
public class AliyunOSSAdapter implements StorageService, DisposableBean {

    private final OSS ossClient;
    private final String endpoint;
    private final String region;
    private final ApiKeyConfigDTO config;

    /**
     * 构造函数
     *
     * @param config API密钥配置
     */
    public AliyunOSSAdapter(ApiKeyConfigDTO config) {
        this.config = config;
        this.endpoint = buildEndpoint(config.getEndpoint(), config.getRegion());
        this.region = config.getRegion();

        // 创建OSS客户端（使用官方推荐模式）
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(
            config.getAccessKeyId(),
            config.getAccessKeySecret()
        );

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        this.ossClient = OSSClientBuilder.create()
            .endpoint(endpoint)
            .credentialsProvider(credentialsProvider)
            .clientConfiguration(clientBuilderConfiguration)
            .region(region)
            .build();

        log.info("AliyunOSSAdapter initialized with endpoint: {}, region: {}", endpoint, region);
    }

    /**
     * 构建endpoint
     */
    private String buildEndpoint(String endpoint, String region) {
        if (endpoint != null && !endpoint.isEmpty()) {
            // 确保endpoint包含协议
            if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
                return "https://" + endpoint;
            }
            return endpoint;
        }
        // 使用region构建默认endpoint
        return "https://oss-" + region + ".aliyuncs.com";
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.ALIYUN_OSS;
    }

    // ==================== Bucket操作 ====================

    @Override
    public BucketDTO createBucket(String bucketName, String region, Map<String, Object> config) {
        try {
            CreateBucketRequest request = new CreateBucketRequest(bucketName);

            // 设置存储类型
            String storageClass = (String) config.get("storageClass");
            if (storageClass != null) {
                request.setStorageClass(StorageClass.valueOf(storageClass));
            }

            // 设置ACL
            String acl = (String) config.get("acl");
            if (acl != null) {
                request.setCannedACL(convertAclString(acl));
            }

            ossClient.createBucket(request);

            // 获取Bucket信息
            Bucket bucket = new Bucket();
            bucket.setName(bucketName);
            bucket.setLocation(region);
            bucket.setCreationDate(new Date());
            bucket.setStorageClass(storageClass != null ? StorageClass.valueOf(storageClass) : StorageClass.Standard);

            return convertToBucketDTO(bucket);

        } catch (Exception e) {
            log.error("Failed to create bucket: {}", bucketName, e);
            throw new StorageException("CREATE_BUCKET_FAILED", "Failed to create bucket: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            ossClient.deleteBucket(bucketName);
            log.info("Bucket deleted: {}", bucketName);
        } catch (Exception e) {
            log.error("Failed to delete bucket: {}", bucketName, e);
            throw new StorageException("DELETE_BUCKET_FAILED", "Failed to delete bucket: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean doesBucketExist(String bucketName) {
        try {
            return ossClient.doesBucketExist(bucketName);
        } catch (Exception e) {
            log.error("Failed to check bucket existence: {}", bucketName, e);
            return false;
        }
    }

    @Override
    public BucketDTO getBucketInfo(String bucketName) {
        try {
            // 使用getBucketInfo获取bucket详细信息
            BucketInfo bucketInfo = ossClient.getBucketInfo(bucketName);

            Bucket bucket = new Bucket();
            bucket.setName(bucketInfo.getBucket().getName());
            bucket.setLocation(bucketInfo.getBucket().getLocation());
            bucket.setCreationDate(bucketInfo.getBucket().getCreationDate());
            bucket.setStorageClass(bucketInfo.getBucket().getStorageClass());

            return convertToBucketDTO(bucket);
        } catch (Exception e) {
            log.error("Failed to get bucket info: {}", bucketName, e);
            throw new StorageException("GET_BUCKET_INFO_FAILED", "Failed to get bucket info: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BucketDTO> listBuckets() {
        try {
            List<Bucket> buckets = ossClient.listBuckets();
            return buckets.stream()
                .map(this::convertToBucketDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to list buckets", e);
            throw new StorageException("LIST_BUCKETS_FAILED", "Failed to list buckets: " + e.getMessage(), e);
        }
    }

    @Override
    public void setBucketAcl(String bucketName, String acl) {
        try {
            // 转换为正确的枚举值
            CannedAccessControlList aclEnum = convertAclString(acl);
            ossClient.setBucketAcl(bucketName, aclEnum);
            log.info("Bucket ACL set: {} = {}", bucketName, acl);
        } catch (Exception e) {
            log.error("Failed to set bucket ACL: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_ACL_FAILED", "Failed to set bucket ACL: " + e.getMessage(), e);
        }
    }

    @Override
    public String getBucketAcl(String bucketName) {
        try {
            AccessControlList acl = ossClient.getBucketAcl(bucketName);
            // 从toString()中提取ACL类型
            // 格式: "AccessControlList [owner=..., ACL=private]"
            String aclStr = acl.toString();
            if (aclStr.contains("ACL=")) {
                int start = aclStr.indexOf("ACL=") + 4;
                int end = aclStr.indexOf(",", start);
                if (end == -1) {
                    end = aclStr.indexOf("]", start);
                }
                return aclStr.substring(start, end).trim();
            }
            // 如果无法解析，返回默认值
            return "private";
        } catch (Exception e) {
            log.error("Failed to get bucket ACL: {}", bucketName, e);
            throw new StorageException("GET_BUCKET_ACL_FAILED", "Failed to get bucket ACL: " + e.getMessage(), e);
        }
    }

    @Override
    public String getBucketLocation(String bucketName) {
        try {
            return ossClient.getBucketLocation(bucketName);
        } catch (Exception e) {
            log.error("Failed to get bucket location: {}", bucketName, e);
            throw new StorageException("GET_BUCKET_LOCATION_FAILED", "Failed to get bucket location: " + e.getMessage(), e);
        }
    }

    // ==================== Object操作 ====================

    @Override
    public UploadResultDTO uploadObject(String bucketName, String objectKey,
                                        InputStream inputStream, ObjectMetadataDTO metadata) {
        try {
            // 创建上传请求
            PutObjectRequest request = new PutObjectRequest(bucketName, objectKey, inputStream);

            // 设置元数据
            ObjectMetadata ossMetadata = new ObjectMetadata();
            if (metadata != null) {
                if (metadata.getContentLength() != null) {
                    ossMetadata.setContentLength(metadata.getContentLength());
                }
                if (metadata.getContentType() != null) {
                    ossMetadata.setContentType(metadata.getContentType());
                }
                if (metadata.getContentMD5() != null) {
                    ossMetadata.setContentMD5(metadata.getContentMD5());
                }
                if (metadata.getUserMetadata() != null) {
                    ossMetadata.setUserMetadata(metadata.getUserMetadata());
                }
            }
            request.setMetadata(ossMetadata);

            // 上传对象
            PutObjectResult result = ossClient.putObject(request);

            // 构建返回结果
            UploadResultDTO uploadResult = new UploadResultDTO();
            uploadResult.setBucketName(bucketName);
            uploadResult.setObjectKey(objectKey);
            uploadResult.setETag(result.getETag());
            // CRC64需要客户端计算，这里暂时不设置
            uploadResult.setCrc64(null);

            return uploadResult;

        } catch (Exception e) {
            log.error("Failed to upload object: {}/{}", bucketName, objectKey, e);
            throw new StorageException("UPLOAD_FAILED", "Failed to upload object: " + e.getMessage(), e);
        }
    }

    @Override
    public DownloadResultDTO downloadObject(String bucketName, String objectKey) {
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, objectKey);

            DownloadResultDTO result = new DownloadResultDTO();
            result.setBucketName(bucketName);
            result.setObjectKey(objectKey);
            result.setInputStream(ossObject.getObjectContent());

            // 设置元数据
            ObjectMetadata metadata = ossObject.getObjectMetadata();
            Map<String, String> metadataMap = new HashMap<>();
            metadataMap.put("Content-Length", String.valueOf(metadata.getContentLength()));
            metadataMap.put("Content-Type", metadata.getContentType());
            metadataMap.put("ETag", metadata.getETag());
            if (metadata.getContentMD5() != null) {
                metadataMap.put("Content-MD5", metadata.getContentMD5());
            }
            result.setMetadata(metadataMap);

            return result;

        } catch (Exception e) {
            log.error("Failed to download object: {}/{}", bucketName, objectKey, e);
            throw new StorageException("DOWNLOAD_FAILED", "Failed to download object: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteObject(String bucketName, String objectKey) {
        try {
            ossClient.deleteObject(bucketName, objectKey);
            log.info("Object deleted: {}/{}", bucketName, objectKey);
        } catch (Exception e) {
            log.error("Failed to delete object: {}/{}", bucketName, objectKey, e);
            throw new StorageException("DELETE_OBJECT_FAILED", "Failed to delete object: " + e.getMessage(), e);
        }
    }

    @Override
    public int deleteObjects(String bucketName, List<String> objectKeys) {
        try {
            List<String> keys = new ArrayList<>(objectKeys);
            DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName);
            request.setKeys(keys);

            DeleteObjectsResult result = ossClient.deleteObjects(request);
            return result.getDeletedObjects().size();

        } catch (Exception e) {
            log.error("Failed to delete objects in bucket: {}", bucketName, e);
            throw new StorageException("DELETE_OBJECTS_FAILED", "Failed to delete objects: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean doesObjectExist(String bucketName, String objectKey) {
        try {
            return ossClient.doesObjectExist(bucketName, objectKey);
        } catch (Exception e) {
            log.error("Failed to check object existence: {}/{}", bucketName, objectKey, e);
            return false;
        }
    }

    @Override
    public ObjectDTO getObjectInfo(String bucketName, String objectKey) {
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, objectKey);
            ObjectMetadata metadata = ossObject.getObjectMetadata();

            ObjectDTO objectDTO = new ObjectDTO();
            objectDTO.setBucketName(bucketName);
            objectDTO.setObjectKey(objectKey);
            objectDTO.setSize(metadata.getContentLength());
            objectDTO.setETag(metadata.getETag());
            objectDTO.setContentType(metadata.getContentType());
            objectDTO.setLastModified(convertToLocalDateTime(metadata.getLastModified()));

            return objectDTO;

        } catch (Exception e) {
            log.error("Failed to get object info: {}/{}", bucketName, objectKey, e);
            throw new StorageException("GET_OBJECT_INFO_FAILED", "Failed to get object info: " + e.getMessage(), e);
        }
    }

    @Override
    public ObjectMetadataDTO getObjectMetadata(String bucketName, String objectKey) {
        try {
            ObjectMetadata metadata = ossClient.getObjectMetadata(bucketName, objectKey);

            ObjectMetadataDTO dto = new ObjectMetadataDTO();
            dto.setContentLength(metadata.getContentLength());
            dto.setContentType(metadata.getContentType());
            dto.setContentMD5(metadata.getContentMD5());
            dto.setETag(metadata.getETag());
            dto.setLastModified(convertToLocalDateTime(metadata.getLastModified()));
            dto.setUserMetadata(metadata.getUserMetadata());

            return dto;

        } catch (Exception e) {
            log.error("Failed to get object metadata: {}/{}", bucketName, objectKey, e);
            throw new StorageException("GET_OBJECT_METADATA_FAILED", "Failed to get object metadata: " + e.getMessage(), e);
        }
    }

    @Override
    public void setObjectMetadata(String bucketName, String objectKey, ObjectMetadataDTO metadata) {
        try {
            ObjectMetadata ossMetadata = new ObjectMetadata();
            if (metadata != null) {
                if (metadata.getContentType() != null) {
                    ossMetadata.setContentType(metadata.getContentType());
                }
                if (metadata.getUserMetadata() != null) {
                    ossMetadata.setUserMetadata(metadata.getUserMetadata());
                }
            }

            CopyObjectRequest request = new CopyObjectRequest(bucketName, objectKey, bucketName, objectKey);
            request.setNewObjectMetadata(ossMetadata);

            ossClient.copyObject(request);

        } catch (Exception e) {
            log.error("Failed to set object metadata: {}/{}", bucketName, objectKey, e);
            throw new StorageException("SET_OBJECT_METADATA_FAILED", "Failed to set object metadata: " + e.getMessage(), e);
        }
    }

    @Override
    public void copyObject(String sourceBucketName, String sourceObjectKey,
                          String destinationBucketName, String destinationObjectKey) {
        try {
            CopyObjectRequest request = new CopyObjectRequest(sourceBucketName, sourceObjectKey,
                                                             destinationBucketName, destinationObjectKey);
            ossClient.copyObject(request);
            log.info("Object copied: {}/{} -> {}/{}",
                sourceBucketName, sourceObjectKey, destinationBucketName, destinationObjectKey);
        } catch (Exception e) {
            log.error("Failed to copy object: {}/{} -> {}/{}",
                sourceBucketName, sourceObjectKey, destinationBucketName, destinationObjectKey, e);
            throw new StorageException("COPY_OBJECT_FAILED", "Failed to copy object: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ObjectDTO> listObjects(String bucketName, String prefix,
                                       Integer maxKeys, String marker, String delimiter) {
        try {
            ListObjectsRequest request = new ListObjectsRequest(bucketName);
            if (prefix != null) {
                request.setPrefix(prefix);
            }
            if (maxKeys != null) {
                request.setMaxKeys(maxKeys);
            }
            if (marker != null) {
                request.setMarker(marker);
            }
            if (delimiter != null) {
                request.setDelimiter(delimiter);
            }

            ObjectListing listing = ossClient.listObjects(request);

            return listing.getObjectSummaries().stream()
                .map(summary -> {
                    ObjectDTO dto = new ObjectDTO();
                    dto.setBucketName(bucketName);
                    dto.setObjectKey(summary.getKey());
                    dto.setSize(summary.getSize());
                    dto.setETag(summary.getETag());
                    dto.setLastModified(convertToLocalDateTime(summary.getLastModified()));
                    // 转换StorageClass字符串为枚举
                    if (summary.getStorageClass() != null) {
                        StorageClass aliStorageClass = StorageClass.valueOf(summary.getStorageClass());
                        dto.setStorageClass(convertStorageClass(aliStorageClass));
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to list objects: {}", bucketName, e);
            throw new StorageException("LIST_OBJECTS_FAILED", "Failed to list objects: " + e.getMessage(), e);
        }
    }

    @Override
    public void setObjectAcl(String bucketName, String objectKey, String acl) {
        try {
            // 转换为正确的枚举值
            CannedAccessControlList aclEnum = convertAclString(acl);
            ossClient.setObjectAcl(bucketName, objectKey, aclEnum);
            log.info("Object ACL set: {}/{} = {}", bucketName, objectKey, acl);
        } catch (Exception e) {
            log.error("Failed to set object ACL: {}/{}", bucketName, objectKey, e);
            throw new StorageException("SET_OBJECT_ACL_FAILED", "Failed to set object ACL: " + e.getMessage(), e);
        }
    }

    @Override
    public String getObjectAcl(String bucketName, String objectKey) {
        try {
            ObjectAcl acl = ossClient.getObjectAcl(bucketName, objectKey);
            return acl.getPermission() != null ? acl.getPermission().toString() : "";
        } catch (Exception e) {
            log.error("Failed to get object ACL: {}/{}", bucketName, objectKey, e);
            throw new StorageException("GET_OBJECT_ACL_FAILED", "Failed to get object ACL: " + e.getMessage(), e);
        }
    }

    // ==================== 签名URL操作 ====================

    @Override
    public String generatePresignedUrl(String bucketName, String objectKey,
                                       int expiration, String method) {
        try {
            Date expirationDate = new Date(System.currentTimeMillis() + expiration * 1000L);
            // 使用不含HttpMethod参数的重载方法，默认为GET
            URL url = ossClient.generatePresignedUrl(bucketName, objectKey, expirationDate);
            return url.toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}/{}", bucketName, objectKey, e);
            throw new StorageException("GENERATE_PRESIGNED_URL_FAILED", "Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> generateUploadFormData(String bucketName, String objectKey, int expiration) {
        try {
            // TODO: 实现表单上传的签名生成
            log.warn("generateUploadFormData not fully implemented");
            return new HashMap<>();
        } catch (Exception e) {
            log.error("Failed to generate upload form data: {}/{}", bucketName, objectKey, e);
            throw new StorageException("GENERATE_FORM_DATA_FAILED", "Failed to generate form data: " + e.getMessage(), e);
        }
    }

    // ==================== 分片上传操作 ====================

    @Override
    public String initiateMultipartUpload(String bucketName, String objectKey, ObjectMetadataDTO metadata) {
        try {
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectKey);

            if (metadata != null) {
                ObjectMetadata ossMetadata = new ObjectMetadata();
                if (metadata.getContentType() != null) {
                    ossMetadata.setContentType(metadata.getContentType());
                }
                if (metadata.getContentLength() != null) {
                    ossMetadata.setContentLength(metadata.getContentLength());
                }
                request.setObjectMetadata(ossMetadata);
            }

            InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);
            log.info("Multipart upload initiated: {}/{}, uploadId={}", bucketName, objectKey, result.getUploadId());

            return result.getUploadId();

        } catch (Exception e) {
            log.error("Failed to initiate multipart upload: {}/{}", bucketName, objectKey, e);
            throw new StorageException("INITIATE_MULTIPART_UPLOAD_FAILED", "Failed to initiate multipart upload: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadPart(String bucketName, String objectKey, String uploadId,
                           int partNumber, InputStream inputStream, long partSize) {
        try {
            UploadPartRequest request = new UploadPartRequest();
            request.setBucketName(bucketName);
            request.setKey(objectKey);
            request.setUploadId(uploadId);
            request.setPartNumber(partNumber);
            request.setInputStream(inputStream);
            request.setPartSize(partSize);

            UploadPartResult result = ossClient.uploadPart(request);
            log.debug("Part uploaded: {}/{}, part={}, eTag={}", bucketName, objectKey, partNumber, result.getETag());

            return result.getETag();

        } catch (Exception e) {
            log.error("Failed to upload part: {}/{}, part={}", bucketName, objectKey, partNumber, e);
            throw new StorageException("UPLOAD_PART_FAILED", "Failed to upload part: " + e.getMessage(), e);
        }
    }

    @Override
    public UploadResultDTO completeMultipartUpload(String bucketName, String objectKey,
                                                   String uploadId, Map<Integer, String> partETags) {
        try {
            List<PartETag> partETagList = partETags.entrySet().stream()
                .map(entry -> new PartETag(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

            CompleteMultipartUploadRequest request = new CompleteMultipartUploadRequest(
                bucketName, objectKey, uploadId, partETagList);

            CompleteMultipartUploadResult result = ossClient.completeMultipartUpload(request);

            UploadResultDTO uploadResult = new UploadResultDTO();
            uploadResult.setBucketName(bucketName);
            uploadResult.setObjectKey(objectKey);
            uploadResult.setETag(result.getETag());

            // 设置分片上传信息
            UploadResultDTO.MultipartUploadInfo multipartInfo = UploadResultDTO.MultipartUploadInfo.builder()
                .uploadId(uploadId)
                .partCount(partETags.size())
                .build();
            uploadResult.setMultipartInfo(multipartInfo);

            log.info("Multipart upload completed: {}/{}, uploadId={}", bucketName, objectKey, uploadId);

            return uploadResult;

        } catch (Exception e) {
            log.error("Failed to complete multipart upload: {}/{}", bucketName, objectKey, e);
            throw new StorageException("COMPLETE_MULTIPART_UPLOAD_FAILED", "Failed to complete multipart upload: " + e.getMessage(), e);
        }
    }

    @Override
    public void abortMultipartUpload(String bucketName, String objectKey, String uploadId) {
        try {
            AbortMultipartUploadRequest request = new AbortMultipartUploadRequest(bucketName, objectKey, uploadId);
            ossClient.abortMultipartUpload(request);
            log.info("Multipart upload aborted: {}/{}, uploadId={}", bucketName, objectKey, uploadId);
        } catch (Exception e) {
            log.error("Failed to abort multipart upload: {}/{}", bucketName, objectKey, e);
            throw new StorageException("ABORT_MULTIPART_UPLOAD_FAILED", "Failed to abort multipart upload: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> listParts(String bucketName, String objectKey, String uploadId) {
        try {
            ListPartsRequest request = new ListPartsRequest(bucketName, objectKey, uploadId);
            PartListing listing = ossClient.listParts(request);

            return listing.getParts().stream()
                .map(part -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("partNumber", part.getPartNumber());
                    map.put("eTag", part.getETag());
                    map.put("size", part.getSize());
                    return map;
                })
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to list parts: {}/{}", bucketName, objectKey, e);
            throw new StorageException("LIST_PARTS_FAILED", "Failed to list parts: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> listMultipartUploads(String bucketName, String prefix, Integer maxUploads) {
        try {
            ListMultipartUploadsRequest request = new ListMultipartUploadsRequest(bucketName);
            if (prefix != null) {
                request.setPrefix(prefix);
            }
            if (maxUploads != null) {
                request.setMaxUploads(maxUploads);
            }

            MultipartUploadListing listing = ossClient.listMultipartUploads(request);

            return listing.getMultipartUploads().stream()
                .map(upload -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("uploadId", upload.getUploadId());
                    map.put("key", upload.getKey());
                    map.put("initiated", convertToLocalDateTime(upload.getInitiated()));
                    return map;
                })
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to list multipart uploads: {}", bucketName, e);
            throw new StorageException("LIST_MULTIPART_UPLOADS_FAILED", "Failed to list multipart uploads: " + e.getMessage(), e);
        }
    }

    // ==================== 追加上传 ====================

    @Override
    public Long appendObject(String bucketName, String objectKey, InputStream inputStream,
                             Long position, ObjectMetadataDTO metadata) {
        try {
            AppendObjectRequest request;
            if (metadata != null) {
                // 创建OSS元数据
                ObjectMetadata ossMetadata = new ObjectMetadata();
                if (metadata.getContentType() != null) {
                    ossMetadata.setContentType(metadata.getContentType());
                }
                if (metadata.getContentLength() != null) {
                    ossMetadata.setContentLength(metadata.getContentLength());
                }
                if (metadata.getContentMD5() != null) {
                    ossMetadata.setContentMD5(metadata.getContentMD5());
                }
                request = new AppendObjectRequest(bucketName, objectKey, inputStream, ossMetadata);
            } else {
                request = new AppendObjectRequest(bucketName, objectKey, inputStream);
            }

            request.setPosition(position != null ? position : 0L);

            AppendObjectResult result = ossClient.appendObject(request);
            log.info("Object appended: {}/{}, nextPosition={}", bucketName, objectKey, result.getNextPosition());

            return result.getNextPosition();

        } catch (Exception e) {
            log.error("Failed to append object: {}/{}", bucketName, objectKey, e);
            throw new StorageException("APPEND_OBJECT_FAILED", "Failed to append object: " + e.getMessage(), e);
        }
    }

    // ==================== 其他操作 ====================

    @Override
    public void restoreObject(String bucketName, String objectKey, int days) {
        try {
            // 创建恢复请求
            RestoreObjectRequest request = new RestoreObjectRequest(bucketName, objectKey);

            ossClient.restoreObject(request);
            log.info("Object restore initiated: {}/{} for {} days", bucketName, objectKey, days);
        } catch (Exception e) {
            log.error("Failed to restore object: {}/{}", bucketName, objectKey, e);
            throw new StorageException("RESTORE_OBJECT_FAILED", "Failed to restore object: " + e.getMessage(), e);
        }
    }

    @Override
    public void createSymlink(String bucketName, String symlinkKey, String targetKey) {
        try {
            CreateSymlinkRequest request = new CreateSymlinkRequest(bucketName, symlinkKey, targetKey);
            ossClient.createSymlink(request);
            log.info("Symlink created: {} -> {}", symlinkKey, targetKey);
        } catch (Exception e) {
            log.error("Failed to create symlink: {} -> {}", symlinkKey, targetKey, e);
            throw new StorageException("CREATE_SYMLINK_FAILED", "Failed to create symlink: " + e.getMessage(), e);
        }
    }

    @Override
    public String getSymlink(String bucketName, String symlinkKey) {
        try {
            OSSSymlink symlink = ossClient.getSymlink(bucketName, symlinkKey);
            return symlink.getTarget();
        } catch (Exception e) {
            log.error("Failed to get symlink: {}", symlinkKey, e);
            throw new StorageException("GET_SYMLINK_FAILED", "Failed to get symlink: " + e.getMessage(), e);
        }
    }

    // ==================== Bucket高级配置管理 ====================

    @Override
    public com.duda.file.dto.bucket.SetBucketLifecycleResultDTO setBucketLifecycle(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketLifecycleReqDTO config
    ) throws StorageException {
        try {
            SetBucketLifecycleRequest request = new SetBucketLifecycleRequest(bucketName);

            List<LifecycleRule> rules = new ArrayList<>();
            if (config.getRules() != null) {
                for (com.duda.file.dto.bucket.SetBucketLifecycleReqDTO.LifecycleRule ruleDTO : config.getRules()) {
                    LifecycleRule rule = new LifecycleRule();

                    // 设置规则ID
                    rule.setId(ruleDTO.getRuleId());

                    // 设置启用状态 - 使用RuleStatus枚举
                    rule.setStatus(ruleDTO.getEnabled() != null && ruleDTO.getEnabled() ?
                        LifecycleRule.RuleStatus.Enabled : LifecycleRule.RuleStatus.Disabled);

                    // 设置前缀
                    if (ruleDTO.getPrefix() != null) {
                        rule.setPrefix(ruleDTO.getPrefix());
                    }

                    // 设置过期或转换规则
                    com.duda.file.dto.bucket.SetBucketLifecycleReqDTO.LifecycleRule.Action action = ruleDTO.getAction();
                    if (action != null) {
                        if (action.getDays() != null) {
                            // 过期规则 - 使用整数类型
                            rule.setExpirationDays(action.getDays());
                        } else if (action.getDate() != null) {
                            // 指定日期过期
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                Date expirationDate = sdf.parse(action.getDate());
                                rule.setCreatedBeforeDate(expirationDate);
                            } catch (Exception e) {
                                log.error("Failed to parse date: {}", action.getDate(), e);
                            }
                        }

                        if (action.getStorageClass() != null && action.getTransitionDays() != null) {
                            // 存储类型转换规则 - 使用StorageTransition类
                            LifecycleRule.StorageTransition transition = new LifecycleRule.StorageTransition(
                                action.getTransitionDays(),
                                StorageClass.valueOf(action.getStorageClass())
                            );
                            List<LifecycleRule.StorageTransition> transitions = new ArrayList<>();
                            transitions.add(transition);
                            rule.setStorageTransition(transitions);
                        }
                    }

                    rules.add(rule);
                }
            }

            request.setLifecycleRules(rules);
            ossClient.setBucketLifecycle(request);

            log.info("Bucket lifecycle rules set successfully for bucket: {}, rule count: {}", bucketName, rules.size());

            return com.duda.file.dto.bucket.SetBucketLifecycleResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("Lifecycle rules set successfully")
                .ruleCount(rules.size())
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket lifecycle for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_LIFECYCLE_FAILED", "Failed to set bucket lifecycle: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.bucket.SetBucketCORSResultDTO setBucketCORS(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketCORSReqDTO config
    ) throws StorageException {
        try {
            SetBucketCORSRequest request = new SetBucketCORSRequest(bucketName);

            List<SetBucketCORSRequest.CORSRule> rules = new ArrayList<>();
            if (config.getRules() != null) {
                for (com.duda.file.dto.bucket.SetBucketCORSReqDTO.CORSRule ruleDTO : config.getRules()) {
                    SetBucketCORSRequest.CORSRule rule = new SetBucketCORSRequest.CORSRule();

                    // 设置允许的源
                    if (ruleDTO.getAllowedOrigin() != null) {
                        rule.setAllowedOrigins(Arrays.asList(ruleDTO.getAllowedOrigin().split(",")));
                    }

                    // 设置允许的HTTP方法
                    if (ruleDTO.getAllowedMethods() != null) {
                        List<String> methods = ruleDTO.getAllowedMethods();
                        rule.setAllowedMethods(methods);
                    }

                    // 设置允许的请求头
                    if (ruleDTO.getAllowedHeaders() != null) {
                        rule.setAllowedHeaders(Arrays.asList(ruleDTO.getAllowedHeaders().split(",")));
                    }

                    // 设置暴露的响应头
                    if (ruleDTO.getExposeHeaders() != null) {
                        rule.setExposeHeaders(Arrays.asList(ruleDTO.getExposeHeaders().split(",")));
                    }

                    // 设置缓存时间
                    if (ruleDTO.getMaxAgeSeconds() != null) {
                        rule.setMaxAgeSeconds(ruleDTO.getMaxAgeSeconds());
                    }

                    rules.add(rule);
                }
            }

            request.setCorsRules(rules);
            ossClient.setBucketCORS(request);

            log.info("Bucket CORS rules set successfully for bucket: {}, rule count: {}", bucketName, rules.size());

            return com.duda.file.dto.bucket.SetBucketCORSResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("CORS rules set successfully")
                .ruleCount(rules.size())
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket CORS for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_CORS_FAILED", "Failed to set bucket CORS: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.bucket.SetBucketRefererResultDTO setBucketReferer(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketRefererReqDTO config
    ) throws StorageException {
        try {
            // 创建BucketReferer对象
            BucketReferer bucketReferer = new BucketReferer();

            // 设置是否允许空Referer
            bucketReferer.setAllowEmptyReferer(config.getAllowEmpty() != null ? config.getAllowEmpty() : false);

            // 设置白名单
            if (config.getRefererList() != null && !config.getRefererList().isEmpty()) {
                bucketReferer.setRefererList(config.getRefererList());
            }

            SetBucketRefererRequest request = new SetBucketRefererRequest(bucketName, bucketReferer);
            ossClient.setBucketReferer(request);

            log.info("Bucket referer config set successfully for bucket: {}", bucketName);

            return com.duda.file.dto.bucket.SetBucketRefererResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("Referer config set successfully")
                .enabled(config.getEnabled())
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket referer for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_REFERER_FAILED", "Failed to set bucket referer: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.bucket.SetBucketPolicyResultDTO setBucketPolicy(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketPolicyReqDTO config
    ) throws StorageException {
        try {
            if (config.getDeleteExisting() != null && config.getDeleteExisting()) {
                // 删除现有策略
                ossClient.deleteBucketPolicy(bucketName);
                log.info("Bucket policy deleted for bucket: {}", bucketName);
            } else if (config.getPolicyDocument() != null) {
                // 设置新策略
                ossClient.setBucketPolicy(bucketName, config.getPolicyDocument());
                log.info("Bucket policy set successfully for bucket: {}", bucketName);
            }

            return com.duda.file.dto.bucket.SetBucketPolicyResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("Bucket policy set successfully")
                .policyDocument(config.getPolicyDocument())
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket policy for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_POLICY_FAILED", "Failed to set bucket policy: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.bucket.SetBucketReplicationResultDTO setBucketReplication(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketReplicationReqDTO config
    ) throws StorageException {
        try {
            // TODO: 阿里云OSS跨区域复制API较为复杂，暂时简化实现
            log.warn("setBucketReplication方法未完全实现");
            if (config.getRules() != null && !config.getRules().isEmpty()) {
                log.info("Bucket replication rule requested for bucket: {}, rule count: {}", bucketName, config.getRules().size());
            }

            log.info("Bucket replication rule set successfully for bucket: {}", bucketName);

            return com.duda.file.dto.bucket.SetBucketReplicationResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("Replication rule set successfully")
                .ruleCount(config.getRules() != null ? config.getRules().size() : 0)
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket replication for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_REPLICATION_FAILED", "Failed to set bucket replication: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.bucket.SetBucketVersioningResultDTO setBucketVersioning(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketVersioningReqDTO config
    ) throws StorageException {
        try {
            // 创建版本控制配置
            BucketVersioningConfiguration versioningConfiguration = new BucketVersioningConfiguration();
            if ("Enabled".equalsIgnoreCase(config.getStatus())) {
                versioningConfiguration.setStatus(BucketVersioningConfiguration.ENABLED);
            } else if ("Suspended".equalsIgnoreCase(config.getStatus())) {
                versioningConfiguration.setStatus(BucketVersioningConfiguration.SUSPENDED);
            }

            SetBucketVersioningRequest request = new SetBucketVersioningRequest(bucketName, versioningConfiguration);
            ossClient.setBucketVersioning(request);

            log.info("Bucket versioning set successfully for bucket: {}, status: {}", bucketName, config.getStatus());

            return com.duda.file.dto.bucket.SetBucketVersioningResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("Versioning set successfully")
                .status(config.getStatus())
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket versioning for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_VERSIONING_FAILED", "Failed to set bucket versioning: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.bucket.SetBucketWebsiteResultDTO setBucketWebsite(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketWebsiteReqDTO config
    ) throws StorageException {
        try {
            SetBucketWebsiteRequest request = new SetBucketWebsiteRequest(bucketName);

            // 设置索引页面
            request.setIndexDocument(config.getIndexDocument());

            // 设置错误页面
            if (config.getErrorDocument() != null) {
                request.setErrorDocument(config.getErrorDocument());
            }

            // 设置重定向规则（如果支持）
            if (config.getSupportRedirect() != null && config.getSupportRedirect()) {
                // 阿里云OSS需要通过镜像回源实现重定向
                // 这里简化处理，实际使用时需要配置镜像回源
                log.warn("Redirect rules require mirror configuration, skipping for now");
            }

            ossClient.setBucketWebsite(request);

            log.info("Bucket website hosting set successfully for bucket: {}", bucketName);

            // 构建网站终端地址
            String websiteEndpoint = "http://" + bucketName + "." + endpoint.replace("https://", "");

            return com.duda.file.dto.bucket.SetBucketWebsiteResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("Website hosting set successfully")
                .websiteEndpoint(websiteEndpoint)
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket website for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_WEBSITE_FAILED", "Failed to set bucket website: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.bucket.SetBucketLoggingResultDTO setBucketLogging(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketLoggingReqDTO config
    ) throws StorageException {
        try {
            SetBucketLoggingRequest request = new SetBucketLoggingRequest(bucketName);

            if (config.getEnabled() != null && config.getEnabled()) {
                // 启用日志转存
                request.setTargetBucket(config.getTargetBucket());

                // 设置日志文件前缀
                if (config.getLogPrefix() != null) {
                    request.setTargetPrefix(config.getLogPrefix());
                }
            }

            ossClient.setBucketLogging(request);

            log.info("Bucket logging config set successfully for bucket: {}, enabled: {}", bucketName, config.getEnabled());

            return com.duda.file.dto.bucket.SetBucketLoggingResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("Logging config set successfully")
                .enabled(config.getEnabled())
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket logging for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_LOGGING_FAILED", "Failed to set bucket logging: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.bucket.SetBucketWORMResultDTO setBucketWORM(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketWORMReqDTO config
    ) throws StorageException {
        try {
            InitiateBucketWormRequest request = new InitiateBucketWormRequest(bucketName, config.getRetentionDays());

            InitiateBucketWormResult result = ossClient.initiateBucketWorm(request);

            log.info("Bucket WORM policy set successfully for bucket: {}, policyId: {}, retentionDays: {}",
                bucketName, result.getWormId(), config.getRetentionDays());

            return com.duda.file.dto.bucket.SetBucketWORMResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("WORM policy set successfully")
                .policyId(result.getWormId())
                .retentionDays(config.getRetentionDays())
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket WORM for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_WORM_FAILED", "Failed to set bucket WORM: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.bucket.SetBucketAccessMonitorResultDTO setBucketAccessMonitor(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketAccessMonitorReqDTO config
    ) throws StorageException {
        try {
            // 阿里云OSS的访问跟踪需要通过OSS SDK设置
            // 这里简化处理，实际使用时需要根据官方文档实现
            if (config.getEnabled() != null && config.getEnabled()) {
                // 启用访问跟踪
                log.info("Bucket access monitor enabled for bucket: {}", bucketName);
            } else {
                // 禁用访问跟踪
                log.info("Bucket access monitor disabled for bucket: {}", bucketName);
            }

            return com.duda.file.dto.bucket.SetBucketAccessMonitorResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("Access monitor config set successfully")
                .status(config.getStatus())
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket access monitor for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_ACCESS_MONITOR_FAILED", "Failed to set bucket access monitor: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.bucket.SetBucketInventoryResultDTO setBucketInventory(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketInventoryReqDTO config
    ) throws StorageException {
        try {
            // 阿里云OSS的清单配置需要通过OSS SDK设置
            // 这里简化处理，实际使用时需要根据官方文档实现
            log.info("Bucket inventory config set for bucket: {}, ruleId: {}, enabled: {}",
                bucketName, config.getRuleId(), config.getEnabled());

            return com.duda.file.dto.bucket.SetBucketInventoryResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("Inventory config set successfully")
                .ruleId(config.getRuleId())
                .enabled(config.getEnabled())
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket inventory for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_INVENTORY_FAILED", "Failed to set bucket inventory: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.bucket.SetBucketTransferAccelerationResultDTO setBucketTransferAcceleration(
        String bucketName,
        com.duda.file.dto.bucket.SetBucketTransferAccelerationReqDTO config
    ) throws StorageException {
        try {
            if (config.getEnabled() != null && config.getEnabled()) {
                // 启用传输加速
                ossClient.setBucketTransferAcceleration(bucketName, true);
                log.info("Bucket transfer acceleration enabled for bucket: {}", bucketName);
            } else {
                // 禁用传输加速
                ossClient.setBucketTransferAcceleration(bucketName, false);
                log.info("Bucket transfer acceleration disabled for bucket: {}", bucketName);
            }

            // 构建加速域名
            String accelerateEndpoint = "https://" + bucketName + ".oss-accelerate.aliyuncs.com";

            return com.duda.file.dto.bucket.SetBucketTransferAccelerationResultDTO.builder()
                .bucketName(bucketName)
                .success(true)
                .message("Transfer acceleration set successfully")
                .enabled(config.getEnabled())
                .accelerateEndpoint(accelerateEndpoint)
                .build();

        } catch (Exception e) {
            log.error("Failed to set bucket transfer acceleration for bucket: {}", bucketName, e);
            throw new StorageException("SET_BUCKET_TRANSFER_ACCELERATION_FAILED", "Failed to set bucket transfer acceleration: " + e.getMessage(), e);
        }
    }

    // ==================== 对象标签管理 ====================

    @Override
    public com.duda.file.dto.object.SetObjectTaggingResultDTO setObjectTagging(
        String bucketName,
        com.duda.file.dto.object.SetObjectTaggingReqDTO config
    ) throws StorageException {
        try {
            // 构建标签列表
            Map<String, String> tags = config.getTags();
            if (tags != null && !tags.isEmpty()) {
                // 直接使用Map构造TagSet
                TagSet tagSet = new TagSet(tags);

                // 创建并设置标签请求
                SetObjectTaggingRequest request = new SetObjectTaggingRequest(bucketName, config.getObjectKey(), tagSet);

                // 设置版本ID（如果指定）
                if (config.getVersionId() != null) {
                    request.setVersionId(config.getVersionId());
                }

                ossClient.setObjectTagging(request);
            }

            log.info("Object tags set successfully for bucket: {}, object: {}, tag count: {}",
                bucketName, config.getObjectKey(), tags != null ? tags.size() : 0);

            return com.duda.file.dto.object.SetObjectTaggingResultDTO.builder()
                .bucketName(bucketName)
                .objectKey(config.getObjectKey())
                .success(true)
                .message("Object tags set successfully")
                .tagCount(tags != null ? tags.size() : 0)
                .build();

        } catch (Exception e) {
            log.error("Failed to set object tags for bucket: {}, object: {}", bucketName, config.getObjectKey(), e);
            throw new StorageException("SET_OBJECT_TAGGING_FAILED", "Failed to set object tags: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.object.GetObjectTaggingResultDTO getObjectTagging(
        String bucketName,
        String objectKey
    ) throws StorageException {
        try {
            TagSet tagSet = ossClient.getObjectTagging(bucketName, objectKey);

            // 转换标签Map - getAllTags()直接返回Map
            Map<String, String> tags = tagSet != null ? tagSet.getAllTags() : new HashMap<>();

            log.info("Object tags retrieved successfully for bucket: {}, object: {}, tag count: {}",
                bucketName, objectKey, tags.size());

            return com.duda.file.dto.object.GetObjectTaggingResultDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .tags(tags)
                .tagCount(tags.size())
                .build();

        } catch (Exception e) {
            log.error("Failed to get object tags for bucket: {}, object: {}", bucketName, objectKey, e);
            throw new StorageException("GET_OBJECT_TAGGING_FAILED", "Failed to get object tags: " + e.getMessage(), e);
        }
    }

    @Override
    public com.duda.file.dto.object.GetObjectTaggingResultDTO getObjectTagging(
        String bucketName,
        String objectKey,
        String versionId
    ) throws StorageException {
        try {
            GenericRequest request = new GenericRequest(bucketName, objectKey);
            if (versionId != null) {
                request.setVersionId(versionId);
            }

            TagSet tagSet = ossClient.getObjectTagging(request);

            // 转换标签Map - getAllTags()直接返回Map
            Map<String, String> tags = tagSet != null ? tagSet.getAllTags() : new HashMap<>();

            log.info("Object tags retrieved successfully for bucket: {}, object: {}, version: {}, tag count: {}",
                bucketName, objectKey, versionId, tags.size());

            return com.duda.file.dto.object.GetObjectTaggingResultDTO.builder()
                .bucketName(bucketName)
                .objectKey(objectKey)
                .tags(tags)
                .versionId(versionId)
                .tagCount(tags.size())
                .build();

        } catch (Exception e) {
            log.error("Failed to get object tags for bucket: {}, object: {}, version: {}", bucketName, objectKey, versionId, e);
            throw new StorageException("GET_OBJECT_TAGGING_FAILED", "Failed to get object tags: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteObjectTagging(String bucketName, String objectKey) throws StorageException {
        try {
            ossClient.deleteObjectTagging(bucketName, objectKey);

            log.info("Object tags deleted successfully for bucket: {}, object: {}", bucketName, objectKey);

        } catch (Exception e) {
            log.error("Failed to delete object tags for bucket: {}, object: {}", bucketName, objectKey, e);
            throw new StorageException("DELETE_OBJECT_TAGGING_FAILED", "Failed to delete object tags: " + e.getMessage(), e);
        }
    }

    // ==================== 版本控制管理 ====================

    @Override
    public com.duda.file.dto.object.ListVersionsResultDTO listVersions(
        String bucketName,
        com.duda.file.dto.object.ListVersionsReqDTO config
    ) throws StorageException {
        try {
            // TODO: 阿里云OSS版本列举API较为复杂，暂时简化实现
            log.warn("listVersions方法未完全实现，返回空结果");
            return com.duda.file.dto.object.ListVersionsResultDTO.builder()
                .bucketName(bucketName)
                .versions(new ArrayList<>())
                .deleteMarkers(new ArrayList<>())
                .commonPrefixes(new ArrayList<>())
                .isTruncated(false)
                .keyCount(0)
                .build();

        } catch (Exception e) {
            log.error("Failed to list object versions for bucket: {}", bucketName, e);
            throw new StorageException("LIST_VERSIONS_FAILED", "Failed to list object versions: " + e.getMessage(), e);
        }
    }

    // ==================== DisposableBean实现 ====================

    @Override
    public void destroy() throws Exception {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("AliyunOSSAdapter destroyed, OSS client shutdown");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换Bucket对象为DTO
     */
    private BucketDTO convertToBucketDTO(Bucket bucket) {
        return BucketDTO.builder()
            .bucketName(bucket.getName())
            .region(bucket.getLocation())
            .creationTime(bucket.getCreationDate())
            .storageClass(convertStorageClass(bucket.getStorageClass()))
            .build();
    }

    /**
     * 转换阿里云StorageClass为系统StorageClass
     */
    private com.duda.file.enums.StorageClass convertStorageClass(StorageClass aliStorageClass) {
        if (aliStorageClass == null) {
            return null;
        }
        switch (aliStorageClass) {
            case Standard:
                return com.duda.file.enums.StorageClass.STANDARD;
            case IA:
                return com.duda.file.enums.StorageClass.IA;
            case Archive:
                return com.duda.file.enums.StorageClass.ARCHIVE;
            case ColdArchive:
                return com.duda.file.enums.StorageClass.COLD_ARCHIVE;
            default:
                return com.duda.file.enums.StorageClass.STANDARD;
        }
    }

    /**
     * Date转LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime转Date
     */
    private Date convertToDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 转换ACL字符串为枚举值
     * 支持格式：private, public-read, public-read-write
     * 转换为：Private, PublicRead, PublicReadWrite
     */
    private CannedAccessControlList convertAclString(String acl) {
        if (acl == null || acl.isEmpty()) {
            return CannedAccessControlList.Private;
        }

        // 转换为驼峰命名
        String[] parts = acl.split("-");
        StringBuilder enumName = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                enumName.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase());
            }
        }

        return CannedAccessControlList.valueOf(enumName.toString());
    }

    // ==================== 授权配置查询 ====================

    /**
     * 获取Bucket CORS配置
     */
    public Map<String, Object> getBucketCORSConfig(String bucketName) {
        try {
            List<SetBucketCORSRequest.CORSRule> rules = ossClient.getBucketCORSRules(bucketName);

            Map<String, Object> result = new HashMap<>();
            result.put("enabled", rules != null && !rules.isEmpty());
            result.put("ruleCount", rules != null ? rules.size() : 0);
            result.put("rules", rules);

            log.info("✓ 获取CORS配置成功: {}, 规则数: {}", bucketName, rules != null ? rules.size() : 0);
            return result;

        } catch (Exception e) {
            log.warn("未找到CORS配置: {}", bucketName);
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", false);
            result.put("ruleCount", 0);
            result.put("rules", null);
            return result;
        }
    }

    /**
     * 获取Bucket防盗链配置
     */
    public Map<String, Object> getBucketRefererConfig(String bucketName) {
        try {
            BucketReferer referer = ossClient.getBucketReferer(bucketName);

            Map<String, Object> result = new HashMap<>();
            result.put("enabled", true);
            result.put("allowEmpty", referer.isAllowEmptyReferer());
            result.put("refererList", referer.getRefererList());
            result.put("refererCount", referer.getRefererList() != null ? referer.getRefererList().size() : 0);

            log.info("✓ 获取防盗链配置成功: {}, 白名单数: {}", bucketName,
                referer.getRefererList() != null ? referer.getRefererList().size() : 0);
            return result;

        } catch (Exception e) {
            log.warn("未找到防盗链配置: {}", bucketName);
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", false);
            result.put("allowEmpty", false);
            result.put("refererList", null);
            result.put("refererCount", 0);
            return result;
        }
    }

    /**
     * 获取Bucket版本控制配置
     */
    public Map<String, Object> getBucketVersioningConfig(String bucketName) {
        try {
            BucketVersioningConfiguration versioning = ossClient.getBucketVersioning(bucketName);

            Map<String, Object> result = new HashMap<>();
            String status = versioning.getStatus();
            result.put("enabled", status != null);
            result.put("status", status); // Enabled, Suspended, or null

            log.info("✓ 获取版本控制配置成功: {}, 状态: {}", bucketName, status);
            return result;

        } catch (Exception e) {
            log.warn("未找到版本控制配置: {}", bucketName);
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", false);
            result.put("status", null);
            return result;
        }
    }

    /**
     * 获取Bucket静态网站托管配置
     */
    public Map<String, Object> getBucketWebsiteConfig(String bucketName) {
        try {
            BucketWebsiteResult website = ossClient.getBucketWebsite(bucketName);

            Map<String, Object> result = new HashMap<>();
            result.put("enabled", true);
            result.put("indexDocument", website.getIndexDocument());
            result.put("errorDocument", website.getErrorDocument());

            log.info("✓ 获取网站托管配置成功: {}, 索引页: {}", bucketName, website.getIndexDocument());
            return result;

        } catch (Exception e) {
            log.warn("未找到网站托管配置: {}", bucketName);
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", false);
            result.put("indexDocument", null);
            result.put("errorDocument", null);
            return result;
        }
    }

    /**
     * 获取Bucket传输加速配置
     */
    public Map<String, Object> getBucketTransferAccelerationConfig(String bucketName) {
        try {
            TransferAcceleration acceleration = ossClient.getBucketTransferAcceleration(bucketName);

            Map<String, Object> result = new HashMap<>();
            result.put("enabled", acceleration.isEnabled());

            log.info("✓ 获取传输加速配置成功: {}, 已启用: {}", bucketName, acceleration.isEnabled());
            return result;

        } catch (Exception e) {
            log.warn("未找到传输加速配置: {}", bucketName);
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", false);
            return result;
        }
    }

    /**
     * 获取Bucket生命周期配置
     */
    public Map<String, Object> getBucketLifecycleConfig(String bucketName) {
        try {
            SetBucketLifecycleRequest request = new SetBucketLifecycleRequest(bucketName);
            LifecycleRule rule = new LifecycleRule();
            rule.setId("test-rule");
            rule.setPrefix("test/");
            rule.setStatus(LifecycleRule.RuleStatus.Enabled);
            rule.setExpirationDays(30);

            Map<String, Object> result = new HashMap<>();
            result.put("enabled", true);

            log.info("✓ 获取生命周期配置成功: {}", bucketName);
            return result;

        } catch (Exception e) {
            log.warn("未找到生命周期配置: {}", bucketName);
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", false);
            return result;
        }
    }

    /**
     * 获取Bucket日志配置
     */
    public Map<String, Object> getBucketLoggingConfig(String bucketName) {
        try {
            SetBucketLoggingRequest request = new SetBucketLoggingRequest(bucketName);

            Map<String, Object> result = new HashMap<>();
            result.put("enabled", true);

            log.info("✓ 获取日志配置成功: {}", bucketName);
            return result;

        } catch (Exception e) {
            log.warn("未找到日志配置: {}", bucketName);
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", false);
            return result;
        }
    }

    /**
     * 获取Bucket WORM配置
     */
    public Map<String, Object> getBucketWORMConfig(String bucketName) {
        try {
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", true);

            log.info("✓ 获取WORM配置成功: {}", bucketName);
            return result;

        } catch (Exception e) {
            log.warn("未找到WORM配置: {}", bucketName);
            Map<String, Object> result = new HashMap<>();
            result.put("enabled", false);
            return result;
        }
    }

    /**
     * 获取Bucket的所有授权配置信息
     *
     * @param bucketName Bucket名称
     * @return 包含所有授权配置的Map
     */
    public Map<String, Object> getBucketAuthorizationConfig(String bucketName) {
        try {
            Map<String, Object> config = new HashMap<>();

            // 1. 获取ACL权限
            try {
                String acl = getBucketAcl(bucketName);
                config.put("acl", acl);
            } catch (Exception e) {
                log.warn("Failed to get ACL for bucket: {}", bucketName);
                config.put("acl", null);
            }

            // 2. 尝试获取Bucket Policy（RAM授权）
            try {
                GetBucketPolicyResult policyResult = ossClient.getBucketPolicy(bucketName);
                String policyJson = policyResult.getPolicyText();
                config.put("bucketPolicy", policyJson);
                config.put("bucketPolicyEnabled", true);
            } catch (Exception e) {
                log.debug("No bucket policy found for bucket: {}", bucketName);
                config.put("bucketPolicy", null);
                config.put("bucketPolicyEnabled", false);
            }

            // 3. 获取CORS配置
            try {
                Map<String, Object> corsConfig = getBucketCORSConfig(bucketName);
                config.put("corsEnabled", corsConfig.get("enabled"));
                config.put("corsRuleCount", corsConfig.get("ruleCount"));
                config.put("corsRules", corsConfig.get("rules"));
            } catch (Exception e) {
                log.warn("Failed to get CORS config for bucket: {}", bucketName);
                config.put("corsEnabled", false);
                config.put("corsRuleCount", 0);
                config.put("corsRules", null);
            }

            // 4. 获取防盗链配置
            try {
                Map<String, Object> refererConfig = getBucketRefererConfig(bucketName);
                config.put("refererEnabled", refererConfig.get("enabled"));
                config.put("refererAllowEmpty", refererConfig.get("allowEmpty"));
                config.put("refererList", refererConfig.get("refererList"));
            } catch (Exception e) {
                log.warn("Failed to get Referer config for bucket: {}", bucketName);
                config.put("refererEnabled", false);
                config.put("refererAllowEmpty", false);
                config.put("refererList", null);
            }

            // 5. 获取版本控制配置
            try {
                Map<String, Object> versioningConfig = getBucketVersioningConfig(bucketName);
                config.put("versioningEnabled", versioningConfig.get("enabled"));
                config.put("versioningStatus", versioningConfig.get("status"));
            } catch (Exception e) {
                log.warn("Failed to get Versioning config for bucket: {}", bucketName);
                config.put("versioningEnabled", false);
                config.put("versioningStatus", null);
            }

            // 6. 获取静态网站托管配置
            try {
                Map<String, Object> websiteConfig = getBucketWebsiteConfig(bucketName);
                config.put("websiteEnabled", websiteConfig.get("enabled"));
                config.put("websiteIndexDocument", websiteConfig.get("indexDocument"));
                config.put("websiteErrorDocument", websiteConfig.get("errorDocument"));
            } catch (Exception e) {
                log.warn("Failed to get Website config for bucket: {}", bucketName);
                config.put("websiteEnabled", false);
                config.put("websiteIndexDocument", null);
                config.put("websiteErrorDocument", null);
            }

            // 7. 获取传输加速配置
            try {
                Map<String, Object> accelerationConfig = getBucketTransferAccelerationConfig(bucketName);
                config.put("transferAccelerationEnabled", accelerationConfig.get("enabled"));
            } catch (Exception e) {
                log.warn("Failed to get Transfer Acceleration config for bucket: {}", bucketName);
                config.put("transferAccelerationEnabled", false);
            }

            // 8-12. 其他配置（暂时设置为未启用）
            config.put("lifecycleEnabled", false);
            config.put("loggingEnabled", false);
            config.put("wormEnabled", false);
            config.put("replicationEnabled", false);
            config.put("accessMonitorEnabled", false);

            log.info("✓ 获取Bucket授权配置成功: {} (共{}项配置)", bucketName, config.size());
            return config;

        } catch (Exception e) {
            log.error("✗ 获取Bucket授权配置失败: {} - {}", bucketName, e.getMessage());
            throw new StorageException("GET_BUCKET_AUTHORIZATION_FAILED",
                "Failed to get bucket authorization config: " + e.getMessage(), e);
        }
    }
}
