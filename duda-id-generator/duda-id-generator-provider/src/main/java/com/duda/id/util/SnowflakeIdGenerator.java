package com.duda.id.util;

import com.duda.id.config.MachineIdConfig;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花算法ID生成器
 * 基于Twitter Snowflake算法实现
 *
 * ID结构 (64位)：
 * - 1位：符号位（始终为0）
 * - 41位：时间戳（毫秒级）
 * - 5位：机器ID（支持0-31，共32台机器）
 * - 12位：序列号（每毫秒可生成4096个ID）
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Component
public class SnowflakeIdGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerator.class);

    // 起始时间戳（2026-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1735660800000L;

    // 各部分位数
    private static final long MACHINE_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    // 各部分最大值
    private static final long MAX_MACHINE_ID = (1L << MACHINE_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    // 各部分位移
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = MACHINE_ID_BITS + SEQUENCE_BITS;

    // 机器ID
    private final long machineId;

    // 序列号
    private final AtomicLong sequence = new AtomicLong(0);

    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     * @param machineIdConfig 机器ID配置
     */
    public SnowflakeIdGenerator(MachineIdConfig machineIdConfig) {
        long id = machineIdConfig.getMachineId();
        if (id < 0 || id > MAX_MACHINE_ID) {
            throw new IllegalArgumentException(
                String.format("机器ID必须在[0, %d]范围内，当前值：%d", MAX_MACHINE_ID, id)
            );
        }
        this.machineId = id;
        logger.info("雪花ID生成器初始化成功，机器ID：{}", machineId);
    }

    /**
     * 生成下一个唯一ID
     * @return 唯一ID
     */
    public synchronized long generateId() {
        long currentTimestamp = System.currentTimeMillis();

        // 时钟回拨检测
        if (currentTimestamp < lastTimestamp) {
            long offset = lastTimestamp - currentTimestamp;
            throw new RuntimeException(
                String.format("时钟回拨检测：回拨%d毫秒，拒绝生成ID以避免ID冲突", offset)
            );
        }

        // 同一毫秒内，序列号自增
        if (currentTimestamp == lastTimestamp) {
            long sequenceValue = sequence.getAndIncrement() & MAX_SEQUENCE;

            // 序列号溢出，等待下一毫秒
            if (sequenceValue == 0) {
                currentTimestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置
            sequence.set(0);
        }

        lastTimestamp = currentTimestamp;

        // 组合ID：时间戳差值 + 机器ID + 序列号
        long id = ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence.get();

        logger.debug("生成ID：{}", id);
        return id;
    }

    /**
     * 批量生成ID
     * @param count 生成数量
     * @return ID数组
     */
    public synchronized long[] generateBatch(int count) {
        if (count <= 0 || count > MAX_SEQUENCE) {
            throw new IllegalArgumentException(
                String.format("批量生成数量必须在[1, %d]范围内", MAX_SEQUENCE)
            );
        }

        long[] ids = new long[count];
        long currentTimestamp = System.currentTimeMillis();

        // 时钟回拨检测
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }

        if (currentTimestamp == lastTimestamp) {
            // 同一毫秒内，序列号连续递增
            for (int i = 0; i < count; i++) {
                ids[i] = ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                        | (machineId << MACHINE_ID_SHIFT)
                        | (i & MAX_SEQUENCE);
            }
            sequence.addAndGet(count);
        } else {
            // 新的毫秒，序列号从0开始
            for (int i = 0; i < count; i++) {
                ids[i] = ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                        | (machineId << MACHINE_ID_SHIFT)
                        | i;
            }
            sequence.set(count);
        }

        lastTimestamp = currentTimestamp;
        logger.info("批量生成{}个ID", count);
        return ids;
    }

    /**
     * 等待下一毫秒
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 获取当前机器ID
     * @return 机器ID
     */
    public long getMachineId() {
        return machineId;
    }
}
