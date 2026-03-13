package com.duda.file.util;

import com.duda.file.enums.AclType;
import com.duda.file.enums.DataRedundancyType;
import com.duda.file.enums.StorageClass;
import com.duda.file.enums.StorageType;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Bucket验证工具类
 * <p>
 * 用于验证Bucket相关参数的合法性
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
public class BucketValidationUtil {

    /**
     * Bucket名称最小长度
     */
    public static final int MIN_BUCKET_NAME_LENGTH = 3;

    /**
     * Bucket名称最大长度
     */
    public static final int MAX_BUCKET_NAME_LENGTH = 63;

    /**
     * Bucket显示名称最大长度
     */
    public static final int MAX_DISPLAY_NAME_LENGTH = 100;

    /**
     * 地域代码最大长度
     */
    public static final int MAX_REGION_LENGTH = 50;

    /**
     * 标签键最大长度
     */
    public static final int MAX_TAG_KEY_LENGTH = 64;

    /**
     * 标签值最大长度
     */
    public static final int MAX_TAG_VALUE_LENGTH = 128;

    /**
     * 最大标签数量
     */
    public static final int MAX_TAG_COUNT = 50;

    /**
     * Bucket名称正则表达式
     */
    private static final Pattern BUCKET_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]*[a-z0-9]$");

    /**
     * 支持的地域代码
     */
    private static final Set<String> SUPPORTED_REGIONS = Set.of(
        // 阿里云
        "cn-hangzhou", "cn-shanghai", "cn-beijing", "cn-shenzhen", "cn-guangzhou",
        "cn-chengdu", "cn-hongkong", "us-west-1", "us-east-1", "ap-southeast-1",
        // 腾讯云
        "ap-guangzhou", "ap-shanghai", "ap-beijing", "ap-chengdu", "ap-hongkong",
        "ap-singapore", "na-toronto", "eu-frankfurt",
        // 七牛云
        "east-cn", "south-cn", "north-cn", "cloud-cn"
    );

    /**
     * 验证Bucket名称
     *
     * @param bucketName Bucket名称
     * @return 验证结果
     */
    public static ValidationResult validateBucketName(String bucketName) {
        if (bucketName == null || bucketName.isEmpty()) {
            return ValidationResult.fail("Bucket名称不能为空");
        }

        int length = bucketName.length();
        if (length < MIN_BUCKET_NAME_LENGTH) {
            return ValidationResult.fail("Bucket名称长度不能小于" + MIN_BUCKET_NAME_LENGTH + "个字符");
        }

        if (length > MAX_BUCKET_NAME_LENGTH) {
            return ValidationResult.fail("Bucket名称长度不能超过" + MAX_BUCKET_NAME_LENGTH + "个字符");
        }

        if (!BUCKET_NAME_PATTERN.matcher(bucketName).matches()) {
            return ValidationResult.fail("Bucket名称只能包含小写字母、数字和短横线，且必须以字母或数字开头和结尾");
        }

        if (bucketName.contains("--")) {
            return ValidationResult.fail("Bucket名称不能包含连续的短横线");
        }

        if (bucketName.matches("^(\\d{1,3}\\.){3}\\d{1,3}$")) {
            return ValidationResult.fail("Bucket名称不能采用IP地址格式");
        }

        return ValidationResult.success();
    }

    /**
     * 验证Bucket显示名称
     *
     * @param displayName 显示名称
     * @return 验证结果
     */
    public static ValidationResult validateDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return ValidationResult.fail("显示名称不能为空");
        }

        if (displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            return ValidationResult.fail("显示名称长度不能超过" + MAX_DISPLAY_NAME_LENGTH + "个字符");
        }

        return ValidationResult.success();
    }

    /**
     * 验证地域代码
     *
     * @param region 地域代码
     * @return 验证结果
     */
    public static ValidationResult validateRegion(String region) {
        if (region == null || region.isEmpty()) {
            return ValidationResult.fail("地域不能为空");
        }

        if (region.length() > MAX_REGION_LENGTH) {
            return ValidationResult.fail("地域代码长度不能超过" + MAX_REGION_LENGTH + "个字符");
        }

        if (!SUPPORTED_REGIONS.contains(region)) {
            return ValidationResult.fail("不支持的地域代码: " + region);
        }

        return ValidationResult.success();
    }

    /**
     * 验证存储类型
     *
     * @param storageType 存储类型
     * @return 验证结果
     */
    public static ValidationResult validateStorageType(String storageType) {
        if (storageType == null || storageType.isEmpty()) {
            return ValidationResult.fail("存储类型不能为空");
        }

        try {
            StorageType.fromCode(storageType);
            return ValidationResult.success();
        } catch (IllegalArgumentException e) {
            return ValidationResult.fail("不支持的存储类型: " + storageType);
        }
    }

    /**
     * 验证存储类型
     *
     * @param storageClass 存储类型枚举
     * @return 验证结果
     */
    public static ValidationResult validateStorageClass(StorageClass storageClass) {
        if (storageClass == null) {
            return ValidationResult.fail("存储类型不能为空");
        }

        return ValidationResult.success();
    }

    /**
     * 验证数据冗余类型
     *
     * @param dataRedundancyType 数据冗余类型
     * @return 验证结果
     */
    public static ValidationResult validateDataRedundancyType(DataRedundancyType dataRedundancyType) {
        if (dataRedundancyType == null) {
            return ValidationResult.fail("数据冗余类型不能为空");
        }

        return ValidationResult.success();
    }

    /**
     * 验证ACL类型
     *
     * @param aclType ACL类型
     * @return 验证结果
     */
    public static ValidationResult validateAclType(AclType aclType) {
        if (aclType == null) {
            return ValidationResult.fail("ACL类型不能为空");
        }

        return ValidationResult.success();
    }

    /**
     * 验证标签
     *
     * @param tags 标签
     * @return 验证结果
     */
    public static ValidationResult validateTags(java.util.Map<String, String> tags) {
        if (tags == null || tags.isEmpty()) {
            return ValidationResult.success();
        }

        if (tags.size() > MAX_TAG_COUNT) {
            return ValidationResult.fail("标签数量不能超过" + MAX_TAG_COUNT + "个");
        }

        for (String key : tags.keySet()) {
            if (key == null || key.isEmpty()) {
                return ValidationResult.fail("标签键不能为空");
            }

            if (key.length() > MAX_TAG_KEY_LENGTH) {
                return ValidationResult.fail("标签键长度不能超过" + MAX_TAG_KEY_LENGTH + "个字符");
            }

            String value = tags.get(key);
            if (value != null && value.length() > MAX_TAG_VALUE_LENGTH) {
                return ValidationResult.fail("标签值长度不能超过" + MAX_TAG_VALUE_LENGTH + "个字符");
            }
        }

        return ValidationResult.success();
    }

    /**
     * 验证存储配额
     *
     * @param maxSize  最大文件大小（字节）
     * @param maxCount 最大文件数量
     * @return 验证结果
     */
    public static ValidationResult validateQuota(Long maxSize, Integer maxCount) {
        if (maxSize != null && maxSize <= 0) {
            return ValidationResult.fail("最大文件大小必须大于0");
        }

        if (maxSize != null && maxSize > 107374182400L) { // 100GB
            return ValidationResult.fail("最大文件大小不能超过100GB");
        }

        if (maxCount != null && maxCount <= 0) {
            return ValidationResult.fail("最大文件数量必须大于0");
        }

        if (maxCount != null && maxCount > 1000000000) {
            return ValidationResult.fail("最大文件数量不能超过10亿");
        }

        return ValidationResult.success();
    }

    /**
     * 验证用户ID
     *
     * @param userId 用户ID
     * @return 验证结果
     */
    public static ValidationResult validateUserId(Long userId) {
        if (userId == null) {
            return ValidationResult.fail("用户ID不能为空");
        }

        if (userId <= 0) {
            return ValidationResult.fail("用户ID必须大于0");
        }

        return ValidationResult.success();
    }

    /**
     * 验证用户类型
     *
     * @param userType 用户类型
     * @return 验证结果
     */
    public static ValidationResult validateUserType(String userType) {
        if (userType == null || userType.isEmpty()) {
            return ValidationResult.fail("用户类型不能为空");
        }

        return ValidationResult.success();
    }

    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
