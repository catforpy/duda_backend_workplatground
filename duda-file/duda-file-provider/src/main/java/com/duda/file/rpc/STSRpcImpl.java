package com.duda.file.rpc;

import com.duda.file.dto.upload.STSCredentialsDTO;
import com.duda.file.service.STSService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * STS RPC 实现类
 * 对外提供 Dubbo RPC 服务,注册到Nacos
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@DubboService(version = "1.0.0", group = "DUDA_FILE_GROUP", timeout = 30000)
public class STSRpcImpl implements ISTSRpc {

    @Resource
    private STSService stsService;

    @Override
    public STSCredentialsDTO generateSTSCredentials(String bucketName, String objectPrefix, Long durationSeconds, Long userId) {
        log.info("【RPC】Generating STS credentials: bucket={}, prefix={}, userId={}", bucketName, objectPrefix, userId);
        return stsService.generateSTSCredentials(bucketName, objectPrefix, durationSeconds, userId);
    }
}
