package com.duda.msg.provider.service.impl;

import com.duda.msg.dto.MsgCheckDTO;
import com.duda.msg.enums.MsgSendResultEnum;
import com.duda.msg.provider.config.SmsConfig;
import com.duda.msg.provider.service.ISmsService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现类
 *
 * 核心功能：
 * 1. 发送验证码（支持开发/生产环境插拔式切换）
 * 2. 校验验证码
 * 3. 防重发机制（60秒限制）
 * 4. Redis缓存管理
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Service
public class SmsServiceImpl implements ISmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SmsConfig smsConfig;

    private static final String SMS_CODE_PREFIX = "duda:sms:code:";

    /**
     * 发送短信验证码
     *
     * 流程：
     * 1. 校验手机号
     * 2. 检查防重发（60秒内不能重复发送）
     * 3. 生成随机验证码
     * 4. 存储到Redis（60秒过期）
     * 5. 根据环境决定是否真实发送
     */
    @Override
    public MsgSendResultEnum sendLoginCode(String phone) {
        logger.info("【sendLoginCode】开始发送验证码，phone={}", phone);

        try {
            // 1. 校验手机号
            if (StringUtils.isEmpty(phone) || phone.length() != 11) {
                logger.warn("【sendLoginCode】手机号格式错误，phone={}", phone);
                return MsgSendResultEnum.MSG_PARAM_ERROR;
            }

            // 2. 构建Redis缓存Key
            String codeCacheKey = SMS_CODE_PREFIX + phone;
            logger.info("【sendLoginCode】Redis key={}", codeCacheKey);

            // 3. 检查是否已发送（防重发）
            Boolean hasCache = redisTemplate.hasKey(codeCacheKey);
            if (Boolean.TRUE.equals(hasCache)) {
                logger.warn("【sendLoginCode】发送过于频繁，phone={}", phone);
                return MsgSendResultEnum.MSG_PARAM_ERROR;
            }

            // 4. 生成验证码
            int codeLength = smsConfig.getCodeLength();
            int code = generateCode(codeLength);
            logger.info("【sendLoginCode】生成验证码，phone={}, code={}", phone, code);

            // 5. 存储到Redis
            int expireSeconds = smsConfig.getCodeExpireSeconds();
            redisTemplate.opsForValue().set(codeCacheKey, code, expireSeconds, TimeUnit.SECONDS);
            logger.info("【sendLoginCode】验证码已存储到Redis，过期时间={}秒", expireSeconds);

            // 6. 根据环境决定是否真实发送
            if (smsConfig.isRealSend()) {
                // 生产环境：调用真实短信服务SDK
                logger.info("【sendLoginCode】生产环境，调用真实短信服务，phone={}, code={}", phone, code);
                // TODO: 集成真实的短信服务SDK（阿里云/腾讯云/容联云等）
                // sendSmsByRealSDK(phone, code);
            } else {
                // 开发环境：只打印日志，不真实发送（省钱）
                logger.info("【sendLoginCode】开发环境，验证码打印到日志，phone={}, code={}", phone, code);
                logger.info("========================================");
                logger.info("【验证码】手机号：{}，验证码：{}", phone, code);
                logger.info("========================================");
            }

            logger.info("【sendLoginCode】发送成功，phone={}", phone);
            return MsgSendResultEnum.SEND_SUCCESS;

        } catch (Exception e) {
            logger.error("【sendLoginCode】发送失败，phone={}", phone, e);
            return MsgSendResultEnum.SEND_FAIL;
        }
    }

    /**
     * 校验登录验证码
     *
     * 流程：
     * 1. 校验参数
     * 2. 从Redis获取验证码
     * 3. 验证成功后删除（防止重复使用）
     */
    @Override
    public MsgCheckDTO checkLoginCode(String phone, Integer code) {
        logger.info("【checkLoginCode】开始校验验证码，phone={}, code={}", phone, code);

        try {
            // 1. 参数校验
            if (StringUtils.isEmpty(phone) || code == null) {
                logger.warn("【checkLoginCode】参数异常，phone={}, code={}", phone, code);
                return new MsgCheckDTO(false, "参数异常");
            }

            // 2. 从Redis获取验证码
            String codeCacheKey = SMS_CODE_PREFIX + phone;
            Object redisCode = redisTemplate.opsForValue().get(codeCacheKey);

            if (redisCode == null) {
                logger.warn("【checkLoginCode】验证码已失效，phone={}", phone);
                return new MsgCheckDTO(false, "验证码已失效");
            }

            // 3. 验证码比对
            int storedCode = Integer.parseInt(redisCode.toString());
            if (storedCode == code) {
                // 验证成功，删除Redis中的验证码
                redisTemplate.delete(codeCacheKey);
                logger.info("【checkLoginCode】验证成功，phone={}", phone);
                return new MsgCheckDTO(true, "验证码校验成功");
            }

            logger.warn("【checkLoginCode】验证码错误，phone={}, expected={}, actual={}",
                phone, storedCode, code);
            return new MsgCheckDTO(false, "验证码错误");

        } catch (Exception e) {
            logger.error("【checkLoginCode】校验异常，phone={}, code={}", phone, code, e);
            return new MsgCheckDTO(false, "校验异常");
        }
    }

    /**
     * 生成指定长度的随机数字验证码
     *
     * @param length 验证码长度
     * @return 验证码
     */
    private int generateCode(int length) {
        Random random = new Random();
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        return min + random.nextInt(max - min + 1);
    }
}
