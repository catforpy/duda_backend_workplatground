package com.duda.common.redis.cache;

import com.duda.common.redis.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * 延迟双删缓存策略
 *
 * 用于解决更新数据库和删除缓存之间的时序问题
 *
 * 执行流程：
 * 1. 删除缓存
 * 2. 更新数据库
 * 3. 休眠一段时间（比如500ms）
 * 4. 再次删除缓存
 *
 * 适用场景：写操作频繁，对一致性要求较高的业务
 * 优点：最大限度保证缓存和数据库的一致性
 * 缺点：需要休眠，影响性能
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Component
public class DoubleDeleteCache {

    private static final Logger logger = LoggerFactory.getLogger(DoubleDeleteCache.class);

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 异步执行延迟双删的线程池
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 默认延迟时间（毫秒）
     */
    private static final long DEFAULT_DELAY_MS = 500L;

    /**
     * 延迟双删 - 同步版本
     *
     * @param cacheKey 缓存key
     * @param dbUpdater 数据库更新函数
     * @param delayMs 延迟时间（毫秒）
     * @return 是否成功
     */
    public boolean updateSync(String cacheKey, Function<String, Boolean> dbUpdater, long delayMs) {
        try {
            // 1. 第一次删除缓存
            redisUtils.delete(cacheKey);
            logger.debug("延迟双删：第一次删除缓存，key: {}", cacheKey);

            // 2. 更新数据库
            boolean updateResult = dbUpdater.apply(cacheKey);
            if (!updateResult) {
                logger.error("延迟双删：数据库更新失败，key: {}", cacheKey);
                return false;
            }
            logger.debug("延迟双删：数据库更新成功，key: {}", cacheKey);

            // 3. 休眠指定时间
            Thread.sleep(delayMs);

            // 4. 第二次删除缓存
            redisUtils.delete(cacheKey);
            logger.debug("延迟双删：第二次删除缓存，key: {}, delayMs: {}", cacheKey, delayMs);

            return true;
        } catch (Exception e) {
            logger.error("延迟双删失败，key: {}", cacheKey, e);
            return false;
        }
    }

    /**
     * 延迟双删 - 同步版本（使用默认延迟时间）
     *
     * @param cacheKey 缓存key
     * @param dbUpdater 数据库更新函数
     * @return 是否成功
     */
    public boolean updateSync(String cacheKey, Function<String, Boolean> dbUpdater) {
        return updateSync(cacheKey, dbUpdater, DEFAULT_DELAY_MS);
    }

    /**
     * 延迟双删 - 异步版本
     *
     * 第二次删除操作异步执行，不阻塞主流程
     *
     * @param cacheKey 缓存key
     * @param dbUpdater 数据库更新函数
     * @param delayMs 延迟时间（毫秒）
     * @return CompletableFuture<Boolean>
     */
    public CompletableFuture<Boolean> updateAsync(String cacheKey, Function<String, Boolean> dbUpdater, long delayMs) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 第一次删除缓存
                redisUtils.delete(cacheKey);
                logger.debug("延迟双删（异步）：第一次删除缓存，key: {}", cacheKey);

                // 2. 更新数据库
                boolean updateResult = dbUpdater.apply(cacheKey);
                if (!updateResult) {
                    logger.error("延迟双删（异步）：数据库更新失败，key: {}", cacheKey);
                    return false;
                }
                logger.debug("延迟双删（异步）：数据库更新成功，key: {}", cacheKey);

                // 3. 延迟后第二次删除（异步）
                Thread.sleep(delayMs);
                redisUtils.delete(cacheKey);
                logger.debug("延迟双删（异步）：第二次删除缓存，key: {}, delayMs: {}", cacheKey, delayMs);

                return true;
            } catch (Exception e) {
                logger.error("延迟双删（异步）失败，key: {}", cacheKey, e);
                return false;
            }
        }, executorService);
    }

    /**
     * 延迟双删 - 异步版本（使用默认延迟时间）
     *
     * @param cacheKey 缓存key
     * @param dbUpdater 数据库更新函数
     * @return CompletableFuture<Boolean>
     */
    public CompletableFuture<Boolean> updateAsync(String cacheKey, Function<String, Boolean> dbUpdater) {
        return updateAsync(cacheKey, dbUpdater, DEFAULT_DELAY_MS);
    }
}
