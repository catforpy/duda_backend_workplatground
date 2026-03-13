package com.duda.file.dto;

import com.duda.file.enums.AclType;
import com.duda.file.enums.DataRedundancyType;
import com.duda.file.enums.StorageClass;
import lombok.Builder;
import lombok.Data;

/**
 * 创建Bucket请求DTO
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
@Data
@Builder
public class CreateBucketReqDTO {

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
}
