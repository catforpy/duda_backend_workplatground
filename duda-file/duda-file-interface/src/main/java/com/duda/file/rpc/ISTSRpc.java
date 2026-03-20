package com.duda.file.rpc;

import com.duda.file.dto.upload.STSCredentialsDTO;

/**
 * STS RPC 接口
 * 对外提供 Dubbo RPC 服务,用于生成临时访问凭证
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
public interface ISTSRpc {

    /**
     * 生成STS临时凭证
     * 用于客户端直传云存储时的临时授权
     *
     * @param bucketName Bucket名称
     * @param objectPrefix 对象前缀(用于限制权限范围)
     * @param durationSeconds 过期时间(秒)
     * @param userId 用户ID（用于权限验证）
     * @return STS临时凭证
     */
    STSCredentialsDTO generateSTSCredentials(String bucketName, String objectPrefix, Long durationSeconds, Long userId);
}
