package com.duda.file.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResultDTO<T> {

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码(从1开始)
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否为第一页
     */
    private Boolean isFirst;

    /**
     * 是否为最后一页
     */
    private Boolean isLast;

    /**
     * 构建空结果
     */
    public static <T> PageResultDTO<T> empty() {
        return PageResultDTO.<T>builder()
                .records(List.of())
                .total(0L)
                .current(1)
                .size(10)
                .pages(0)
                .hasPrevious(false)
                .hasNext(false)
                .isFirst(true)
                .isLast(true)
                .build();
    }

    /**
     * 构建分页结果
     */
    public static <T> PageResultDTO<T> of(List<T> records, Long total, Integer current, Integer size) {
        int pages = (int) Math.ceil((double) total / size);
        boolean hasPrevious = current > 1;
        boolean hasNext = current < pages;

        return PageResultDTO.<T>builder()
                .records(records)
                .total(total)
                .current(current)
                .size(size)
                .pages(pages)
                .hasPrevious(hasPrevious)
                .hasNext(hasNext)
                .isFirst(!hasPrevious)
                .isLast(!hasNext)
                .build();
    }

    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    /**
     * 获取记录数量
     */
    public int getRecordCount() {
        return records != null ? records.size() : 0;
    }

    /**
     * 获取下一页页码
     */
    public Integer getNextPage() {
        return hasNext ? current + 1 : null;
    }

    /**
     * 获取上一页页码
     */
    public Integer getPreviousPage() {
        return hasPrevious ? current - 1 : null;
    }

    /**
     * 获取起始位置(从0开始)
     */
    public long getOffset() {
        return (current - 1) * size;
    }

    /**
     * 获取结束位置
     */
    public long getEnd() {
        return Math.min(current * size, total);
    }
}
