package com.duda.common.redis.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类：用于自定义RedisTemplate的序列化方式
 * 替代Spring默认的RedisTemplate配置
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Configuration
// 条件注解：只有当RedisTemplate类存在于类路径下时，才会加载这个配置类
@ConditionalOnClass(RedisTemplate.class)
// 扫描当前模块下的utils包
@ComponentScan(basePackages = "com.duda.common.redis")
public class RedisConfig {

    /**
     * 自定义RedisTemplate Bean
     * Spring容器会优先使用这个Bean，而不是默认的RedisTemplate
     *
     * @param redisConnectionFactory Redis连接工厂（由Spring自动配置注入）
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // 创建RedisTemplate实例
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 设置Redis连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 创建自定义的JSON序列化器（用于值的序列化）
        DudaJackson2JsonRedisSerializer valueSerializer = new DudaJackson2JsonRedisSerializer();
        // 创建字符串序列化器（用于键的序列化）
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 配置键的序列化方式：使用字符串序列化器
        redisTemplate.setKeySerializer(stringRedisSerializer);
        // 配置值的序列化方式：使用自定义JSON序列化器
        redisTemplate.setValueSerializer(valueSerializer);

        // 配置哈希结构的键的序列化方式
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        // 配置哈希结构的值的序列化方式
        redisTemplate.setHashValueSerializer(valueSerializer);

        // 初始化RedisTemplate（应用配置）
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
