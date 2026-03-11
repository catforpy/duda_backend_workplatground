package com.duda.common.enums;

/**
 * HTTP状态码枚举
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
public enum HttpCodeEnum {

    /**
     * 成功
     */
    SUCCESS(200, "success"),

    /**
     * 创建成功
     */
    CREATED(201, "created"),

    /**
     * 请求成功但无返回内容
     */
    NO_CONTENT(204, "no content"),

    /**
     * 参数错误
     */
    BAD_REQUEST(400, "bad request"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "unauthorized"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "forbidden"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "not found"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "method not allowed"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "request timeout"),

    /**
     * 请求实体过大
     */
    REQUEST_ENTITY_TOO_LARGE(413, "request entity too large"),

    /**
     * 业务错误
     */
    BIZ_ERROR(501, "business error"),

    /**
     * 系统内部错误
     */
    INTERNAL_SERVER_ERROR(500, "internal server error"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "service unavailable");

    private final Integer code;
    private final String desc;

    HttpCodeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据code获取枚举
     */
    public static HttpCodeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (HttpCodeEnum httpCodeEnum : values()) {
            if (httpCodeEnum.getCode().equals(code)) {
                return httpCodeEnum;
            }
        }
        return null;
    }
}
