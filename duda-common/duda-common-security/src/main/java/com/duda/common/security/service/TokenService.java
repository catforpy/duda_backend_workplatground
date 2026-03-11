package com.duda.common.security.service;

import com.duda.common.redis.RedisUtils;
import com.duda.common.security.properties.JwtProperties;
import com.duda.common.security.util.JwtTokenProvider;
import com.duda.common.security.dto.TokenDTO;
import com.duda.common.security.dto.RefreshTokenReqDTO;
import com.duda.common.web.exception.BizException;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Token服务
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private RedisUtils redisUtils;

    /**
     * 生成Access Token和Refresh Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @return Token信息
     */
    public TokenDTO generateTokens(Long userId, String username, String userType) {
        // 生成Access Token
        String accessToken = jwtTokenProvider.generateAccessToken(userId, username, userType);

        // 生成Refresh Token
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // 将Refresh Token存储到Redis
        String refreshKey = jwtProperties.getRefreshTokenPrefix() + userId;
        Long refreshExpiration = jwtProperties.getRefreshTokenExpiration();
        redisUtils.set(refreshKey, refreshToken, refreshExpiration.intValue());

        logger.info("生成Token成功，userId: {}, username: {}", userId, username);

        return TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiration().intValue())
                .build();
    }

    /**
     * 刷新Access Token
     *
     * @param reqDTO 刷新Token请求
     * @return 新的Token信息
     */
    public TokenDTO refreshToken(RefreshTokenReqDTO reqDTO) {
        String refreshToken = reqDTO.getRefreshToken();

        // 1. 验证Refresh Token格式
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BizException("Refresh Token无效或已过期");
        }

        // 2. 检查是否为Refresh Token
        String tokenType = jwtTokenProvider.getTokenTypeFromToken(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BizException("Token类型错误");
        }

        // 3. 从Refresh Token中获取用户ID
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new BizException("无法从Token中获取用户信息");
        }

        // 4. 检查Redis中是否存在该Refresh Token
        String refreshKey = jwtProperties.getRefreshTokenPrefix() + userId;
        String storedRefreshToken = redisUtils.get(refreshKey, String.class);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new BizException("Refresh Token已失效，请重新登录");
        }

        // 5. 重新生成Token（TODO: 这里需要从数据库或缓存获取用户信息，暂时简化处理）
        // 注意：实际应用中应该从数据库查询最新的用户信息
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        String userType = jwtTokenProvider.getUserTypeFromToken(refreshToken);
        if (username == null || userType == null) {
            // 对于Refresh Token，可能没有这些信息，需要从数据库查询
            throw new BizException("无法获取用户完整信息，请重新登录");
        }

        return generateTokens(userId, username, userType);
    }

    /**
     * 将Access Token加入黑名单（用于登出）
     *
     * @param accessToken Access Token
     */
    public void blacklistAccessToken(String accessToken) {
        // 1. 验证Token
        if (!jwtTokenProvider.validateToken(accessToken)) {
            logger.warn("尝试将无效Token加入黑名单: {}", accessToken);
            return;
        }

        // 2. 计算Token剩余有效时间
        long remainingTime = jwtTokenProvider.getTokenRemainingTime(accessToken);
        if (remainingTime <= 0) {
            logger.warn("Token已过期，无需加入黑名单");
            return;
        }

        // 3. 将Token加入黑名单
        String blacklistKey = jwtProperties.getTokenBlacklistPrefix() + accessToken;
        redisUtils.set(blacklistKey, "1", (int) remainingTime);

        logger.info("Access Token已加入黑名单，剩余时间: {}秒", remainingTime);
    }

    /**
     * 删除Refresh Token（用于登出）
     *
     * @param userId 用户ID
     */
    public void deleteRefreshToken(Long userId) {
        String refreshKey = jwtProperties.getRefreshTokenPrefix() + userId;
        redisUtils.delete(refreshKey);
        logger.info("Refresh Token已删除，userId: {}", userId);
    }

    /**
     * 检查Access Token是否在黑名单中
     *
     * @param accessToken Access Token
     * @return 是否在黑名单中
     */
    public boolean isAccessTokenBlacklisted(String accessToken) {
        String blacklistKey = jwtProperties.getTokenBlacklistPrefix() + accessToken;
        return Boolean.TRUE.equals(redisUtils.hasKey(blacklistKey));
    }

    /**
     * 用户登出（同时失效Access Token和Refresh Token）
     *
     * @param accessToken Access Token
     * @param userId 用户ID
     */
    public void logout(String accessToken, Long userId) {
        // 将Access Token加入黑名单
        blacklistAccessToken(accessToken);

        // 删除Refresh Token
        deleteRefreshToken(userId);

        logger.info("用户登出成功，userId: {}", userId);
    }

    /**
     * 验证Access Token是否有效
     *
     * @param accessToken Access Token
     * @return 是否有效
     */
    public boolean validateAccessToken(String accessToken) {
        // 1. 检查Token格式和签名
        if (!jwtTokenProvider.validateToken(accessToken)) {
            return false;
        }

        // 2. 检查是否在黑名单中
        if (isAccessTokenBlacklisted(accessToken)) {
            logger.warn("Access Token在黑名单中");
            return false;
        }

        // 3. 检查是否过期
        if (jwtTokenProvider.isTokenExpired(accessToken)) {
            logger.warn("Access Token已过期");
            return false;
        }

        // 4. 检查是否为Access Token
        String tokenType = jwtTokenProvider.getTokenTypeFromToken(accessToken);
        if (!"access".equals(tokenType)) {
            logger.warn("Token类型错误，期望Access Token");
            return false;
        }

        return true;
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    /**
     * 从Token中获取用户类型
     *
     * @param token JWT Token
     * @return 用户类型
     */
    public String getUserTypeFromToken(String token) {
        return jwtTokenProvider.getUserTypeFromToken(token);
    }
}
