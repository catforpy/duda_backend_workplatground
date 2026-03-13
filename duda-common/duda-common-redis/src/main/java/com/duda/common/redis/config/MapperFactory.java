package com.duda.common.redis.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.support.NullValue;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 对象映射器工厂：创建并配置Jackson的ObjectMapper
 * 用于JSON序列化和反序列化的配置
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
public class MapperFactory {

    /**
     * 创建默认配置的ObjectMapper实例
     * @return 配置好的ObjectMapper
     */
    public static ObjectMapper newInstance() {
        return initMapper(new ObjectMapper(), (String) null);
    }

    /**
     * 初始化并配置ObjectMapper
     * @param mapper 要配置的ObjectMapper实例
     * @param classPropertyTypeName 用于存储类型信息的属性名
     * @return 配置好的ObjectMapper
     */
    private static ObjectMapper initMapper(ObjectMapper mapper, String classPropertyTypeName) {
        // 注册JavaTimeModule，支持Java 8日期时间类型（LocalDateTime等）
        mapper.registerModule(new JavaTimeModule());

        // 注册自定义模块，添加空值序列化器
        mapper.registerModule(new SimpleModule().addSerializer(new MapperNullValueSerializer(classPropertyTypeName)));

        // 配置类型信息的序列化方式
        if (StringUtils.hasText(classPropertyTypeName)) {
            // 如果指定了类型属性名，则使用该属性名存储类型信息
            mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, classPropertyTypeName);
        } else {
            // 默认：使用@class属性存储类型信息
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        }

        // 配置反序列化特性：忽略未知属性（避免因类结构变化导致反序列化失败）
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return mapper;
    }

    /**
     * 自定义空值序列化器：处理NullValue类型的序列化
     * 解决Spring缓存中null值的序列化问题
     */
    private static class MapperNullValueSerializer extends StdSerializer<NullValue> {

        private static final long serialVersionUID = -1L;

        // 用于标识类型的属性名（默认为"@class"）
        private final String classIdentifier;

        private MapperNullValueSerializer(String classIdentifier) {
            super(NullValue.class);
            // 如果未指定类型属性名，则使用默认值"@class"
            this.classIdentifier = StringUtils.hasText(classIdentifier) ? classIdentifier : "@class";
        }

        /**
         * 序列化NullValue对象
         * 将NullValue序列化为包含类型信息的JSON对象
         */
        @Override
        public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {
            jgen.writeStartObject();
            // 写入类型信息，方便反序列化时识别
            jgen.writeStringField(classIdentifier, NullValue.class.getName());
            jgen.writeEndObject();
        }
    }
}
