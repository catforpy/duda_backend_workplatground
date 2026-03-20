package com.duda.file.dto.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 列出对象请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListObjectsReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键前缀
     * 用于过滤对象,只返回以该前缀开头的对象
     */
    private String prefix;

    /**
     * 分隔符
     * 通常为"/",用于模拟目录结构
     */
    private String delimiter;

    /**
     * 分页标记
     * 从该位置开始列出对象
     */
    private String marker;

    /**
     * 最大对象数量
     * 范围: 1-1000, 默认100
     */
    private Integer maxKeys;

    /**
     * 编码方式
     * 对对象键进行编码,默认不编码
     */
    private String encodingType;

    /**
     * 是否递归列出所有对象
     * true: 不使用delimiter,递归列出所有对象
     * false: 使用delimiter,只列出当前层级
     */
    private Boolean recursive;

    /**
     * 起始位置
     * 与marker类似,但指定对象键
     */
    private String startAfter;

    /**
     * 继续令牌
     * 用于继续上一次的列表操作
     */
    private String continuationToken;

    /**
     * 编码
     * URL编码类型:url
     */
    private String encoding;

    /**
     * 所有者信息
     * 是否在结果中包含对象所有者信息
     */
    private Boolean fetchOwner;

    /**
     * 用户ID (用于权限验证)
     */
    private Long userId;

    /**
     * 获取默认的分页大小
     */
    public static final int DEFAULT_MAX_KEYS = 100;

    /**
     * 获取最大的分页大小
     */
    public static final int MAX_MAX_KEYS = 1000;

    /**
     * 获取默认的分隔符
     */
    public static final String DEFAULT_DELIMITER = "/";

    /**
     * 构建默认请求
     */
    public static ListObjectsReqDTO buildDefault(String bucketName) {
        return ListObjectsReqDTO.builder()
                .bucketName(bucketName)
                .maxKeys(DEFAULT_MAX_KEYS)
                .delimiter(DEFAULT_DELIMITER)
                .recursive(false)
                .build();
    }

    /**
     * 构建递归列表请求
     */
    public static ListObjectsReqDTO buildRecursive(String bucketName, String prefix) {
        return ListObjectsReqDTO.builder()
                .bucketName(bucketName)
                .prefix(prefix)
                .maxKeys(MAX_MAX_KEYS)
                .recursive(true)
                .delimiter(null)
                .build();
    }
}
