package com.duda.file.provider.sync;

import com.duda.file.service.OSSSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * OSS元数据同步定时任务
 *
 * @author DudaNexus
 * @since 2026-03-19
 */
@Slf4j
@Component
public class OSSSyncScheduledTask {

    @Autowired
    private OSSSyncService ossSyncService;

    /**
     * 定时同步任务：每小时同步一次所有Bucket的元数据
     * Cron表达式：每小时第0分钟执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncAllBucketsHourly() {
        log.info("========== 开始定时同步所有Bucket的OSS元数据 ==========");

        try {
            long startTime = System.currentTimeMillis();

            // 执行同步
            int totalCount = ossSyncService.syncAllBuckets();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("========== 定时同步完成: 同步对象数={}, 耗时={}ms ==========",
                totalCount, duration);

        } catch (Exception e) {
            log.error("========== 定时同步失败 ==========", e);
        }
    }

    /**
     * 定时同步任务：每天凌晨2点同步一次（用于全量同步）
     * Cron表达式：每天凌晨2点0分执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncAllBucketsDaily() {
        log.info("========== 开始每日全量同步所有Bucket的OSS元数据 ==========");

        try {
            long startTime = System.currentTimeMillis();

            // 执行同步
            int totalCount = ossSyncService.syncAllBuckets();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("========== 每日全量同步完成: 同步对象数={}, 耗时={}ms ==========",
                totalCount, duration);

        } catch (Exception e) {
            log.error("========== 每日全量同步失败 ==========", e);
        }
    }
}
