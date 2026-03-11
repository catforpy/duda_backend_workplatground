package com.duda.common.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果
 * 用于所有API接口的统一返回格式
 *
 * 使用方法：
 * <pre>
 * // 成功返回
 * return Result.success(data);
 *
 * // 失败返回
 * return Result.error("错误信息");
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Data
public class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private Object data;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 无参构造函数（用于 Swagger/SpringDoc 反射创建实例）
     */
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回（无数据）
     */
    public static Result success() {
        return new Result(200, "success", null);
    }

    /**
     * 成功返回（带数据）
     */
    public static Result success(Object data) {
        return new Result(200, "success", data);
    }

    /**
     * 成功返回（自定义消息）
     */
    public static Result success(String message, Object data) {
        return new Result(200, message, data);
    }

    /**
     * 失败返回（默认错误码500）
     */
    public static Result error() {
        return new Result(500, "system error", null);
    }

    /**
     * 失败返回（自定义消息）
     */
    public static Result error(String message) {
        return new Result(500, message, null);
    }

    /**
     * 失败返回（自定义错误码和消息）
     */
    public static Result error(int code, String message) {
        return new Result(code, message, null);
    }

    /**
     * 业务错误（错误码501）
     */
    public static Result bizError(String message) {
        return new Result(501, message, null);
    }

    /**
     * 参数错误（错误码400）
     */
    public static Result errorParam() {
        return new Result(400, "error param", null);
    }

    /**
     * 参数错误（自定义消息）
     */
    public static Result errorParam(String message) {
        return new Result(400, message, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == 200;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
