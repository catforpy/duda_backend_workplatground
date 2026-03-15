package com.duda.file.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 获取STS临时凭证请求DTO
 *
 * @author duda
 * @date 2025-03-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSTSReqDTO implements Serializable {

    /**
     * 存储空间名称
     */
    private String bucketName;

    /**
     * 对象键前缀
     * 用于限制临时凭证的访问范围
     */
    private String objectPrefix;

    /**
     * 有效期(秒)
     * 范围: 900-3600 (15分钟-1小时)
     * 默认: 3600
     */
    private Long durationSeconds;

    /**
     * 权限类型
     * - ReadOnly: 只读
     * - ReadWrite: 读写
     * - FullControl: 完全控制
     */
    private String permissionType;

    /**
     * 允许访问的存储空间列表
     * 如果为空则可以使用bucketName指定的存储空间
     */
    private List<String> allowedBuckets;

    /**
     * 允许访问的前缀列表
     * 如果为空则可以访问所有前缀
     */
    private List<String> allowedPrefixes;

    /**
     * 自定义策略
     * JSON格式的策略文档
     */
    private String customPolicy;

    /**
     * 角色ARN
     * 用于扮演角色获取临时凭证
     */
    private String roleArn;

    /**
     * 角色会话名
     */
    private String roleSessionName;

    /**
     * 是否强制使用HTTPS
     */
    private Boolean httpsOnly;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 会话名称
     */
    private String sessionName;

    /**
     * 扩展参数
     */
    private Map<String, Object> extra;

    /**
     * 构建默认请求(读写权限,1小时有效期)
     */
    public static GetSTSReqDTO buildDefault(String bucketName, Long userId) {
        return GetSTSReqDTO.builder()
                .bucketName(bucketName)
                .userId(userId)
                .durationSeconds(3600L)
                .permissionType("ReadWrite")
                .httpsOnly(true)
                .build();
    }

    /**
     * 构建只读请求
     */
    public static GetSTSReqDTO buildReadOnly(String bucketName, Long userId, Long durationSeconds) {
        return GetSTSReqDTO.builder()
                .bucketName(bucketName)
                .userId(userId)
                .durationSeconds(durationSeconds)
                .permissionType("ReadOnly")
                .httpsOnly(true)
                .build();
    }

    /**
     * 构建限制前缀请求
     */
    public static GetSTSReqDTO buildWithPrefix(String bucketName, String prefix, Long userId) {
        return GetSTSReqDTO.builder()
                .bucketName(bucketName)
                .objectPrefix(prefix)
                .userId(userId)
                .durationSeconds(3600L)
                .permissionType("ReadWrite")
                .httpsOnly(true)
                .build();
    }

    /**
     * 验证请求参数
     */
    public boolean validate() {
        if (bucketName == null || bucketName.isEmpty()) {
            return false;
        }
        if (userId == null) {
            return false;
        }
        if (durationSeconds != null && (durationSeconds < 900 || durationSeconds > 3600)) {
            return false;
        }
        return true;
    }
}
