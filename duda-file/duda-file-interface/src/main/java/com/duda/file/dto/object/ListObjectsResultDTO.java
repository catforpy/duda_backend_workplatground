package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 列出对象结果DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListObjectsResultDTO {

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象前缀
     */
    private String prefix;

    /**
     * 分隔符
     */
    private String delimiter;

    /**
     * 分页标记
     * 下一页的起始位置
     */
    private String marker;

    /**
     * 下一次分页的标记
     */
    private String nextMarker;

    /**
     * 对象列表
     */
    private List<ObjectDTO> objects;

    /**
     * 公共前缀列表(模拟目录)
     */
    private List<String> commonPrefixes;

    /**
     * 是否截断
     * true表示还有更多对象未列出
     */
    private Boolean truncated;

    /**
     * 本次返回的对象数量
     */
    private Integer objectCount;

    /**
     * 总对象数量
     * 注意：某些云厂商可能不返回此值
     */
    private Integer totalCount;

    /**
     * 继续令牌
     * 用于继续下一次列表操作
     */
    private String nextContinuationToken;

    /**
     * 编码类型
     */
    private String encodingType;

    /**
     * 起始位置
     */
    private String startAfter;

    /**
     * 是否为空结果
     */
    public boolean isEmpty() {
        return (objects == null || objects.isEmpty()) &&
                (commonPrefixes == null || commonPrefixes.isEmpty());
    }

    /**
     * 是否有下一页
     */
    public boolean hasNextPage() {
        return truncated != null && truncated && nextMarker != null;
    }

    /**
     * 获取所有对象和目录的总数
     */
    public int getTotalCount() {
        int count = 0;
        if (objects != null) {
            count += objects.size();
        }
        if (commonPrefixes != null) {
            count += commonPrefixes.size();
        }
        return count;
    }
}
