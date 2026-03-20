package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import java.io.Serializable;
import lombok.Builder;
import java.io.Serializable;
import lombok.Data;
import java.io.Serializable;
import lombok.NoArgsConstructor;
import java.io.Serializable;

import java.util.List;
import java.io.Serializable;
import java.util.Map;
import java.io.Serializable;

/**
 * 目录统计信息DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 目录路径
     */
    private String directoryPath;

    /**
     * 文件总数(递归统计所有子目录)
     */
    private Long fileCount;

    /**
     * 目录总数(递归统计所有子目录)
     */
    private Long directoryCount;

    /**
     * 总文件大小(字节,递归统计)
     */
    private Long totalSize;

    /**
     * 平均文件大小(字节)
     */
    private Long averageSize;

    /**
     * 最大文件大小(字节)
     */
    private Long maxSize;

    /**
     * 最小文件大小(字节)
     */
    private Long minSize;

    /**
     * 文件大小分布
     * key: 文件大小区间(如: <1KB, 1KB-1MB, 1MB-100MB, 100MB-1GB, >=1GB)
     * value: 文件数量
     */
    private Map<String, Long> sizeDistribution;

    /**
     * 文件类型分布
     * key: 文件扩展名(如: jpg, mp4, pdf)
     * value: 文件数量
     */
    private Map<String, Long> fileTypeDistribution;

    /**
     * 存储类型分布
     * key: 存储类型(STANDARD, IA, ARCHIVE, COLD_ARCHIVE)
     * value: 文件数量
     */
    private Map<String, Long> storageClassDistribution;

    /**
     * 最后修改时间
     */
    private Long lastModifiedTime;

    /**
     * 最早创建时间
     */
    private Long earliestCreateTime;

    /**
     * 最新创建时间
     */
    private Long latestCreateTime;

    /**
     * 子目录列表(仅一级子目录)
     */
    private List<String> subDirectories;

    /**
     * 统计时间
     */
    private Long statisticsTime;

    /**
     * 是否为实时统计
     * false表示缓存数据
     */
    private Boolean realTime;

    /**
     * 获取人类可读的总大小
     */
    public String getHumanReadableTotalSize() {
        if (totalSize == null) {
            return "unknown";
        }
        if (totalSize < 1024) {
            return totalSize + " B";
        } else if (totalSize < 1024 * 1024) {
            return String.format("%.2f KB", totalSize / 1024.0);
        } else if (totalSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", totalSize / (1024.0 * 1024));
        } else if (totalSize < 1024L * 1024 * 1024 * 1024) {
            return String.format("%.2f GB", totalSize / (1024.0 * 1024 * 1024));
        } else {
            return String.format("%.2f TB", totalSize / (1024.0 * 1024 * 1024 * 1024));
        }
    }

    /**
     * 获取总对象数(文件+目录)
     */
    public Long getTotalObjectCount() {
        long files = fileCount != null ? fileCount : 0;
        long dirs = directoryCount != null ? directoryCount : 0;
        return files + dirs;
    }
}
