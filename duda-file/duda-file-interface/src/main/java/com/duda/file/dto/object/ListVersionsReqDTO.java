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
 * 列出对象版本请求DTO
 *
 * @author duda
 * @date 2025-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListVersionsReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Bucket名称
     */
    private String bucketName;

    /**
     * 前缀（可选）
     */
    private String prefix;

    /**
     * 分隔符（可选，用于模拟目录）
     */
    private String delimiter;

    /**
     * 分页标记（keyMarker）
     */
    private String keyMarker;

    /**
     * 版本ID标记（versionIdMarker）
     */
    private String versionIdMarker;

    /**
     * 最大返回数量
     */
    private Integer maxKeys;

    /**
     * 编码方式
     */
    private String encodingType;
}
