package com.duda.file.util;

import com.duda.file.enums.StorageType;
import java.util.UUID;

/**
 * Bucket名称生成器
 * <p>
 * 规则（符合阿里云OSS命名规范）：
 * - 全局唯一
 * - 只能包含小写字母、数字、短横线
 * - 长度3-63字符
 * - 必须以字母或数字开头和结尾
 * - 短横线不能连续出现
 * <p>
 * 命名策略：
 * - 格式：{prefix}-{userType}-{userId}-{category}-{timestamp}
 * - 示例：duda-platform-123456-avatar-20260313103045
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
public class BucketNameGenerator {

    /**
     * 平台默认前缀
     */
    private static final String DEFAULT_PREFIX = "duda";

    /**
     * 分隔符
     */
    private static final String SEPARATOR = "-";

    /**
     * Bucket名称最大长度
     */
    private static final int MAX_BUCKET_NAME_LENGTH = 63;

    /**
     * Bucket名称最小长度
     */
    private static final int MIN_BUCKET_NAME_LENGTH = 3;

    /**
     * 生成Bucket名称
     *
     * @param userId    用户ID
     * @param userType  用户类型（platform, enterprise, personal等）
     * @param category  分类（avatar, document, video, image等）
     * @param prefix    前缀（可选，默认为duda）
     * @return Bucket名称
     */
    public static String generateBucketName(Long userId, String userType, String category, String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = DEFAULT_PREFIX;
        }

        // 格式：{prefix}-{userType}-{userId}-{category}-{timestamp}
        String timestamp = String.valueOf(System.currentTimeMillis());
        String bucketName = String.format("%s%s%s%s%d%s%s%s",
            prefix.toLowerCase(), SEPARATOR,
            userType.toLowerCase(), SEPARATOR,
            userId, SEPARATOR,
            category.toLowerCase(), SEPARATOR,
            timestamp
        );

        // 如果超过长度限制，使用UUID缩短
        if (bucketName.length() > MAX_BUCKET_NAME_LENGTH) {
            bucketName = generateShortBucketName(prefix, userType, userId, category);
        }

        // 验证生成的名称
        if (!validateBucketName(bucketName)) {
            throw new IllegalArgumentException("生成的Bucket名称不符合规范: " + bucketName);
        }

        return bucketName;
    }

    /**
     * 生成Bucket名称（使用默认前缀）
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @param category 分类
     * @return Bucket名称
     */
    public static String generateBucketName(Long userId, String userType, String category) {
        return generateBucketName(userId, userType, category, DEFAULT_PREFIX);
    }

    /**
     * 生成短Bucket名称（使用UUID）
     *
     * @param prefix   前缀
     * @param userType 用户类型
     * @param userId   用户ID
     * @param category 分类
     * @return Bucket名称
     */
    private static String generateShortBucketName(String prefix, String userType, Long userId, String category) {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return String.format("%s%s%s%s%d%s%s%s%s",
            prefix.toLowerCase(), SEPARATOR,
            userType.toLowerCase(), SEPARATOR,
            userId, SEPARATOR,
            category.toLowerCase(), SEPARATOR,
            uuid
        );
    }

    /**
     * 验证Bucket名称是否合法
     *
     * @param bucketName Bucket名称
     * @return 是否合法
     */
    public static boolean validateBucketName(String bucketName) {
        if (bucketName == null || bucketName.isEmpty()) {
            return false;
        }

        // 检查长度
        int length = bucketName.length();
        if (length < MIN_BUCKET_NAME_LENGTH || length > MAX_BUCKET_NAME_LENGTH) {
            return false;
        }

        // 检查字符：只能包含小写字母、数字、短横线
        if (!bucketName.matches("^[a-z0-9-]+$")) {
            return false;
        }

        // 检查开头：必须以字母或数字开头
        if (!Character.isLetterOrDigit(bucketName.charAt(0))) {
            return false;
        }

        // 检查结尾：必须以字母或数字结尾
        if (!Character.isLetterOrDigit(bucketName.charAt(length - 1))) {
            return false;
        }

        // 检查短横线不能连续出现
        if (bucketName.contains("--")) {
            return false;
        }

        // 检查不能是IP地址格式
        if (bucketName.matches("^(\\d{1,3}\\.){3}\\d{1,3}$")) {
            return false;
        }

        return true;
    }

    /**
     * 根据存储类型优化Bucket名称
     * <p>
     * 不同云存储平台可能有特殊要求，这里做适配
     *
     * @param bucketName  原始Bucket名称
     * @param storageType 存储类型
     * @return 优化后的Bucket名称
     */
    public static String optimizeForStorageType(String bucketName, StorageType storageType) {
        if (bucketName == null || !validateBucketName(bucketName)) {
            throw new IllegalArgumentException("Bucket名称不合法");
        }

        // 目前所有存储类型都遵循相同规则
        // 如果未来有特殊要求，在这里处理
        return bucketName;
    }

    /**
     * 生成临时测试Bucket名称
     *
     * @return 测试Bucket名称
     */
    public static String generateTestBucketName() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return "test-" + uuid;
    }
}
