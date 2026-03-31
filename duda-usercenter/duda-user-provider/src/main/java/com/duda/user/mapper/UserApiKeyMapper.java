package com.duda.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.duda.user.entity.UserApiKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户API密钥Mapper接口
 * 继承 MyBatis-Plus BaseMapper，获得基础CRUD方法
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Mapper
public interface UserApiKeyMapper extends BaseMapper<UserApiKey> {

    /**
     * 根据用户ID查询所有API密钥（未删除）
     *
     * @param userId 用户ID
     * @return API密钥列表
     */
    List<UserApiKey> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询默认API密钥
     *
     * @param userId 用户ID
     * @return 默认API密钥（如果不存在返回null）
     */
    UserApiKey selectDefaultByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和密钥名称查询
     *
     * @param userId 用户ID
     * @param keyName 密钥名称
     * @return API密钥实体
     */
    UserApiKey selectByUserIdAndKeyName(@Param("userId") Long userId,
                                        @Param("keyName") String keyName);

    /**
     * 软删除API密钥（更新is_deleted和deleted_time）
     *
     * @param id 密钥ID
     * @param userId 用户ID（用于权限验证）
     * @param deletedTime 删除时间
     * @return 影响行数
     */
    int softDeleteById(@Param("id") Long id,
                      @Param("userId") Long userId,
                      @Param("deletedTime") LocalDateTime deletedTime);

    /**
     * 设置默认密钥（会先取消用户的其他默认密钥）
     *
     * @param userId 用户ID
     * @param keyId 密钥ID
     * @return 影响行数
     */
    int setDefaultKey(@Param("userId") Long userId, @Param("keyId") Long keyId);

    /**
     * 取消用户的所有默认密钥
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int clearDefaultKeys(@Param("userId") Long userId);

    /**
     * 更新最后使用时间
     *
     * @param id 密钥ID
     * @param lastUsedTime 最后使用时间
     * @return 影响行数
     */
    int updateLastUsedTime(@Param("id") Long id, @Param("lastUsedTime") LocalDateTime lastUsedTime);

    /**
     * 更新验证状态
     *
     * @param id 密钥ID
     * @param verificationStatus 验证状态
     * @param lastVerifiedTime 验证时间
     * @return 影响行数
     */
    int updateVerificationStatus(@Param("id") Long id,
                                 @Param("verificationStatus") String verificationStatus,
                                 @Param("lastVerifiedTime") LocalDateTime lastVerifiedTime);

    // ==================== 多租户支持方法（2026-03-31新增） ====================

    /**
     * 根据租户ID和用户ID查询所有API密钥（未删除）
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return API密钥列表
     */
    List<UserApiKey> selectByTenantIdAndUserId(@Param("tenantId") Long tenantId,
                                               @Param("userId") Long userId);

    /**
     * 根据租户ID和用户ID查询默认API密钥
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 默认API密钥（如果不存在返回null）
     */
    UserApiKey selectDefaultByTenantIdAndUserId(@Param("tenantId") Long tenantId,
                                                 @Param("userId") Long userId);

    /**
     * 根据租户ID和密钥ID查询
     *
     * @param tenantId 租户ID
     * @param id 密钥ID
     * @return API密钥实体
     */
    UserApiKey selectByTenantIdAndId(@Param("tenantId") Long tenantId,
                                     @Param("id") Long id);

    /**
     * 软删除API密钥（按租户隔离）
     *
     * @param tenantId 租户ID
     * @param id 密钥ID
     * @param userId 用户ID（用于权限验证）
     * @param deletedTime 删除时间
     * @return 影响行数
     */
    int softDeleteByTenantIdAndId(@Param("tenantId") Long tenantId,
                                  @Param("id") Long id,
                                  @Param("userId") Long userId,
                                  @Param("deletedTime") LocalDateTime deletedTime);

    /**
     * 设置默认密钥（按租户隔离，会先取消该租户用户的其他默认密钥）
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @param keyId 密钥ID
     * @return 影响行数
     */
    int setDefaultKeyByTenantId(@Param("tenantId") Long tenantId,
                                @Param("userId") Long userId,
                                @Param("keyId") Long keyId);

    /**
     * 取消租户用户的所有默认密钥
     *
     * @param tenantId 租户ID
     * @param userId 用户ID
     * @return 影响行数
     */
    int clearDefaultKeysByTenantId(@Param("tenantId") Long tenantId,
                                   @Param("userId") Long userId);
}
