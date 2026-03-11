package com.duda.user.service;

import com.duda.common.domain.PageResult;
import com.duda.user.dto.UserDTO;
import com.duda.user.dto.UserLoginReqDTO;
import com.duda.user.dto.UserRegisterReqDTO;

/**
 * 用户服务接口
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param registerReq 注册请求
     * @return 用户ID
     */
    Long register(UserRegisterReqDTO registerReq);

    /**
     * 用户登录
     *
     * @param loginReq 登录请求
     * @return 用户信息
     */
    UserDTO login(UserLoginReqDTO loginReq);

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserDTO getUserById(Long userId);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserDTO getUserByUsername(String username);

    /**
     * 更新用户信息
     *
     * @param userDTO 用户信息
     * @return 是否成功
     */
    Boolean updateUser(UserDTO userDTO);

    /**
     * 删除用户（软删除）
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteUser(Long userId);

    /**
     * 分页查询用户
     *
     * @param userType 用户类型（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选，搜索用户名、真实姓名、手机号）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult pageUsers(String userType, String status, String keyword, Integer pageNum, Integer pageSize);
}
