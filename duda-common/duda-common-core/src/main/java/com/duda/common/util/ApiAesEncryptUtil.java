package com.duda.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * API密钥AES加密解密工具类（公共模块）
 * 使用 Nacos 公共配置中的密钥进行 AES-GCM 加密/解密
 *
 * 配置来源：common-encryption.yml (Nacos)
 * encryption.aes.key: IllvgnDNuazK972Ly+WG7ftWJj9r2AchDY5bjRhhTek=
 *
 * 使用场景：
 * - user-service: 加密用户输入的明文密钥后存储
 * - file-service: 从数据库读取密文后解密使用
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@Component
public class ApiAesEncryptUtil {

    /**
     * AES密钥（从 Nacos 公共配置读取）
     * Base64编码的32字节密钥（AES-256）
     *
     * 默认值仅用于开发环境，生产环境必须从 Nacos 配置
     */
    @Value("${encryption.aes.key:IllvgnDNuazK972Ly+WG7ftWJj9r2AchDY5bjRhhTek=}")
    private String aesKeyStr;

    /**
     * GCM认证标签长度（位）
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * GCM初始化向量长度（字节）
     */
    private static final int GCM_IV_LENGTH = 12;

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加密模式和填充
     */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * 加密字符串
     *
     * @param plaintext 明文（如 AccessKey ID/Secret）
     * @return Base64编码的密文
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            // 1. 获取密钥
            byte[] keyBytes = getKeyBytes();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            // 2. 生成随机IV（初始化向量）
            byte[] iv = generateIV();

            // 3. 创建加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            // 4. 加密
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // 5. 组合：IV + 密文
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            byte[] combined = byteBuffer.array();

            // 6. Base64编码
            String encrypted = Base64.getEncoder().encodeToString(combined);

            log.info("✅ API密钥加密成功");
            log.info("  明文长度: {} 字符", plaintext.length());
            log.info("  明文（前8字符）: {}***", plaintext.substring(0, Math.min(8, plaintext.length())));
            log.info("  密文长度: {} 字符", encrypted.length());

            return encrypted;

        } catch (Exception e) {
            log.error("❌ API密钥加密失败: {}", e.getMessage(), e);
            throw new RuntimeException("API密钥加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解密字符串
     *
     * @param ciphertext Base64编码的密文
     * @return 明文
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isEmpty()) {
            return ciphertext;
        }

        log.info("========================================");
        log.info("开始解密API密钥...");
        log.info("加密密文长度: {} 字符", ciphertext.length());
        log.info("加密密文（前50字符）: {}...", ciphertext.substring(0, Math.min(50, ciphertext.length())));

        try {
            // 1. Base64解码
            byte[] combined = Base64.getDecoder().decode(ciphertext);

            // 2. 提取IV和密文
            ByteBuffer byteBuffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encrypted = new byte[byteBuffer.remaining()];
            byteBuffer.get(encrypted);

            // 3. 获取密钥
            byte[] keyBytes = getKeyBytes();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            // 4. 创建解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            // 5. 解密
            byte[] plaintext = cipher.doFinal(encrypted);

            // 6. 转换为字符串
            String decrypted = new String(plaintext, StandardCharsets.UTF_8);

            log.info("✅ 成功解密API密钥");
            log.info("解密后密钥长度: {} 字符", decrypted.length());
            log.info("解密后密钥（完整）: {}", decrypted);
            log.info("解密后密钥（前8字符）: {}***", decrypted.substring(0, Math.min(8, decrypted.length())));
            log.info("解密后密钥（后8字符）: ***{}", decrypted.substring(Math.max(0, decrypted.length() - 8)));
            log.info("========================================");

            return decrypted;

        } catch (Exception e) {
            log.error("❌ 解密API密钥失败: {}", e.getMessage(), e);
            log.error("异常类型: {}", e.getClass().getName());
            log.info("========================================");
            throw new RuntimeException("API密钥解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取密钥字节数组
     * 确保密钥长度为32字节（AES-256）
     *
     * 配置的密钥格式：Base64编码的字符串
     * 例如：IllvgnDNuazK972Ly+WG7ftWJj9r2AchDY5bjRhhTek=
     */
    private byte[] getKeyBytes() {
        byte[] keyBytes;

        // 配置的密钥是 Base64 编码的，先解码
        try {
            keyBytes = Base64.getDecoder().decode(aesKeyStr);
            log.debug("✅ AES密钥已Base64解码，长度: {} 字节", keyBytes.length);
        } catch (IllegalArgumentException e) {
            log.error("❌ AES密钥Base64解码失败: {}", e.getMessage());
            throw new RuntimeException("AES密钥配置错误，必须是Base64编码的字符串", e);
        }

        // 验证密钥长度
        if (keyBytes.length != 32) {
            log.error("❌ AES密钥长度错误: {} 字节，期望 32 字节（AES-256）", keyBytes.length);
            throw new RuntimeException("AES密钥长度必须为32字节（AES-256）");
        }

        return keyBytes;
    }

    /**
     * 生成随机IV（初始化向量）
     * GCM模式推荐12字节IV
     */
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        // 使用安全随机数生成器
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
    }
}
