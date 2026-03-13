package com.duda.file.manager.support;

import com.duda.file.common.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 权限检查器
 * 用于验证用户是否有权限操作指定的Bucket或Object
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
@Component
public class PermissionChecker {

    /**
     * 检查用户是否有权操作该Bucket
     *
     * @param bucketName Bucket名称
     * @param userId 用户ID
     * @return 是否有权限
     */
    public boolean checkBucketPermission(String bucketName, Long userId) {
        if (bucketName == null || bucketName.isEmpty()) {
            return false;
        }
        if (userId == null) {
            return false;
        }

        // TODO: 从数据库查询Bucket配置,验证用户权限
        // 1. 查询Bucket的所有者
        // 2. 检查用户是否为所有者或有权限的用户
        // 3. 返回检查结果

        // 临时实现: 简单的名称前缀检查
        // 例如: user-{userId}-* 表示属于该用户的Bucket
        return bucketName.startsWith("user-" + userId + "-") ||
               bucketName.startsWith("tenant-") ||
               bucketName.startsWith("public-");
    }

    /**
     * 检查用户是否有权操作该Object
     *
     * @param bucketName Bucket名称
     * @param objectKey Object键
     * @param userId 用户ID
     * @return 是否有权限
     */
    public boolean checkObjectPermission(String bucketName, String objectKey, Long userId) {
        // Object权限检查基于Bucket权限
        return checkBucketPermission(bucketName, userId);
    }

    /**
     * 验证用户权限,无权限时抛出异常
     *
     * @param bucketName Bucket名称
     * @param userId 用户ID
     * @throws StorageException 无权限时抛出
     */
    public void validateBucketPermission(String bucketName, Long userId) throws StorageException {
        if (!checkBucketPermission(bucketName, userId)) {
            throw new StorageException("PERMISSION_DENIED",
                    "Permission denied for bucket: " + bucketName + ", user: " + userId);
        }
    }

    /**
     * 验证用户权限,无权限时抛出异常
     *
     * @param bucketName Bucket名称
     * @param objectKey Object键
     * @param userId 用户ID
     * @throws StorageException 无权限时抛出
     */
    public void validateObjectPermission(String bucketName, String objectKey, Long userId) throws StorageException {
        if (!checkObjectPermission(bucketName, objectKey, userId)) {
            throw new StorageException("PERMISSION_DENIED",
                    "Permission denied for object: " + bucketName + "/" + objectKey + ", user: " + userId);
        }
    }

    /**
     * 检查用户是否为Bucket的所有者
     *
     * @param bucketName Bucket名称
     * @param userId 用户ID
     * @return 是否为所有者
     */
    public boolean isBucketOwner(String bucketName, Long userId) {
        // TODO: 从数据库查询Bucket所有者
        // 临时实现
        return bucketName.startsWith("user-" + userId + "-");
    }

    /**
     * 检查是否为系统Bucket
     * 系统Bucket只有管理员可以操作
     *
     * @param bucketName Bucket名称
     * @return 是否为系统Bucket
     */
    public boolean isSystemBucket(String bucketName) {
        return bucketName.startsWith("system-") ||
               bucketName.startsWith("admin-") ||
               bucketName.equals("public-assets");
    }

    /**
     * 检查用户是否为管理员
     *
     * @param userId 用户ID
     * @return 是否为管理员
     */
    public boolean isAdmin(Long userId) {
        // TODO: 从数据库或缓存查询用户角色
        // 临时实现: 假设user-1为管理员
        return userId != null && userId == 1L;
    }
}
