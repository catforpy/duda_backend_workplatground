package com.duda.file.provider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 文件统计实体
 * 对应file_statistics表
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileStatistics implements Serializable {

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
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 统计类型: daily-每日, monthly-每月
     */
    private String statType;

    /**
     * 文件总数
     */
    private Long totalFiles;

    /**
     * 总存储量(字节)
     */
    private Long totalSize;

    /**
     * 上传次数
     */
    private Long uploadCount;

    /**
     * 下载次数
     */
    private Long downloadCount;

    /**
     * 上传流量(字节)
     */
    private Long trafficUpload;

    /**
     * 下载流量(字节)
     */
    private Long trafficDownload;

    /**
     * 新增文件数
     */
    private Long newFiles;

    /**
     * 删除文件数
     */
    private Long deletedFiles;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
