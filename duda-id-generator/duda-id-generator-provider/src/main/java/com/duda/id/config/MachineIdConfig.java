package com.duda.id.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 机器ID配置
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Configuration
public class MachineIdConfig {

    private static final Logger logger = LoggerFactory.getLogger(MachineIdConfig.class);

    /**
     * 机器ID（通过环境变量或配置文件设置）
     * 范围：0-31（支持32台机器）
     *
     * 环境变量：SNOWFLAKE_MACHINE_ID
     * 配置文件：duda.snowflake.machine-id
     */
    @Value("${duda.snowflake.machine-id:1}")
    private long machineId;

    /**
     * 配置校验
     */
    @PostConstruct
    public void init() {
        if (machineId < 0 || machineId > 31) {
            throw new IllegalArgumentException(
                String.format("机器ID配置错误：必须在[0, 31]范围内，当前值：%d", machineId)
            );
        }
        logger.info("机器ID配置加载成功：{}", machineId);
    }

    public long getMachineId() {
        return machineId;
    }
}
