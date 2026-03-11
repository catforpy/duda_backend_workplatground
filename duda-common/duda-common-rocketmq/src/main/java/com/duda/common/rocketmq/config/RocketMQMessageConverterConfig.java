package com.duda.common.rocketmq.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * RocketMQ 消息转换器配置
 * 用于消息的序列化和反序列化
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Configuration
public class RocketMQMessageConverterConfig {

    /**
     * 配置 RocketMQ 消息转换器
     * 支持 JSON 格式的消息序列化和反序列化
     *
     * @return 消息转换器
     */
    @Bean
    public MessageConverter rocketMQMessageConverter() {
        // 创建ObjectMapper，配置JSON序列化规则（复用Redis的MapperFactory逻辑）
        ObjectMapper objectMapper = createObjectMapper();

        // 创建JSON消息转换器
        MappingJackson2MessageConverter jsonConverter = new MappingJackson2MessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        jsonConverter.setStrictContentTypeMatch(false); // 宽松匹配Content-Type

        // 创建字符串消息转换器
        StringMessageConverter stringConverter = new StringMessageConverter();

        // 组合多个转换器
        List<MessageConverter> converters = new ArrayList<>();
        converters.add(jsonConverter);
        converters.add(stringConverter);

        // 创建组合转换器
        return new CompositeMessageConverter(converters);
    }

    /**
     * 创建配置好的ObjectMapper
     * 与Redis模块的MapperFactory保持一致
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 启用默认类型信息（用于反序列化时识别具体类型）
        objectMapper.enableDefaultTyping(
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // 忽略未知属性（避免因字段不匹配导致反序列化失败）
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return objectMapper;
    }
}
