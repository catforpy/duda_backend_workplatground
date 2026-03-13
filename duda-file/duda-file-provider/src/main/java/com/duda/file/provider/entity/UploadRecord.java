package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 上传记录实体
 * 对应upload_record表
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadRecord implements Serializable {

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
     * 分片上传ID(断点续传用)
     */
    private String uploadId;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 用户分片编号
     */
    private Integer userShard;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * MIME类型
     */
    private String contentType;

    /**
     * 上传方式: simple, multipart, append, form, sts, presigned
     */
    private String uploadMethod;

    /**
     * 分片数量
     */
    private Integer partCount;

    /**
     * 分片大小
     */
    private Long partSize;

    /**
     * 已上传分片数
     */
    private Integer uploadedParts;

    /**
     * 上传状态: INIT, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
     */
    private String uploadStatus;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 上传IP
     */
    private String uploadIp;

    /**
     * 客户端类型: web, app, mini_program
     */
    private String clientType;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
