package com.duda.id.rpc;

import com.duda.id.api.IdGeneratorRpc;
import com.duda.id.util.SnowflakeIdGenerator;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 雪花ID生成RPC实现
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@DubboService(
    version = "1.0.0",
    group = "INFRA_GROUP",
    timeout = 5000,
    retries = 0
)
public class IdGeneratorRpcImpl implements IdGeneratorRpc {

    private static final Logger logger = LoggerFactory.getLogger(IdGeneratorRpcImpl.class);

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    @Override
    public Long generateUserId() {
        long id = snowflakeIdGenerator.generateId();
        logger.info("生成用户ID：{}", id);
        return id;
    }

    @Override
    public Long generateOrderId() {
        long id = snowflakeIdGenerator.generateId();
        logger.info("生成订单ID：{}", id);
        return id;
    }

    @Override
    public Long generateBusinessId(String businessType) {
        long id = snowflakeIdGenerator.generateId();
        logger.info("生成业务ID：businessType={}, id={}", businessType, id);
        return id;
    }

    @Override
    public Long[] generateBatch(String businessType, int count) {
        logger.info("批量生成ID：businessType={}, count={}", businessType, count);
        long[] ids = snowflakeIdGenerator.generateBatch(count);
        // 转换 long[] 为 Long[]
        Long[] result = new Long[ids.length];
        for (int i = 0; i < ids.length; i++) {
            result[i] = ids[i];
        }
        return result;
    }
}
