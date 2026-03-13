package com.duda.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.UserTypeRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户身份关联Mapper
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Mapper
public interface UserTypeRoleMapper extends BaseMapper<UserTypeRole> {

    /**
     * 根据用户ID查询用户拥有的所有身份
     *
     * @param userId 用户ID
     * @return 身份列表
     */
    @Select("SELECT * FROM user_type_roles WHERE user_id = #{userId} AND deleted = 0")
    List<UserTypeRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和身份类型查询
     *
     * @param userId 用户ID
     * @param userType 身份类型
     * @return 身份信息
     */
    @Select("SELECT * FROM user_type_roles WHERE user_id = #{userId} AND user_type = #{userType} AND deleted = 0")
    UserTypeRole selectByUserIdAndType(@Param("userId") Long userId, @Param("userType") String userType);

    /**
     * 检查用户是否拥有指定身份
     *
     * @param userId 用户ID
     * @param userType 身份类型
     * @return 是否拥有
     */
    @Select("SELECT COUNT(*) > 0 FROM user_type_roles WHERE user_id = #{userId} AND user_type = #{userType} AND deleted = 0 AND status = 'active'")
    boolean hasUserRole(@Param("userId") Long userId, @Param("userType") String userType);
}
