package com.duda.file.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * API密钥加密解密工具类
 * <p>
 * 用于在数据库中存储用户的API密钥（AccessKey、SecretKey等）
 * 使用AES-256-GCM加密算法，提供 authenticated encryption
 * <p>
 * 安全说明：
 * - 加密密钥应从配置中心（Nacos）获取，不应硬编码
 * - 每次加密都会生成新的IV（初始化向量），防止相同的明文产生相同的密文
 * - GCM模式提供完整性校验，防止密文被篡改
 * - 密文格式：IV(12字节) + 密文 + 认证标签(16字节)
 *
 * @author DudaNexus
 * @since 2026-03-13
 */
public class ApiKeyEncryptionUtil {

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    /**
     * 密钥长度（位）
     */
    private static final int KEY_SIZE = 256;

    /**
     * IV长度（字节）
     */
    private static final int IV_LENGTH = 12;

    /**
     * GCM认证标签长度（位）
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * 加密密钥（应从Nacos配置中心获取）
     * 注意：这是示例密钥，实际使用时应从配置中心读取
     */
    private static volatile String encryptionKey;

    /**
     * 设置加密密钥（从配置中心初始化）
     *
     * @param key 加密密钥（Base64编码的32字节密钥）
     */
    public static void setEncryptionKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("加密密钥不能为空");
        }
        encryptionKey = key;
    }

    /**
     * 获取加密密钥
     *
     * @return 加密密钥
     */
    private static String getEncryptionKey() {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            throw new IllegalStateException("加密密钥未初始化，请先调用setEncryptionKey()方法");
        }
        return encryptionKey;
    }

    /**
     * 加密API密钥
     *
     * @param plaintext 明文
     * @return Base64编码的密文
     * @throws Exception 加密失败
     */
    public static String encrypt(String plaintext) throws Exception {
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("明文不能为空");
        }

        // 解码密钥
        byte[] keyBytes = Base64.getDecoder().decode(getEncryptionKey());
        if (keyBytes.length != KEY_SIZE / 8) {
            throw new IllegalArgumentException("加密密钥长度必须是" + (KEY_SIZE / 8) + "字节");
        }

        // 生成随机IV
        byte[] iv = new byte[IV_LENGTH];
        java.security.SecureRandom.getInstanceStrong().nextBytes(iv);

        // 创建密钥和密码器
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        // 加密
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // 组合：IV + 密文
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertext);
        byte[] encryptedData = byteBuffer.array();

        // 返回Base64编码
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * 解密API密钥
     *
     * @param encryptedText Base64编码的密文
     * @return 明文
     * @throws Exception 解密失败
     */
    public static String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new IllegalArgumentException("密文不能为空");
        }

        // 解码密文
        byte[] encryptedData = Base64.getDecoder().decode(encryptedText);

        // 检查长度
        if (encryptedData.length < IV_LENGTH) {
            throw new IllegalArgumentException("密文格式错误");
        }

        // 提取IV和密文
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
        byte[] iv = new byte[IV_LENGTH];
        byteBuffer.get(iv);
        byte[] ciphertext = new byte[byteBuffer.remaining()];
        byteBuffer.get(ciphertext);

        // 解码密钥
        byte[] keyBytes = Base64.getDecoder().decode(getEncryptionKey());
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // 创建密码器并解密
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] plaintext = cipher.doFinal(ciphertext);

        return new String(plaintext, StandardCharsets.UTF_8);
    }

    /**
     * 验证密文是否有效
     *
     * @param encryptedText Base64编码的密文
     * @return 是否有效
     */
    public static boolean isValidEncryptedText(String encryptedText) {
        try {
            decrypt(encryptedText);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成随机加密密钥
     * <p>
     * 用于系统初始化时生成新的加密密钥
     *
     * @return Base64编码的32字节密钥
     */
    public static String generateEncryptionKey() {
        byte[] key = new byte[KEY_SIZE / 8];
        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * 隐藏敏感信息（用于日志输出）
     *
     * @param apiKey API密钥
     * @return 隐藏后的字符串（只显示前4位和后4位）
     */
    public static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
