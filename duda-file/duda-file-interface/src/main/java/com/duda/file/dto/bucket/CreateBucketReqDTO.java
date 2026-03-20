package com.duda.file.dto.bucket;

import com.duda.file.enums.AclType;
import com.duda.file.enums.DataRedundancyType;
import com.duda.file.enums.StorageClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建Bucket请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBucketReqDTO implements java.io.Serializable {

    /**
     * Bucket名称（可选，不填则自动生成）
     * 规则：
     * - 全局唯一
     * - 只能包含小写字母、数字、短横线
     * - 长度3-63字符
     * - 必须以字母或数字开头和结尾
     */
    private String bucketName;

    /**
     * Bucket显示名称（用户自定义，可选）
     */
    private String displayName;

    /**
     * 存储类型
     */
    private String storageType;

    /**
     * 地域（例如：cn-hangzhou）
     */
    private String region;

    /**
     * 存储类型（默认：STANDARD）
     */
    private StorageClass storageClass;

    /**
     * 数据冗余类型（默认：LRS）
     */
    private DataRedundancyType dataRedundancyType;

    /**
     * 权限类型（默认：PRIVATE）
     */
    private AclType aclType;

    /**
     * 是否开启版本控制（默认：false）
     */
    private Boolean versioningEnabled;

    /**
     * Bucket标签
     */
    private java.util.Map<String, String> tags;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 最大文件大小（字节，可选）
     */
    private Long maxFileSize;

    /**
     * 最大文件数量（可选）
     */
    private Integer maxFileCount;

    /**
     * Bucket类别（可选）
     */
    private String category;

    /**
     * Bucket描述（可选）
     */
    private String description;

    /**
     * API密钥名称（用于选择使用哪个密钥创建Bucket）
     */
    private String keyName;
}
