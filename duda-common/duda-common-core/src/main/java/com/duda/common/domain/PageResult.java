package com.duda.common.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 * 用于包装分页查询的结果
 *
 * 使用方法：
 * <pre>
 * PageResult pageResult = new PageResult(list, total, current, size);
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Data
public class PageResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 无参构造函数（用于 Swagger/SpringDoc 反射创建实例）
     */
    public PageResult() {
    }

    public PageResult(List records, Long total, Long current, Long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size;
    }

    /**
     * 创建分页结果（推荐使用）
     */
    public static PageResult of(List records, Long total, Long current, Long size) {
        return new PageResult(records, total, current, size);
    }

    /**
     * 空分页结果
     */
    public static PageResult empty() {
        return new PageResult(List.of(), 0L, 1L, 10L);
    }

    /**
     * 判断是否有数据
     */
    public boolean hasData() {
        return records != null && !records.isEmpty();
    }

    /**
     * 判断是否第一页
     */
    public boolean isFirstPage() {
        return this.current == 1;
    }

    /**
     * 判断是否最后一页
     */
    public boolean isLastPage() {
        return this.current >= this.pages;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "records=" + records +
                ", total=" + total +
                ", current=" + current +
                ", size=" + size +
                ", pages=" + pages +
                '}';
    }
}
