package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.common.domain.PageResult;
import com.duda.user.dto.UserDTO;
import com.duda.user.dto.UserLoginReqDTO;
import com.duda.user.dto.UserRegisterReqDTO;
import com.duda.user.rpc.IUserRpc;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

/**
 * 用户Controller - API层
 * 提供REST API，与前端对接
 * 内部调用Provider的RPC服务
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @DubboReference(
        version = "1.0.0",
        group = "default",
        check = false  // 启动时不检查服务提供者
    )
    private IUserRpc userRpc;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterReqDTO registerReq) {
        Long userId = userRpc.register(registerReq);
        return Result.success(userId);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginReqDTO loginReq) {
        UserDTO userDTO = userRpc.login(loginReq);
        return Result.success(userDTO);
    }

    /**
     * 根据ID查询用户
     */
    @Operation(summary = "根据ID查询用户")
    @GetMapping("/{userId}")
    public Result getUserById(@PathVariable("userId") Long userId) {
        UserDTO userDTO = userRpc.getUserById(userId);
        return Result.success(userDTO);
    }

    /**
     * 根据用户名查询用户
     */
    @Operation(summary = "根据用户名查询用户")
    @GetMapping("/username/{username}")
    public Result getUserByUsername(@PathVariable("username") String username) {
        UserDTO userDTO = userRpc.getUserByUsername(username);
        return Result.success(userDTO);
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    public Result updateUser(@RequestBody UserDTO userDTO) {
        Boolean success = userRpc.updateUser(userDTO);
        return Result.success(success);
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{userId}")
    public Result deleteUser(@PathVariable("userId") Long userId) {
        Boolean success = userRpc.deleteUser(userId);
        return Result.success(success);
    }

    /**
     * 分页查询用户
     */
    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result pageUsers(
            @RequestParam(value = "userType", required = false) String userType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageResult pageResult = userRpc.pageUsers(userType, status, keyword, pageNum, pageSize);
        return Result.success(pageResult);
    }
}
