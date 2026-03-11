package com.duda.common.web.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于业务逻辑中的异常抛出
 *
 * 使用方法：
 * <pre>
 * if (user == null) {
 *     throw new BizException("用户不存在");
 * }
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final int code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 默认错误码（501）
     */
    public BizException(String message) {
        super(message);
        this.code = 501;
        this.message = message;
    }

    /**
     * 自定义错误码和消息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 自定义错误码和消息（带cause）
     */
    public BizException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "BizException{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
