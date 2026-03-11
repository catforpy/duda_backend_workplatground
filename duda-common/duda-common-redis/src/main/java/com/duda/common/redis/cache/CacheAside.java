package com.duda.common.redis.cache;

import com.duda.common.redis.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Cache Aside 缓存策略（推荐模式）
 *
 * 读流程：查缓存 → 缓存未命中则查 DB → 将数据写入缓存
 * 写流程：更新 DB → 删除缓存（而不是更新缓存）
 *
 * 适用场景：读多写少的绝大多数业务
 * 优点：一致性高，实现简单
 * 缺点：首次读会miss，需要查库
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Component
public class CacheAside {

    private static final Logger logger = LoggerFactory.getLogger(CacheAside.class);

    @Autowired
    private RedisUtils redisUtils;

    /**
     * Cache Aside 读操作
     *
     * @param cacheKey 缓存key
     * @param clazz 返回类型
     * @param dbLoader 数据库加载函数
     * @param expireSeconds 过期时间（秒）
     * @param <T> 泛型类型
     * @return 数据
     */
    public <T> T get(String cacheKey, Class<T> clazz, Function<String, T> dbLoader, long expireSeconds) {
        // 1. 先查缓存
        T cachedData = redisUtils.get(cacheKey, clazz);
        if (cachedData != null) {
            logger.debug("缓存命中，key: {}", cacheKey);
            return cachedData;
        }

        // 2. 缓存未命中，查询数据库
        logger.debug("缓存未命中，查询数据库，key: {}", cacheKey);
        T dbData = dbLoader.apply(cacheKey);
        if (dbData == null) {
            logger.warn("数据库中也不存在该数据，key: {}", cacheKey);
            return null;
        }

        // 3. 将数据写入缓存
        redisUtils.set(cacheKey, dbData, expireSeconds);
        logger.debug("数据已写入缓存，key: {}, expireSeconds: {}", cacheKey, expireSeconds);

        return dbData;
    }

    /**
     * Cache Aside 写操作
     *
     * 策略：先更新数据库，然后删除缓存
     *
     * @param cacheKey 缓存key
     * @param dbUpdater 数据库更新函数
     * @return 是否成功
     */
    public boolean update(String cacheKey, Function<String, Boolean> dbUpdater) {
        try {
            // 1. 先更新数据库
            boolean updateResult = dbUpdater.apply(cacheKey);
            if (!updateResult) {
                logger.error("数据库更新失败，key: {}", cacheKey);
                return false;
            }

            // 2. 删除缓存（而不是更新缓存）
            redisUtils.delete(cacheKey);
            logger.debug("数据库更新成功，缓存已删除，key: {}", cacheKey);

            return true;
        } catch (Exception e) {
            logger.error("Cache Aside 更新失败，key: {}", cacheKey, e);
            return false;
        }
    }

    /**
     * Cache Aside 删除操作
     *
     * @param cacheKey 缓存key
     * @param dbDeleter 数据库删除函数
     * @return 是否成功
     */
    public boolean delete(String cacheKey, Function<String, Boolean> dbDeleter) {
        try {
            // 1. 先删除数据库
            boolean deleteResult = dbDeleter.apply(cacheKey);
            if (!deleteResult) {
                logger.error("数据库删除失败，key: {}", cacheKey);
                return false;
            }

            // 2. 删除缓存
            redisUtils.delete(cacheKey);
            logger.debug("数据库删除成功，缓存已删除，key: {}", cacheKey);

            return true;
        } catch (Exception e) {
            logger.error("Cache Aside 删除失败，key: {}", cacheKey, e);
            return false;
        }
    }
}
