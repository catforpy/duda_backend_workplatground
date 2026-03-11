package com.duda.common.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Web MVC 配置
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 使用 extendMessageConverters 而不是 configureMessageConverters
        // 这样可以在现有转换器基础上添加，而不是替换

        // 找到 Jackson 转换器并确保它支持所有 JSON 类型
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jsonConverter) {
                // Spring Boot 默认已经配置好了，不需要修改
                break;
            }
        }

        // 添加字符串转换器（如果还没有）
        boolean hasStringConverter = false;
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof StringHttpMessageConverter) {
                hasStringConverter = true;
                break;
            }
        }

        if (!hasStringConverter) {
            StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
            stringConverter.setWriteAcceptCharset(false);
            converters.add(stringConverter);
        }
    }
}
