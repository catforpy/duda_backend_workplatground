package com.duda.common.security.util;

import com.duda.common.security.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token工具类 - 支持不同用户类型
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Resource
    private JwtProperties jwtProperties;

    /**
     * 生成Access Token
     * 包含用户基本信息，根据用户类型使用不同的密钥和过期时间
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param userType 用户类型
     * @return Access Token
     */
    public String generateAccessToken(Long userId, String username, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("userType", userType);
        claims.put("tokenType", "access");

        // 根据用户类型获取对应的过期时间
        Long expiration = jwtProperties.getUserTypeAccessTokenExpiration(userType);

        logger.info("生成Access Token，userType={}, expiration={}秒", userType, expiration);

        return createToken(claims, expiration, userType);
    }

    /**
     * 生成Refresh Token
     * 只包含用户ID，根据用户类型使用不同的密钥和过期时间
     *
     * @param userId 用户ID
     * @param userType 用户类型
     * @return Refresh Token
     */
    public String generateRefreshToken(Long userId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userType", userType);
        claims.put("tokenType", "refresh");

        // 根据用户类型获取对应的过期时间
        Long expiration = jwtProperties.getUserTypeRefreshTokenExpiration(userType);

        logger.info("生成Refresh Token，userType={}, expiration={}秒", userType, expiration);

        return createToken(claims, expiration, userType);
    }

    /**
     * 创建Token
     *
     * @param claims 自定义声明
     * @param expiration 有效期（秒）
     * @param userType 用户类型（用于选择密钥）
     * @return JWT Token
     */
    private String createToken(Map<String, Object> claims, Long expiration, String userType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        // 根据用户类型获取对应的密钥
        SecretKey signingKey = getSigningKeyByUserType(userType);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 从Token中获取用户类型
     *
     * @param token JWT Token
     * @return 用户类型
     */
    public String getUserTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("userType", String.class) : null;
    }

    /**
     * 从Token中获取Token类型
     *
     * @param token JWT Token
     * @return Token类型（access或refresh）
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("tokenType", String.class) : null;
    }

    /**
     * 从Token中获取过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 验证Token是否有效
     * 自动根据Token中的userType选择对应的密钥进行验证
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            // 从Token中提取userType
            String userType = getUserTypeFromToken(token);
            if (userType == null) {
                logger.error("无法从Token中提取userType");
                return false;
            }

            // 根据userType获取对应的密钥
            SecretKey signingKey = getSigningKeyByUserType(userType);

            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * 检查Token是否过期
     *
     * @param token JWT Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    /**
     * 从Token中获取Claims
     * 自动根据Token中的userType选择对应的密钥进行解析
     *
     * @param token JWT Token
     * @return Claims
     */
    private Claims getClaimsFromToken(String token) {
        try {
            // 从Token中提取userType
            String userType = getUserTypeFromToken(token);
            if (userType == null) {
                logger.error("无法从Token中提取userType");
                return null;
            }

            // 根据userType获取对应的密钥
            SecretKey signingKey = getSigningKeyByUserType(userType);

            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("Error parsing JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 根据用户类型获取签名密钥
     *
     * @param userType 用户类型
     * @return 签名密钥
     */
    private SecretKey getSigningKeyByUserType(String userType) {
        String secret = jwtProperties.getUserTypeSecret(userType);
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取默认签名密钥（向后兼容）
     *
     * @return 签名密钥
     * @deprecated 使用 getSigningKeyByUserType() 替代
     */
    @Deprecated
    private SecretKey getSigningKey() {
        // 使用平台管理员密钥作为默认密钥
        String secret = jwtProperties.getUserTypeSecret("platform-admin");
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 计算Token剩余有效时间（秒）
     *
     * @param token JWT Token
     * @return 剩余有效时间（秒），-1表示已过期或无效
     */
    public long getTokenRemainingTime(String token) {
        Date expiration = getExpirationDateFromToken(token);
        if (expiration == null) {
            return -1;
        }
        long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        return remaining > 0 ? remaining : -1;
    }
}
