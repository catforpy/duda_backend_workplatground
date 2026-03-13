package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 列出分片结果DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListPartsResultDTO {

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键
     */
    private String objectKey;

    /**
     * 上传ID
     */
    private String uploadId;

    /**
     * 分片列表
     * key: partNumber, eTag, size
     */
    private List<Map<String, Object>> parts;

    /**
     * 分片总数
     */
    private Integer partCount;

    /**
     * 下一次分页的标记
     */
    private String nextPartNumberMarker;

    /**
     * 是否被截断
     */
    private Boolean isTruncated;

    /**
     * 分片的最大编号
     */
    private Integer maxParts;

    /**
     * 分片编号标记
     */
    private String partNumberMarker;

    /**
     * 获取指定分片的信息
     */
    public Map<String, Object> getPart(int partNumber) {
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        return parts.stream()
                .filter(part -> partNumber == ((Integer) part.get("partNumber")))
                .findFirst()
                .orElse(null);
    }

    /**
     * 是否还有更多分片
     */
    public boolean hasMoreParts() {
        return isTruncated != null && isTruncated;
    }
}
