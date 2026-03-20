package com.duda.file.dto.upload;

import com.duda.file.dto.object.ObjectMetadataDTO;
import java.io.Serializable;
import com.duda.file.enums.StorageClass;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import lombok.Builder;
import java.io.Serializable;
import lombok.Data;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import java.io.Serializable;

import java.util.Map;
import java.io.Serializable;

/**
 * 初始化分片上传请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateMultipartUploadReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 对象元数据
     */
    private ObjectMetadataDTO metadata;

    /**
     * MIME类型
     */
    private String contentType;

    /**
     * 存储类型
     */
    private StorageClass storageClass;

    /**
     * 访问权限
     */
    private com.duda.file.enums.AclType acl;

    /**
     * 用户自定义元数据
     */
    private Map<String, String> userMetadata;

    /**
     * 服务端加密算法
     */
    private String serverSideEncryption;

    /**
     * KMS密钥ID(使用SSE-KMS时)
     */
    private String kmsKeyId;

    /**
     * 分片大小(字节)
     * 不设置则自动计算
     */
    private Long partSize;

    /**
     * 分片数量
     * 不设置则根据partSize自动计算
     */
    private Integer partCount;

    /**
     * 回调配置
     */
    private SimpleUploadReqDTO.CallbackConfig callbackConfig;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 扩展参数
     */
    private Map<String, Object> extra;
}
