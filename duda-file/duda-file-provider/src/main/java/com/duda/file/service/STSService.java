package com.duda.file.service;

import com.duda.file.dto.upload.STSCredentialsDTO;

/**
 * STS Service 接口
 * 内部业务逻辑接口
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
public interface STSService {

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
