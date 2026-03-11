package com.duda.common.web.annotation;

import java.lang.annotation.*;

/**
 * 忽略响应包装注解
 * 标记在Controller方法上，表示该方法的返回值不需要被全局响应增强器包装
 *
 * 使用方法：
 * <pre>
 * &#64;IgnoreResponseWrapper
 * &#64;GetMapping("/raw-data")
 * public Object rawData() {
 *     return "原始数据";  // 不会被包装为Result
 * }
 *
 * &#64;GetMapping("/wrapped-data")
 * public Result&lt;String&gt; wrappedData() {
 *     return Result.success("包装数据");  // 已经是Result，不会再次包装
 * }
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreResponseWrapper {

    /**
     * 是否忽略包装
     * true：忽略包装
     * false：不忽略包装
     */
    boolean value() default true;
}
