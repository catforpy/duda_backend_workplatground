package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 断点续传记录实体
 * 对应resume_upload_record表
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 断点续传记录ID(UUID)
     */
    private String recordId;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 本地文件路径
     */
    private String localFilePath;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 分片大小
     */
    private Long partSize;

    /**
     * 总分片数
     */
    private Integer totalPartCount;

    /**
     * 已上传分片列表(JSON字符串)
     * 格式: "[1,2,3,4,5]"
     */
    private String uploadedParts;

    /**
     * 状态: INIT, IN_PROGRESS, PAUSED, COMPLETED, FAILED, CANCELLED
     */
    private String uploadStatus;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户分片编号
     */
    private Integer userShard;

    /**
     * 客户端类型
     */
    private String clientType;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 并发线程数
     */
    private Integer concurrentThreads;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
