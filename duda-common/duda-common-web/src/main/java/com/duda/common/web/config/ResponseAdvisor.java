package com.duda.common.web.config;

import com.duda.common.domain.Result;
import com.duda.common.web.annotation.IgnoreResponseWrapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局响应增强器
 * 自动将Controller返回值包装为Result格式
 *
 * 注意：
 * - 如果Controller返回值已经是Result类型，则不会再次包装
 * - 可以通过&#64;IgnoreResponseWrapper注解跳过包装
 * - SpringDoc等第三方端点和字节数组响应不会被包装
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@RestControllerAdvice
public class ResponseAdvisor implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 检查是否有@IgnoreResponseWrapper注解
        IgnoreResponseWrapper annotation = returnType.getMethodAnnotation(IgnoreResponseWrapper.class);
        // 如果有注解且值为false，则不包装
        if (annotation != null && !annotation.value()) {
            return false;
        }

        // 检查方法返回类型，如果是byte[]或void，不包装
        Class<?> methodReturnType = returnType.getMethod().getReturnType();
        if (byte[].class.equals(methodReturnType) || void.class.equals(methodReturnType)) {
            return false;
        }

        // 检查声明类，排除SpringDoc相关的类
        String className = returnType.getDeclaringClass().getName();
        if (className.contains("springdoc") || className.contains("OpenApi")) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        // 如果返回值是byte[]，直接返回（不包装）
        if (body instanceof byte[]) {
            return body;
        }

        // 如果返回值已经是Result类型，直接返回
        if (body instanceof Result) {
            return body;
        }

        // 如果返回值是null，返回成功（无数据）
        if (body == null) {
            return Result.success();
        }

        // 其他情况，包装为Result
        return Result.success(body);
    }
}
