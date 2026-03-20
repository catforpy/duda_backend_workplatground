package com.duda.file.dto.object;

import com.duda.file.enums.AclType;
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

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Map;
import java.io.Serializable;

/**
 * 对象元数据DTO
 * 包含对象的详细元数据信息
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectMetadataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== HTTP标准头部 ====================

    /**
     * Content-Type(MIME类型)
     */
    private String contentType;

    /**
     * Content-Encoding(内容编码)
     */
    private String contentEncoding;

    /**
     * Cache-Control(缓存控制)
     */
    private String cacheControl;

    /**
     * Content-Disposition(内容展示方式)
     */
    private String contentDisposition;

    /**
     * Content-Language(内容语言)
     */
    private String contentLanguage;

    /**
     * Expires(过期时间)
     */
    private LocalDateTime expires;

    /**
     * Content-Length(内容长度)
     */
    private Long contentLength;

    // ==================== OSS自定义元数据 ====================

    /**
     * 对象存储类型
     */
    private StorageClass storageClass;

    /**
     * 对象访问权限
     */
    private AclType acl;

    /**
     * 服务器端加密算法
     */
    private String serverSideEncryption;

    /**
     * KMS密钥ID(使用SSE-KMS时)
     */
    private String serverSideEncryptionKeyId;

    /**
     * 对象ETag
     */
    private String eTag;

    /**
     * 对象最后修改时间
     */
    private LocalDateTime lastModified;

    /**
     * 对象创建时间
     */
    private LocalDateTime createTime;

    /**
     * 对象版本ID
     */
    private String versionId;

    /**
     * 删除标记ID
     */
    private String deleteMarkerId;

    /**
     * 对象标签数
     */
    private Integer tagCount;

    // ==================== 完整性校验 ====================

    /**
     * Content-MD5
     */
    private String contentMD5;

    /**
     * CRC64校验值
     */
    private Long crc64;

    // ==================== 归档存储 ====================

    /**
     * 对象是否为归档存储
     */
    private Boolean isArchive;

    /**
     * 归档状态
     */
    private String archiveStatus;

    /**
     * 归档恢复状态
     */
    private String restoreStatus;

    /**
     * 归档过期时间
     */
    private LocalDateTime expiryDate;

    // ==================== 自定义元数据 ====================

    /**
     * 用户自定义元数据
     * (以x-oss-meta-开头的自定义元数据)
     */
    private Map<String, String> userMetadata;

    // ==================== 对象类型 ====================

    /**
     * 对象类型
     */
    private String objectType;

    /**
     * 是否为软链接
     */
    private Boolean isSymlink;

    /**
     * 软链接目标
     */
    private String symlinkTarget;

    /**
     * 是否为追加对象
     */
    private Boolean isAppendable;

    /**
     * 下一次追加位置(如果是追加对象)
     */
    private Long nextAppendPosition;
}
