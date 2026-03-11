package com.duda.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询DTO
 * 用于所有分页查询接口
 *
 * 使用方法：
 * <pre>
 * &#64;PostMapping("/list")
 * public Result&lt;PageResult&lt;UserDTO&gt;&gt; list(@RequestBody PageQuery query) {
 *     // 使用查询逻辑
 * }
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Long current = 1L;

    /**
     * 每页条数
     */
    private Long size = 10L;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式（asc/desc）
     */
    private String sortOrder;

    /**
     * 获取当前页码（最小为1）
     */
    public Long getCurrent() {
        return current == null || current < 1 ? 1L : current;
    }

    /**
     * 获取每页条数（最小为1，最大为100）
     */
    public Long getSize() {
        if (size == null || size < 1) {
            return 10L;
        }
        return Math.min(size, 100L);
    }
}
