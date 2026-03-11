package com.duda.common.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花算法ID生成器
 * 用于生成全局唯一的分布式ID
 *
 * 使用方法：
 * <pre>
 * long id = IdGenerator.nextId();
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
public class IdGenerator {

    // 起始时间戳（2026-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1735660800000L;

    // 机器ID位数（5位，支持32台机器）
    private static final long MACHINE_ID_BITS = 5L;

    // 序列号位数（12位，每台机器每毫秒可生成4096个ID）
    private static final long SEQUENCE_BITS = 12L;

    // 机器ID（可通过配置文件配置，这里默认为1）
    private final long machineId;

    // 序列号
    private final AtomicLong sequence = new AtomicLong(0);

    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;

    // 单例模式
    private static class SingletonHolder {
        private static final IdGenerator INSTANCE = new IdGenerator(1);
    }

    /**
     * 获取IdGenerator单例实例
     */
    public static IdGenerator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 私有构造函数
     * @param machineId 机器ID（0-31）
     */
    private IdGenerator(long machineId) {
        if (machineId < 0 || machineId > ((1 << MACHINE_ID_BITS) - 1)) {
            throw new IllegalArgumentException(String.format("machine Id can't be greater than %d or less than 0", (1 << MACHINE_ID_BITS) - 1));
        }
        this.machineId = machineId;
    }

    /**
     * 实例方法：生成下一个唯一ID
     * @return 唯一ID
     */
    public synchronized long generateId() {
        long currentTimestamp = System.currentTimeMillis();

        // 时钟回拨处理
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - currentTimestamp));
        }

        // 同一毫秒内，序列号自增
        if (currentTimestamp == lastTimestamp) {
            long sequenceMask = (1 << SEQUENCE_BITS) - 1;
            long sequenceValue = sequence.getAndIncrement() & sequenceMask;

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
        return ((currentTimestamp - START_TIMESTAMP) << (MACHINE_ID_BITS + SEQUENCE_BITS))
                | (machineId << SEQUENCE_BITS)
                | sequence.get();
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
     * 静态方法：生成下一个ID（推荐使用）
     * @return 唯一ID
     */
    public static long nextId() {
        return getInstance().generateId();
    }
}
