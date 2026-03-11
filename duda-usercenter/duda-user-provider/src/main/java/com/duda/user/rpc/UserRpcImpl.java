package com.duda.user.rpc;

import com.duda.common.domain.PageResult;
import com.duda.user.dto.UserDTO;
import com.duda.user.dto.UserLoginReqDTO;
import com.duda.user.dto.UserRegisterReqDTO;
import com.duda.user.rpc.IUserRpc;
import com.duda.user.service.UserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户RPC实现类
 * 暴露Dubbo服务供其他服务调用
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@DubboService(
    version = "1.0.0",
    group = "USER_GROUP",
    timeout = 5000
)
public class UserRpcImpl implements IUserRpc {

    @Resource
    private UserService userService;

    @Override
    public Long register(UserRegisterReqDTO registerReq) {
        return userService.register(registerReq);
    }

    @Override
    public UserDTO login(UserLoginReqDTO loginReq) {
        return userService.login(loginReq);
    }

    @Override
    public UserDTO getUserById(Long userId) {
        return userService.getUserById(userId);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        return userService.getUserByUsername(username);
    }

    @Override
    public Boolean updateUser(UserDTO userDTO) {
        return userService.updateUser(userDTO);
    }

    @Override
    public Boolean deleteUser(Long userId) {
        return userService.deleteUser(userId);
    }

    @Override
    public PageResult pageUsers(String userType, String status, String keyword, Integer pageNum, Integer pageSize) {
        return userService.pageUsers(userType, status, keyword, pageNum, pageSize);
    }
}
