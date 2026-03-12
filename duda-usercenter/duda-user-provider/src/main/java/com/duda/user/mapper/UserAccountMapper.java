package com.duda.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户登录账号Mapper
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    /**
     * 根据登录方式和登录账号查询
     *
     * @param loginType 登录方式
     * @param loginAccount 登录账号
     * @return 登录账号信息
     */
    @Select("SELECT * FROM user_accounts WHERE login_type = #{loginType} AND login_account = #{loginAccount} AND status = 'active' LIMIT 1")
    UserAccount selectByLoginTypeAndAccount(@Param("loginType") String loginType, @Param("loginAccount") String loginAccount);

    /**
     * 根据用户ID查询所有登录账号
     *
     * @param userId 用户ID
     * @return 登录账号列表
     */
    @Select("SELECT * FROM user_accounts WHERE user_id = #{userId} AND status = 'active' ORDER BY is_primary DESC, create_time ASC")
    java.util.List<UserAccount> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和登录方式查询
     *
     * @param userId 用户ID
     * @param loginType 登录方式
     * @return 登录账号信息
     */
    @Select("SELECT * FROM user_accounts WHERE user_id = #{userId} AND login_type = #{loginType} AND status = 'active' LIMIT 1")
    UserAccount selectByUserIdAndLoginType(@Param("userId") Long userId, @Param("loginType") String loginType);

    /**
     * 检查登录账号是否已存在
     *
     * @param loginType 登录方式
     * @param loginAccount 登录账号
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM user_accounts WHERE login_type = #{loginType} AND login_account = #{loginAccount}")
    boolean existsByLoginTypeAndAccount(@Param("loginType") String loginType, @Param("loginAccount") String loginAccount);
}
