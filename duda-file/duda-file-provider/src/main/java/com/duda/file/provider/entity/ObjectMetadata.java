package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对象元数据实体
 * 对应object_metadata表
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 对象版本ID(开启版本控制时)
     */
    private String versionId;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * MIME类型
     */
    private String contentType;

    /**
     * MD5值
     */
    private String contentMd5;

    /**
     * CRC64校验值
     */
    private Long crc64;

    /**
     * 存储类型
     */
    private String storageClass;

    /**
     * 对象类型: NORMAL-普通, APPENDABLE-追加, SYMLINK-软链接
     */
    private String objectType;

    /**
     * 是否为目录
     */
    private Boolean isDirectory;

    /**
     * 是否为软链接
     */
    private Boolean isSymlink;

    /**
     * 软链接目标
     */
    private String symlinkTarget;

    /**
     * 访问权限
     */
    private String acl;

    /**
     * 分片上传ID
     */
    private String uploadId;

    /**
     * 分片数量
     */
    private Integer partCount;

    /**
     * 追加位置
     */
    private Long position;

    /**
     * 恢复状态: IN_PROGRESS, COMPLETED
     */
    private String restoreStatus;

    /**
     * 归档过期时间
     */
    private LocalDateTime expiryTime;

    /**
     * 对象ETag
     */
    private String etag;

    /**
     * 用户自定义元数据(JSON格式)
     */
    private String userMetadata;

    /**
     * 对象标签(JSON格式)
     */
    private String tags;

    /**
     * 上传IP
     */
    private String uploadIp;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 访问次数
     */
    private Long accessCount;

    /**
     * 下载次数
     */
    private Long downloadCount;

    /**
     * 状态: active-正常, deleted-已删除
     */
    private String status;

    /**
     * 创建者用户ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
