package com.duda.common.security.interceptor;

import com.duda.common.security.properties.JwtProperties;
import com.duda.common.security.service.TokenService;
import com.duda.common.web.context.UserContext;
import com.duda.common.web.exception.BizException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Token拦截器
 * 验证请求中的Access Token
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);

    @Resource
    private TokenService tokenService;

    @Resource
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从Header中获取Token
        String authHeader = request.getHeader(jwtProperties.getHeaderKey());
        if (authHeader == null || !authHeader.startsWith(jwtProperties.getTokenPrefix())) {
            throw new BizException(401, "未提供认证信息");
        }

        // 2. 去掉前缀，获取纯Token
        String accessToken = authHeader.substring(jwtProperties.getTokenPrefix().length());

        // 3. 验证Token
        if (!tokenService.validateAccessToken(accessToken)) {
            throw new BizException(401, "Token无效或已过期");
        }

        // 4. 将用户信息存入ThreadLocal（供后续业务使用）
        Long userId = getUserIdFromToken(accessToken);
        String username = getUsernameFromToken(accessToken);
        String userType = getUserTypeFromToken(accessToken);

        if (userId != null) {
            UserContext.setUserId(userId);
            UserContext.setUsername(username);
            UserContext.setUserType(userType);

            logger.debug("Token验证成功，userId: {}, username: {}", userId, username);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理ThreadLocal，避免内存泄漏
        UserContext.clear();
    }

    /**
     * 从Token中提取用户ID
     */
    private Long getUserIdFromToken(String token) {
        try {
            // 这里使用简单的方式解析Token获取用户ID
            // 实际上应该调用JwtTokenProvider，但为了避免循环依赖，这里简化处理
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            // TODO: 实现解析逻辑，或者将JwtTokenProvider作为静态工具类
            return null; // 暂时返回null，实际应该解析JWT获取userId
        } catch (Exception e) {
            logger.error("解析Token失败", e);
            return null;
        }
    }

    /**
     * 从Token中提取用户名
     */
    private String getUsernameFromToken(String token) {
        // TODO: 实现解析逻辑
        return null;
    }

    /**
     * 从Token中提取用户类型
     */
    private String getUserTypeFromToken(String token) {
        // TODO: 实现解析逻辑
        return null;
    }
}
