package com.duda.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.duda.common.web.exception.BizException;
import com.duda.common.util.BeanCopyUtils;
import com.duda.msg.enums.MsgSendResultEnum;
import com.duda.msg.rpc.ISmsRpc;
import com.duda.user.dto.BindPhoneReqDTO;
import com.duda.user.dto.ReplacePrimaryPhoneReqDTO;
import com.duda.user.dto.SmsLoginReqDTO;
import com.duda.user.dto.SmsSendCodeReqDTO;
import com.duda.user.dto.SmsSendCodeRespDTO;
import com.duda.user.dto.UserDTO;
import com.duda.user.entity.SmsVerificationCode;
import com.duda.user.entity.UserPhoneBinding;
import com.duda.user.enums.SmsSceneEnum;
import com.duda.user.mapper.SmsVerificationCodeMapper;
import com.duda.user.mapper.UserPhoneBindingMapper;
import com.duda.user.po.UserPO;
import com.duda.user.mapper.UserMapper;
import com.duda.user.service.SmsLoginService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 短信登录服务实现
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Service
public class SmsLoginServiceImpl implements SmsLoginService {

    private static final Logger logger = LoggerFactory.getLogger(SmsLoginServiceImpl.class);

    @Resource
    private SmsVerificationCodeMapper smsVerificationCodeMapper;

    @Resource
    private UserPhoneBindingMapper userPhoneBindingMapper;

    @Resource
    private UserMapper userMapper;

    @DubboReference(
        group = "MSG_GROUP",
        version = "1.0.0",
        registry = "msgRegistry"
    )
    private ISmsRpc smsRpc;

    @DubboReference(
        group = "INFRA_GROUP",
        version = "1.0.0",
        registry = "infraRegistry"
    )
    private com.duda.id.api.IdGeneratorRpc idGeneratorRpc;

    /**
     * 验证码过期时间（秒）
     */
    private static final int CODE_EXPIRE_SECONDS = 300;

    /**
     * 每日最大发送次数限制
     */
    private static final int MAX_DAILY_SEND_PER_PHONE = 10;
    private static final int MAX_DAILY_SEND_PER_IP = 20;
    private static final int MAX_DAILY_SEND_PER_DEVICE = 10;
    private static final int MAX_HOURLY_SEND_PER_PHONE = 5;

