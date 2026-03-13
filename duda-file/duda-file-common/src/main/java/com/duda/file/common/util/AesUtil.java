package com.duda.file.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES加密解密工具类
 * 用于API密钥的加密存储和解密使用
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
public class AesUtil {

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加密模式和填充方式
     * AES/CBC/PKCS5Padding
     */
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    /**
     * 默认向量IV (16字节)
     * 实际使用时应该从配置中心读取
     */
    private static final String DEFAULT_IV = "1234567890123456";

    /**
     * AES加密
     *
     * @param plainText 明文
     * @param secretKey 密钥(必须是16、24或32字节)
     * @return Base64编码的密文
     */
    public static String encrypt(String plainText, String secretKey) {
        if (!StringUtils.isNotBlank(plainText) || !StringUtils.isNotBlank(secretKey)) {
            return plainText;
        }

        try {
            // 密钥长度必须是16、24或32字节
            byte[] keyBytes = padKey(secretKey.getBytes(StandardCharsets.UTF_8));

            // 创建密钥规格
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            // 创建向量IV
            IvParameterSpec ivSpec = new IvParameterSpec(DEFAULT_IV.getBytes(StandardCharsets.UTF_8));

            // 创建加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // 加密
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Base64编码
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("AES加密失败: " + e.getMessage(), e);
        }
    }

    /**
     * AES解密
     *
     * @param cipherText Base64编码的密文
     * @param secretKey 密钥(必须是16、24或32字节)
     * @return 明文
     */
    public static String decrypt(String cipherText, String secretKey) {
        if (!StringUtils.isNotBlank(cipherText) || !StringUtils.isNotBlank(secretKey)) {
            return cipherText;
        }

        try {
            // 密钥长度必须是16、24或32字节
            byte[] keyBytes = padKey(secretKey.getBytes(StandardCharsets.UTF_8));

            // 创建密钥规格
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            // 创建向量IV
            IvParameterSpec ivSpec = new IvParameterSpec(DEFAULT_IV.getBytes(StandardCharsets.UTF_8));

            // 创建解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // Base64解码
            byte[] encrypted = Base64.getDecoder().decode(cipherText);

            // 解密
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("AES解密失败: " + e.getMessage(), e);
        }
    }

    /**
     * 填充或截断密钥到16字节(AES-128)
     *
     * @param key 原始密钥
     * @return 填充后的密钥
     */
    private static byte[] padKey(byte[] key) {
        byte[] result = new byte[16];

        if (key.length >= 16) {
            System.arraycopy(key, 0, result, 0, 16);
        } else {
            System.arraycopy(key, 0, result, 0, key.length);
            // 剩余部分填充0
            for (int i = key.length; i < 16; i++) {
                result[i] = 0;
            }
        }

        return result;
    }

    /**
     * 生成随机密钥
     *
     * @return Base64编码的随机密钥
     */
    public static String generateKey() {
        try {
            javax.crypto.KeyGenerator keyGenerator = javax.crypto.KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(128); // AES-128
            byte[] key = keyGenerator.generateKey().getEncoded();
            return Base64.getEncoder().encodeToString(key);
        } catch (Exception e) {
            log.error("生成AES密钥失败", e);
            throw new RuntimeException("生成AES密钥失败: " + e.getMessage(), e);
        }
    }
}
