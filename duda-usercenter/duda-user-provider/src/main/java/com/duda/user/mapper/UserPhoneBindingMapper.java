package com.duda.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.UserPhoneBinding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户手机号绑定Mapper
 *
 * @author DudaNexus
 * @since 2026-03-21
 */
@Mapper
public interface UserPhoneBindingMapper extends BaseMapper<UserPhoneBinding> {

    /**
     * 查找用户的激活手机号绑定
     *
     * @param userId 用户ID
     * @return 手机号绑定记录
     */
    UserPhoneBinding findActiveByUserId(@Param("userId") Long userId);

    /**
     * 根据手机号查找绑定记录
     *
     * @param phone 手机号
     * @return 绑定记录
     */
    UserPhoneBinding findByPhone(@Param("phone") String phone);

    /**
     * 检查手机号是否已被绑定（包括已更换的历史记录）
     *
     * @param phone 手机号
     * @return 绑定数量
     */
    Integer countByPhone(@Param("phone") String phone);

    /**
     * 检查激活的手机号是否已被绑定
     *
     * @param phone 手机号
     * @return 激活的绑定数量
     */
    Integer countActiveByPhone(@Param("phone") String phone);

    /**
     * 更换手机号（将旧手机号设为已更换，创建新的激活记录）
     *
     * @param userId 用户ID
     * @param userShard 用户分片ID
     * @param oldPhone 旧手机号
     * @param newPhone 新手机号
     * @param verifyCode 验证码
     * @param verifyIp 验证IP
     * @param replaceReason 更换原因
     * @return 影响行数
     */
    int replacePhone(@Param("userId") Long userId,
                     @Param("userShard") Integer userShard,
                     @Param("oldPhone") String oldPhone,
                     @Param("newPhone") String newPhone,
                     @Param("verifyCode") String verifyCode,
                     @Param("verifyIp") String verifyIp,
                     @Param("replaceReason") String replaceReason);

    /**
     * 将用户的所有手机号设为非激活（排除指定的手机号）
     *
     * @param userId 用户ID
     * @param excludePhone 排除的手机号
     * @return 影响行数
     */
    int deactivateOtherPhones(@Param("userId") Long userId, @Param("excludePhone") String excludePhone);
}