    @Override
    public SmsSendCodeRespDTO sendCode(SmsSendCodeReqDTO reqDTO) {
        logger.info("发送短信验证码，手机号:{}, 场景:{}", reqDTO.getPhone(), reqDTO.getScene());

        // 1. 检查发送频率限制
        checkSendRateLimit(reqDTO.getPhone(), reqDTO.getIp(), reqDTO.getDeviceId());

        // 2. 调用短信服务发送验证码
        MsgSendResultEnum sendResult = smsRpc.sendLoginCode(reqDTO.getPhone());

        if (sendResult != MsgSendResultEnum.SEND_SUCCESS) {
            logger.error("短信发送失败，手机号:{}, 结果:{}", reqDTO.getPhone(), sendResult);
            return SmsSendCodeRespDTO.builder()
                    .success(false)
                    .message("短信发送失败: " + sendResult.getDesc())
                    .remainingCount(0)
                    .build();
        }

        // 3. 生成验证码（用于记录，实际验证码由短信服务管理）
        String code = "000000"; // 占位符，实际验证码在短信服务中

        // 4. 计算过期时间
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(CODE_EXPIRE_SECONDS);

        // 5. 查找手机号对应的用户（如果是登录场景）
        UserPhoneBinding phoneBinding = userPhoneBindingMapper.findByPhone(reqDTO.getPhone());
        Long userId = phoneBinding != null ? phoneBinding.getUserId() : null;
        Integer userShard = phoneBinding != null ? phoneBinding.getUserShard() : null;

        // 6. 保存验证码记录（用于审计和追踪）
        SmsVerificationCode verificationCode = new SmsVerificationCode();
        verificationCode.setPhone(reqDTO.getPhone());
        verificationCode.setCode(code);
        verificationCode.setScene(reqDTO.getScene());
        verificationCode.setUserId(userId);
        verificationCode.setUserShard(userShard);
        verificationCode.setDeviceId(reqDTO.getDeviceId());
        verificationCode.setDeviceType(reqDTO.getDeviceType());
        verificationCode.setIp(reqDTO.getIp());
        verificationCode.setSendCount(1);
        verificationCode.setStatus(0);
        verificationCode.setExpireTime(expireTime);
        verificationCode.setCreateTime(LocalDateTime.now());
        smsVerificationCodeMapper.insert(verificationCode);

        // 7. 查询剩余发送次数
        Integer todayCount = smsVerificationCodeMapper.countTodayByPhone(reqDTO.getPhone());
        int remainingCount = MAX_DAILY_SEND_PER_PHONE - todayCount - 1;

        logger.info("短信验证码发送成功，手机号:{}, 剩余次数:{}", reqDTO.getPhone(), remainingCount);

        return SmsSendCodeRespDTO.builder()
                .expireSeconds(CODE_EXPIRE_SECONDS)
                .success(true)
                .message("验证码已发送")
                .remainingCount(remainingCount)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO loginBySms(SmsLoginReqDTO reqDTO) {
        logger.info("短信验证码登录，手机号:{}", reqDTO.getPhone());

        // 1. 调用短信服务验证验证码
        com.duda.msg.dto.MsgCheckDTO checkResult = smsRpc.checkLoginCode(
            reqDTO.getPhone(),
            Integer.parseInt(reqDTO.getCode())
        );

        if (!checkResult.isCheckStatus()) {
            throw new BizException(checkResult.getDesc());
        }

        // 2. 查找手机号对应的绑定记录
        UserPhoneBinding phoneBinding = userPhoneBindingMapper.findByPhone(reqDTO.getPhone());

        UserPO userPO;

        if (phoneBinding == null || phoneBinding.getIsActive() == 0) {
            // 3. 手机号未绑定或已更换，自动注册
            logger.info("手机号未绑定，自动注册:{}", reqDTO.getPhone());
            userPO = autoRegisterByPhone(reqDTO.getPhone(), reqDTO.getLoginIp());
        } else {
            // 4. 手机号已绑定，检查用户状态
            userPO = userMapper.selectById(phoneBinding.getUserId());
            if (userPO == null || userPO.getDeleted() == 1) {
                throw new BizException("账户不存在");
            }
            if ("deleted".equals(userPO.getStatus())) {
                throw new BizException("账户已被删除");
            }

            // 5. 更新最后登录时间和IP
            userPO.setLastLoginTime(LocalDateTime.now());
            userPO.setLastLoginIp(reqDTO.getLoginIp());
            userMapper.updateById(userPO);
        }

        // 6. 转换为 DTO 并返回
        return convertToDTO(userPO, phoneBinding);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean bindPhone(BindPhoneReqDTO reqDTO) {
        logger.info("绑定手机号，用户ID:{}, 手机号:{}", reqDTO.getUserId(), reqDTO.getPhone());

        // 1. 验证用户存在
        UserPO userPO = userMapper.selectById(reqDTO.getUserId());
        if (userPO == null || userPO.getDeleted() == 1) {
            throw new BizException("用户不存在");
        }

        // 2. 调用短信服务验证验证码
        com.duda.msg.dto.MsgCheckDTO checkResult = smsRpc.checkLoginCode(
            reqDTO.getPhone(),
            Integer.parseInt(reqDTO.getCode())
        );

        if (!checkResult.isCheckStatus()) {
            throw new BizException(checkResult.getDesc());
        }

        // 3. 检查手机号是否已被其他账户使用
        Integer activeCount = userPhoneBindingMapper.countActiveByPhone(reqDTO.getPhone());
        if (activeCount > 0) {
            throw new BizException("该手机号已被其他账户使用");
        }

        // 4. 计算用户分片
        Integer userShard = calculateShard(reqDTO.getUserId());

        // 5. 检查当前用户是否已经绑定了手机号
        UserPhoneBinding currentBinding = userPhoneBindingMapper.findActiveByUserId(reqDTO.getUserId());

        if (currentBinding != null && reqDTO.getIsPrimary() != null && reqDTO.getIsPrimary()) {
            // 已有手机号，且要绑定为新的主手机号，需要先将旧的设为非激活
            currentBinding.setIsActive(0);
            currentBinding.setReplaceTime(LocalDateTime.now());
            currentBinding.setReplaceReason("更换手机号");
            userPhoneBindingMapper.updateById(currentBinding);
        }

        // 6. 创建新的手机号绑定
        UserPhoneBinding newBinding = new UserPhoneBinding();
        newBinding.setUserId(reqDTO.getUserId());
        newBinding.setUserShard(userShard);
        newBinding.setPhone(reqDTO.getPhone());
        newBinding.setBindType("sms_login");
        newBinding.setBindTime(LocalDateTime.now());
        newBinding.setVerifyCode(reqDTO.getCode());
        newBinding.setVerifyIp(reqDTO.getIp());
        newBinding.setIsActive(reqDTO.getIsPrimary() != null && reqDTO.getIsPrimary() ? 1 :
            (currentBinding == null ? 1 : 0));
        newBinding.setCreateTime(LocalDateTime.now());
        userPhoneBindingMapper.insert(newBinding);

        logger.info("绑定手机号成功: userId={}, phone={}", reqDTO.getUserId(), reqDTO.getPhone());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean replacePrimaryPhone(ReplacePrimaryPhoneReqDTO reqDTO) {
        logger.info("更换主手机号，用户ID:{}, 新手机号:{}", reqDTO.getUserId(), reqDTO.getNewPhone());

        // 1. 验证用户存在
        UserPO userPO = userMapper.selectById(reqDTO.getUserId());
        if (userPO == null || userPO.getDeleted() == 1) {
            throw new BizException("用户不存在");
        }

        // 2. 验证新手机号验证码
        com.duda.msg.dto.MsgCheckDTO newCheckResult = smsRpc.checkLoginCode(
            reqDTO.getNewPhone(),
            Integer.parseInt(reqDTO.getNewCode())
        );

        if (!newCheckResult.isCheckStatus()) {
            throw new BizException("新手机号" + newCheckResult.getDesc());
        }

        // 3. 如果提供了旧手机号验证码，需要验证
        if (reqDTO.getOldPhone() != null && reqDTO.getOldCode() != null) {
            com.duda.msg.dto.MsgCheckDTO oldCheckResult = smsRpc.checkLoginCode(
                reqDTO.getOldPhone(),
                Integer.parseInt(reqDTO.getOldCode())
            );

            if (!oldCheckResult.isCheckStatus()) {
                throw new BizException("旧手机号" + oldCheckResult.getDesc());
            }
        }

        // 4. 检查新手机号是否已被其他账户使用
        Integer activeCount = userPhoneBindingMapper.countActiveByPhone(reqDTO.getNewPhone());
        if (activeCount > 0) {
            throw new BizException("该手机号已被其他账户使用");
        }

        // 5. 计算用户分片
        Integer userShard = calculateShard(reqDTO.getUserId());

        // 6. 更换手机号（将旧手机号设为已更换，创建新的激活记录）
        userPhoneBindingMapper.replacePhone(
            reqDTO.getUserId(),
            userShard,
            reqDTO.getOldPhone(),
            reqDTO.getNewPhone(),
            reqDTO.getNewCode(),
            reqDTO.getIp(),
            reqDTO.getReason()
        );

        logger.info("更换主手机号成功: userId={}, newPhone={}", reqDTO.getUserId(), reqDTO.getNewPhone());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unbindPhone(Long userId, String phone) {
        logger.info("解绑手机号，用户ID:{}, 手机号:{}", userId, phone);

        // 1. 查找用户的绑定记录
        UserPhoneBinding phoneBinding = userPhoneBindingMapper.findActiveByUserId(userId);
        if (phoneBinding == null) {
            throw new BizException("未找到绑定的手机号");
        }

        // 2. 检查是否为唯一手机号
        LambdaQueryWrapper<UserPhoneBinding> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPhoneBinding::getUserId, userId)
                .eq(UserPhoneBinding::getIsActive, 1);
        long count = userPhoneBindingMapper.selectCount(queryWrapper);

        if (count <= 1) {
            throw new BizException("这是您唯一的手机号，无法解绑");
        }

        // 3. 将手机号设为已更换
        phoneBinding.setIsActive(0);
        phoneBinding.setReplaceTime(LocalDateTime.now());
        phoneBinding.setReplaceReason("用户主动解绑");
        userPhoneBindingMapper.updateById(phoneBinding);

        logger.info("解绑手机号成功: userId={}", userId);
        return true;
    }

    @Override
    public Boolean verifyCode(String phone, String code, String scene) {
        logger.debug("验证短信验证码，手机号:{}, 场景:{}", phone, scene);

        try {
            com.duda.msg.dto.MsgCheckDTO checkResult = smsRpc.checkLoginCode(
                phone,
                Integer.parseInt(code)
            );
            return checkResult.isCheckStatus();
        } catch (Exception e) {
            logger.error("验证码验证失败", e);
            return false;
        }
    }

    @Override
    public void markCodeAsUsed(String phone, String code, String scene) {
        logger.debug("标记验证码已使用，手机号:{}, 场景:{}", phone, scene);
        // 短信服务在验证成功后会自动删除验证码，无需额外操作
    }

    /**
     * 检查发送频率限制
     */
    private void checkSendRateLimit(String phone, String ip, String deviceId) {
        // 检查今日发送次数（按手机号）
        Integer todayPhoneCount = smsVerificationCodeMapper.countTodayByPhone(phone);
        if (todayPhoneCount >= MAX_DAILY_SEND_PER_PHONE) {
            throw new BizException("今日发送次数已达上限");
        }

        // 检查今日发送次数（按IP）
        Integer todayIpCount = smsVerificationCodeMapper.countTodayByIp(ip);
        if (todayIpCount >= MAX_DAILY_SEND_PER_IP) {
            throw new BizException("今日发送次数已达上限");
        }

        // 检查今日发送次数（按设备）
        if (deviceId != null && !deviceId.isEmpty()) {
            Integer todayDeviceCount = smsVerificationCodeMapper.countTodayByDevice(deviceId);
            if (todayDeviceCount >= MAX_DAILY_SEND_PER_DEVICE) {
                throw new BizException("今日发送次数已达上限");
            }
        }

        // 检查最近1小时发送次数（按手机号）
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        LocalDateTime now = LocalDateTime.now();
        Integer hourlyPhoneCount = smsVerificationCodeMapper.countByPhoneAndTimeRange(phone, oneHourAgo, now);
        if (hourlyPhoneCount >= MAX_HOURLY_SEND_PER_PHONE) {
            throw new BizException("发送频率过高，请稍后再试");
        }
    }

    /**
     * 通过手机号自动注册用户
     */
    private UserPO autoRegisterByPhone(String phone, String registerIp) {
        // 1. 生成用户ID
        Long userId = idGeneratorRpc.generateUserId();
        Integer userShard = calculateShard(userId);

        // 2. 创建用户
        UserPO userPO = new UserPO();
        userPO.setId(userId);
        userPO.setUsername(phone); // 暂时用手机号作为用户名
        userPO.setUserType("individual");
        userPO.setStatus("active");
        userPO.setDeleted(0);
        userPO.setPassword(""); // 短信登录用户没有密码，设置为空字符串
        userPO.setCreateTime(LocalDateTime.now());
        userPO.setLastLoginTime(LocalDateTime.now());
        userPO.setLastLoginIp(registerIp);
        userMapper.insert(userPO);

        // 3. 创建手机号绑定
        UserPhoneBinding phoneBinding = new UserPhoneBinding();
        phoneBinding.setUserId(userId);
        phoneBinding.setUserShard(userShard);
        phoneBinding.setPhone(phone);
        phoneBinding.setBindType("sms_login");
        phoneBinding.setBindTime(LocalDateTime.now());
        phoneBinding.setVerifyIp(registerIp);
        phoneBinding.setIsActive(1);
        phoneBinding.setCreateTime(LocalDateTime.now());
        userPhoneBindingMapper.insert(phoneBinding);

        logger.info("手机号自动注册成功: userId={}, phone={}", userId, phone);
        return userPO;
    }

    /**
     * 计算用户分片ID
     */
    private Integer calculateShard(Long userId) {
        return (int) (userId % 100);
    }

    /**
     * 转换为 UserDTO
     */
    private UserDTO convertToDTO(UserPO userPO, UserPhoneBinding phoneBinding) {
        UserDTO userDTO = BeanCopyUtils.copy(userPO, UserDTO.class);
        if (phoneBinding != null) {
            userDTO.setPhone(phoneBinding.getPhone());
        }
        return userDTO;
    }
}
