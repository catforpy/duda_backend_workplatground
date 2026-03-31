package com.duda.tenant.api.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果VO
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Data
public class ResultVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public ResultVO() {
        this.timestamp = System.currentTimeMillis();
    }

    public ResultVO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "success", data);
    }

    public static <T> ResultVO<T> success(String message, T data) {
        return new ResultVO<>(200, message, data);
    }

    public static <T> ResultVO<T> error(String message) {
        return new ResultVO<>(500, message, null);
    }

    public static <T> ResultVO<T> error(Integer code, String message) {
        return new ResultVO<>(code, message, null);
    }
}
