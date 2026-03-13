package com.duda.file.manager.support;

import com.duda.file.common.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 对象键验证器
 * 用于验证ObjectKey的合法性
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
@Component
public class ObjectKeyValidator {

    /**
     * 对象键最大长度
     */
    private static final int MAX_KEY_LENGTH = 1024;

    /**
     * 对象键最小长度
     */
    private static final int MIN_KEY_LENGTH = 1;

    /**
     * 非法字符模式
     * 包含控制字符(0x00-0x1F)和删除字符(0x7F)
     */
    private static final Pattern INVALID_CHARS_PATTERN = Pattern.compile(
            "[\\x00-\\x1F\\x7F]"
    );

    /**
     * 验证对象键
     *
     * @param objectKey 对象键
     * @return 是否合法
     */
    public boolean validateObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return false;
        }

        // 检查长度
        if (objectKey.length() < MIN_KEY_LENGTH || objectKey.length() > MAX_KEY_LENGTH) {
            return false;
        }

        // 检查非法字符
        if (INVALID_CHARS_PATTERN.matcher(objectKey).find()) {
            return false;
        }

        // 不能以/开头(OSS不允许)
        if (objectKey.startsWith("/")) {
            return false;
        }

        // 不能以\开头(Windows路径分隔符)
        if (objectKey.startsWith("\\")) {
            return false;
        }

        // 不能包含连续的/
        if (objectKey.contains("//")) {
            return false;
        }

        return true;
    }

    /**
     * 验证对象键,不合法时抛出异常
     *
     * @param objectKey 对象键
     * @throws StorageException 验证失败时抛出
     */
    public void validateObjectKeyOrThrow(String objectKey) throws StorageException {
        if (!validateObjectKey(objectKey)) {
            throw new StorageException("INVALID_OBJECT_KEY", "Invalid object key: " + objectKey);
        }
    }

    /**
     * 批量验证对象键
     *
     * @param objectKeys 对象键列表
     * @return 是否全部合法
     */
    public boolean validateObjectKeys(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return false;
        }

        for (String objectKey : objectKeys) {
            if (!validateObjectKey(objectKey)) {
                log.warn("Invalid object key: {}", objectKey);
                return false;
            }
        }

        return true;
    }

    /**
     * 验证前缀
     *
     * @param prefix 前缀
     * @return 是否合法
     */
    public boolean validatePrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true; // 空前缀是合法的
        }

        // 前缀需要以/结尾(表示目录)
        if (!prefix.endsWith("/")) {
            return false;
        }

        return validateObjectKey(prefix);
    }

    /**
     * 规范化对象键
     * 去除前导和尾随空格,处理多余的斜杠等
     *
     * @param objectKey 原始对象键
     * @return 规范化后的对象键
     */
    public String normalizeObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return objectKey;
        }

        // 去除前导和尾随空格
        String normalized = objectKey.trim();

        // 替换\为/
        normalized = normalized.replace("\\", "/");

        // 去除连续的/
        while (normalized.contains("//")) {
            normalized = normalized.replace("//", "/");
        }

        // 去除尾部的/
        if (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    /**
     * 检查是否为目录键
     * 目录键以/结尾
     *
     * @param objectKey 对象键
     * @return 是否为目录
     */
    public boolean isDirectoryKey(String objectKey) {
        return objectKey != null && objectKey.endsWith("/");
    }

    /**
     * 获取对象的目录部分
     *
     * @param objectKey 对象键
     * @return 目录部分,如果不存在则返回空字符串
     */
    public String getDirectory(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return "";
        }

        int lastSlashIndex = objectKey.lastIndexOf("/");
        if (lastSlashIndex > 0) {
            return objectKey.substring(0, lastSlashIndex);
        }

        return "";
    }

    /**
     * 获取对象的文件名部分
     *
     * @param objectKey 对象键
     * @return 文件名部分
     */
    public String getFileName(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return "";
        }

        int lastSlashIndex = objectKey.lastIndexOf("/");
        if (lastSlashIndex >= 0 && lastSlashIndex < objectKey.length() - 1) {
            return objectKey.substring(lastSlashIndex + 1);
        }

        return objectKey;
    }

    /**
     * 获取文件扩展名
     *
     * @param objectKey 对象键
     * @return 扩展名,不包含点号;如果没有扩展名则返回空字符串
     */
    public String getFileExtension(String objectKey) {
        String fileName = getFileName(objectKey);
        if (fileName.isEmpty()) {
            return "";
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }

        return "";
    }

    /**
     * 构建对象键
     *
     * @param parts 对象键的各个部分
     * @return 拼接后的对象键
     */
    public String buildObjectKey(String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part == null || part.isEmpty()) {
                continue;
            }

            // 规范化每个部分
            part = normalizeObjectKey(part);

            if (builder.length() > 0 && !builder.toString().endsWith("/") && !part.startsWith("/")) {
                builder.append("/");
            }

            builder.append(part);
        }

        return builder.toString();
    }

    /**
     * 检查对象键是否匹配前缀
     *
     * @param objectKey 对象键
     * @param prefix 前缀
     * @return 是否匹配
     */
    public boolean matchPrefix(String objectKey, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }

        if (objectKey == null) {
            return false;
        }

        // 如果前缀不以/结尾,需要确保对象键匹配完整的前缀段
        if (!prefix.endsWith("/")) {
            return objectKey.startsWith(prefix + "/") || objectKey.equals(prefix);
        }

        return objectKey.startsWith(prefix);
    }

    /**
     * 验证文件类型是否允许
     *
     * @param objectKey 对象键
     * @param allowedExtensions 允许的扩展名列表(不包含点号)
     * @return 是否允许
     */
    public boolean isFileTypeAllowed(String objectKey, List<String> allowedExtensions) {
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            return true; // 没有限制
        }

        String extension = getFileExtension(objectKey);
        if (extension.isEmpty()) {
            return true; // 没有扩展名,不限制
        }

        return allowedExtensions.contains(extension.toLowerCase());
    }
}
