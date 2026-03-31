package com.duda.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.SmsVerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 短信验证码Mapper
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Mapper
public interface SmsVerificationCodeMapper extends BaseMapper<SmsVerificationCode> {

    /**
     * 查找最新的未使用验证码
     *
     * @param phone 手机号
     * @param scene 场景
     * @return 验证码记录
     */
    SmsVerificationCode findLatestUnused(@Param("phone") String phone,
                                          @Param("scene") String scene);

    /**
     * 根据手机号、验证码、场景查找验证码记录
     *
     * @param phone 手机号
     * @param code 验证码
     * @param scene 场景
     * @return 验证码记录
     */
    SmsVerificationCode findByPhoneAndCode(@Param("phone") String phone,
                                            @Param("code") String code,
                                            @Param("scene") String scene);

    /**
     * 统计今日发送次数（按手机号）
     *
     * @param phone 手机号
     * @return 发送次数
     */
    Integer countTodayByPhone(@Param("phone") String phone);

    /**
     * 统计今日发送次数（按IP）
     *
     * @param ip IP地址
     * @return 发送次数
     */
    Integer countTodayByIp(@Param("ip") String ip);

    /**
     * 统计今日发送次数（按设备）
     *
     * @param deviceId 设备ID
     * @return 发送次数
     */
    Integer countTodayByDevice(@Param("deviceId") String deviceId);

    /**
     * 统计指定时间段内发送次数（按手机号）
     *
     * @param phone 手机号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 发送次数
     */
    Integer countByPhoneAndTimeRange(@Param("phone") String phone,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 删除过期验证码
     *
     * @param expireTime 过期时间
     * @return 删除数量
     */
    int deleteExpired(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 更新验证码验证结果
     *
     * @param id 验证码ID
     * @param verifyResult 验证结果
     * @param failReason 失败原因
     * @return 影响行数
     */
    int updateVerifyResult(@Param("id") Long id,
                           @Param("verifyResult") String verifyResult,
                           @Param("failReason") String failReason);
}
