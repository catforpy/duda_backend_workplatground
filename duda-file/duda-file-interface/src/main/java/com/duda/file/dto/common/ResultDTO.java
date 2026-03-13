package com.duda.file.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通用结果DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     * - 200: 成功
     * - 400: 请求错误
     * - 401: 未授权
     * - 403: 禁止访问
     * - 404: 资源不存在
     * - 500: 服务器错误
     */
    @Builder.Default
    private Integer code = 200;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间
     */
    private LocalDateTime timestamp;

    /**
     * 请求ID(用于追踪)
     */
    private String requestId;

    /**
     * 是否成功
     */
    @Builder.Default
    private Boolean success = true;

    /**
     * 错误详情(失败时)
     */
    private ErrorDetail error;

    /**
     * 扩展信息
     */
    private java.util.Map<String, Object> extra;

    /**
     * 构建成功结果
     */
    public static <T> ResultDTO<T> success() {
        return ResultDTO.<T>builder()
                .code(200)
                .message("Success")
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 构建成功结果(带数据)
     */
    public static <T> ResultDTO<T> success(T data) {
        return ResultDTO.<T>builder()
                .code(200)
                .message("Success")
                .data(data)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 构建成功结果(带消息和数据)
     */
    public static <T> ResultDTO<T> success(String message, T data) {
        return ResultDTO.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .success(true)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 构建失败结果
     */
    public static <T> ResultDTO<T> failure(String message) {
        return ResultDTO.<T>builder()
                .code(500)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 构建失败结果(带错误码)
     */
    public static <T> ResultDTO<T> failure(Integer code, String message) {
        return ResultDTO.<T>builder()
                .code(code)
                .message(message)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 构建失败结果(带错误详情)
     */
    public static <T> ResultDTO<T> failure(Integer code, String message, ErrorDetail error) {
        return ResultDTO.<T>builder()
                .code(code)
                .message(message)
                .error(error)
                .success(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return success != null && success && code != null && code == 200;
    }

    /**
     * 错误详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 错误码
         */
        private String errorCode;

        /**
         * 错误消息
         */
        private String errorMessage;

        /**
         * 错误字段
         */
        private String errorField;

        /**
         * 错误堆栈(开发环境)
         */
        private String stackTrace;

        /**
         * 错误详细信息
         */
        private java.util.Map<String, Object> details;
    }
}
