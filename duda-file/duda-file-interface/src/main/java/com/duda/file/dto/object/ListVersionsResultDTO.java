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

/**
 * 列出对象版本结果DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListVersionsResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 对象版本列表
     */
    private List<ObjectVersion> versions;

    /**
     * 删除标记列表
     */
    private List<DeleteMarker> deleteMarkers;

    /**
     * 公共前缀（模拟目录）
     */
    private List<String> commonPrefixes;

    /**
     * 是否截断（是否还有更多数据）
     */
    private Boolean isTruncated;

    /**
     * 下一次请求的keyMarker
     */
    private String nextKeyMarker;

    /**
     * 下一次请求的versionIdMarker
     */
    private String nextVersionIdMarker;

    /**
     * 返回的对象数量
     */
    private Integer keyCount;

    /**
     * 对象版本信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ObjectVersion {
        /**
         * 对象键
         */
        private String key;

        /**
         * 版本ID
         */
        private String versionId;

        /**
         * 是否是最新版本
         */
        private Boolean isLatest;

        /**
         * 最后修改时间
         */
        private String lastModified;

        /**
         * ETag
         */
        private String eTag;

        /**
         * 文件大小
         */
        private Long size;

        /**
         * 存储类型
         */
        private String storageClass;

        /**
         * 对象所有者ID
         */
        private String ownerId;

        /**
         * 对象所有者名称
         */
        private String ownerDisplayName;
    }

    /**
     * 删除标记信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeleteMarker {
        /**
         * 对象键
         */
        private String key;

        /**
         * 版本ID
         */
        private String versionId;

        /**
         * 是否是最新版本
         */
        private Boolean isLatest;

        /**
         * 最后修改时间
         */
        private String lastModified;

        /**
         * 对象所有者ID
         */
        private String ownerId;

        /**
         * 对象所有者名称
         */
        private String ownerDisplayName;
    }
}
