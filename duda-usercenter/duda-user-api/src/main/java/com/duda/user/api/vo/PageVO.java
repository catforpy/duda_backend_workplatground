package com.duda.user.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应VO
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
@Data
public class PageVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    public PageVO() {
    }

    public PageVO(Integer pageNum, Integer pageSize, Long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;

        // 计算总页数
        if (total == null || total == 0) {
            this.totalPages = 0;
        } else {
            this.totalPages = (int) Math.ceil((double) total / pageSize);
        }

        // 计算是否有上一页/下一页
        this.hasNext = pageNum < totalPages;
        this.hasPrevious = pageNum > 1;
    }

    /**
     * 静态工厂方法 - 创建分页响应
     */
    public static <T> PageVO<T> of(Integer pageNum, Integer pageSize, Long total, List<T> records) {
        return new PageVO<>(pageNum, pageSize, total, records);
    }

    /**
     * 静态工厂方法 - 空分页
     */
    public static <T> PageVO<T> empty() {
        return new PageVO<>(1, 10, 0L, List.of());
    }
}
