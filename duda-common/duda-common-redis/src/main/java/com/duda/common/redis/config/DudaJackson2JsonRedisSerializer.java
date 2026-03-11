package com.duda.common.redis.config;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * 自定义Redis JSON序列化器
 * 继承自Spring提供的GenericJackson2JsonRedisSerializer
 * 对字符串和字符类型做特殊处理
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
public class DudaJackson2JsonRedisSerializer extends GenericJackson2JsonRedisSerializer {

    /**
     * 构造方法：使用自定义的ObjectMapper配置
     */
    public DudaJackson2JsonRedisSerializer() {
        super(MapperFactory.newInstance());
    }

    /**
     * 重写序列化方法：对特定类型做特殊处理
     * @param source 要序列化的对象
     * @return 序列化后的字节数组
     * @throws SerializationException 序列化异常
     */
    @Override
    public byte[] serialize(Object source) throws SerializationException {
        // 如果被序列化的对象是 String 或 Character 类型
        if (source != null && ((source instanceof String) || (source instanceof Character))) {
            // 直接转换为字符串的字节数组（不经过 JSON 序列化）
            return source.toString().getBytes();
        }
        // 其他类型：调用父类的默认逻辑（用自定义 ObjectMapper 序列化为 JSON 字节数组）
        return super.serialize(source);
    }
}
