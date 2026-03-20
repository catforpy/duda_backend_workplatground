package com.duda.user.api.controller;

import com.duda.common.domain.Result;
import com.duda.user.api.service.UserApiKeyService;
import com.duda.user.dto.userapikey.AddUserApiKeyReqDTO;
import com.duda.user.dto.userapikey.UserApiKeyDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户API密钥管理控制器
 *
 * @author DudaNexus
 * @since 2026-03-17
 */
@Slf4j
@Tag(name = "用户API密钥管理", description = "用户云存储API密钥管理接口")
@RestController
@RequestMapping("/api/user-api-keys")
@CrossOrigin(originPatterns = "*", maxAge = 3600)
public class UserApiKeyController {

    @Resource
    private UserApiKeyService userApiKeyService;

    /**
     * 添加用户API密钥
     *
     * 接收明文密钥，加密后存储到数据库
     *
     * @param request 添加请求
     * @return API密钥信息
     */
    @Operation(summary = "添加API密钥", description = "添加用户的云存储API密钥（明文接收，加密存储）")
    @PostMapping("/add")
    public Result addUserApiKey(
        @Parameter(description = "添加请求", required = true)
        @Valid @RequestBody AddUserApiKeyReqDTO request) {

        UserApiKeyDTO apiKey = userApiKeyService.addUserApiKey(request);
        return Result.success("API密钥添加成功", apiKey);
    }

    /**
     * 获取用户的所有API密钥
     *
     * @param userId 用户ID
     * @param includeInactive 是否包含禁用的密钥
     * @return API密钥列表
     */
    @Operation(summary = "获取API密钥列表", description = "查询用户的所有API密钥")
    @GetMapping("/list")
    public Result listUserApiKeys(
        @Parameter(description = "用户ID", required = true)
        @RequestParam("userId") Long userId,
        @Parameter(description = "是否包含禁用的密钥")
        @RequestParam(value = "includeInactive", defaultValue = "false") Boolean includeInactive) {

        List<UserApiKeyDTO> apiKeys = userApiKeyService.listUserApiKeys(userId, includeInactive);
        return Result.success(apiKeys);
    }

    /**
     * 获取用户的默认API密钥
     *
     * @param userId 用户ID
     * @return 默认API密钥
     */
    @Operation(summary = "获取默认API密钥", description = "查询用户的默认API密钥")
    @GetMapping("/default")
    public Result getDefaultUserApiKey(
        @Parameter(description = "用户ID", required = true)
        @RequestParam("userId") Long userId) {

        UserApiKeyDTO apiKey = userApiKeyService.getDefaultUserApiKey(userId);
        return Result.success(apiKey);
    }

    /**
     * 删除用户API密钥
     *
     * @param keyId 密钥ID
     * @param userId 用户ID（用于权限验证）
     * @return 删除结果
     */
    @Operation(summary = "删除API密钥", description = "删除指定的API密钥")
    @DeleteMapping("/{keyId}")
    public Result deleteUserApiKey(
        @Parameter(description = "密钥ID", required = true)
        @PathVariable Long keyId,
        @Parameter(description = "用户ID", required = true)
        @RequestParam("userId") Long userId) {

        Boolean success = userApiKeyService.deleteUserApiKey(keyId, userId);
        return success ?
            Result.success("API密钥删除成功", null) :
            Result.error("API密钥删除失败");
    }

    /**
     * 设置默认密钥
     *
     * @param keyId 密钥ID
     * @param userId 用户ID
     * @return 设置结果
     */
    @Operation(summary = "设置默认密钥", description = "将指定密钥设为默认密钥")
    @PutMapping("/{keyId}/set-default")
    public Result setDefaultApiKey(
        @Parameter(description = "密钥ID", required = true)
        @PathVariable Long keyId,
        @Parameter(description = "用户ID", required = true)
        @RequestParam("userId") Long userId) {

        Boolean success = userApiKeyService.setDefaultApiKey(keyId, userId);
        return success ?
            Result.success("默认密钥设置成功", null) :
            Result.error("默认密钥设置失败");
    }

    /**
     * 更新密钥状态
     *
     * @param keyId 密钥ID
     * @param userId 用户ID
     * @param active 是否启用
     * @return 更新结果
     */
    @Operation(summary = "更新密钥状态", description = "启用或禁用API密钥")
    @PutMapping("/{keyId}/status")
    public Result updateApiKeyStatus(
        @Parameter(description = "密钥ID", required = true)
        @PathVariable Long keyId,
        @Parameter(description = "用户ID", required = true)
        @RequestParam("userId") Long userId,
        @Parameter(description = "是否启用", required = true)
        @RequestParam("active") Boolean active) {

        Boolean success = userApiKeyService.updateApiKeyStatus(keyId, userId, active);
        return success ?
            Result.success("密钥状态更新成功", null) :
            Result.error("密钥状态更新失败");
    }

    // ==================== 测试 API ====================

    /**
     * 根据用户名添加API密钥（测试API）
     *
     * 接收明文密钥，加密后保存到数据库
     * 为了简化测试，使用 username 代替 userId
     *
     * @param username 用户名
     * @param keyName 密钥名称
     * @param keyType 密钥类型
     * @param accessKeyId AccessKey ID（明文）
     * @param accessKeySecret AccessKey Secret（明文）
     * @return API密钥信息
     */
    @Operation(summary = "根据用户名添加密钥（测试）", description = "根据用户名添加API密钥，加密后保存")
    @PostMapping("/add-by-username")
    public Result addUserApiKeyByUsername(
        @Parameter(description = "用户名", required = true)
        @RequestParam("username") String username,
        @Parameter(description = "密钥名称", required = true)
        @RequestParam("keyName") String keyName,
        @Parameter(description = "密钥类型", required = true)
        @RequestParam("keyType") String keyType,
        @Parameter(description = "AccessKey ID（明文）", required = true)
        @RequestParam("accessKeyId") String accessKeyId,
        @Parameter(description = "AccessKey Secret（明文）", required = true)
        @RequestParam("accessKeySecret") String accessKeySecret) {

        log.info("【测试API】添加密钥: username={}, keyName={}", username, keyName);

        // 调用 Service
        UserApiKeyDTO apiKey = userApiKeyService.addUserApiKeyByUsername(
            username, keyName, keyType, accessKeyId, accessKeySecret);

        return Result.success("API密钥添加成功", apiKey);
    }

    /**
     * 根据用户名获取API密钥（测试API）
     *
     * 返回解密后的明文密钥，用于验证加密/解密逻辑是否正确
     * ⚠️ 仅用于测试，生产环境应禁用
     *
     * @param username 用户名
     * @return 解密后的API密钥（包含明文）
     */
    @Operation(summary = "根据用户名获取密钥（测试）", description = "获取解密后的明文密钥，仅用于测试")
    @GetMapping("/get-by-username")
    public Result getUserApiKeyByUsername(
        @Parameter(description = "用户名", required = true)
        @RequestParam("username") String username) {

        log.info("【测试API】获取密钥: username={}", username);

        // 调用 Service，返回解密后的密钥
        UserApiKeyDTO apiKey = userApiKeyService.getUserApiKeyByUsername(username);

        if (apiKey == null) {
            return Result.error("未找到密钥");
        }

        return Result.success(apiKey);
    }
}
