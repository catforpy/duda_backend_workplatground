package com.duda.file.rpc;

import com.duda.file.service.OSSSyncService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * OSS元数据同步RPC实现类
 * 对外提供 Dubbo RPC 服务
 *
 * @author DudaNexus
 * @since 2026-03-19
 */
@Slf4j
@DubboService(version = "1.0.0", group = "DUDA_FILE_GROUP", timeout = 300000)
public class OSSSyncRpcImpl implements IOSSSyncRpc {

    @Resource
    private OSSSyncService ossSyncService;

    @Override
    public int syncBucketObjects(String bucketName) {
        log.info("【RPC】Sync bucket objects: {}", bucketName);
        return ossSyncService.syncBucketObjects(bucketName);
    }

    @Override
    public int syncAllBuckets() {
        log.info("【RPC】Sync all buckets");
        return ossSyncService.syncAllBuckets();
    }

    @Override
    public int syncBucketObjectsByPrefix(String bucketName, String prefix) {
        log.info("【RPC】Sync bucket objects by prefix: {} (prefix: {})", bucketName, prefix);
        return ossSyncService.syncBucketObjectsByPrefix(bucketName, prefix);
    }
}
