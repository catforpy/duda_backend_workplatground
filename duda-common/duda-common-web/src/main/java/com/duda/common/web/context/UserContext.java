package com.duda.common.web.context;

/**
 * 用户上下文 - ThreadLocal
 * 用于在请求处理过程中存储和获取用户信息
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_TYPE = new ThreadLocal<>();

    /**
     * 设置用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置用户名
     */
    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    /**
     * 获取用户名
     */
    public static String getUsername() {
        return USERNAME.get();
    }

    /**
     * 设置用户类型
     */
    public static void setUserType(String userType) {
        USER_TYPE.set(userType);
    }

    /**
     * 获取用户类型
     */
    public static String getUserType() {
        return USER_TYPE.get();
    }

    /**
     * 清理所有用户信息
     * 必须在请求结束时调用，避免内存泄漏
     */
    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        USER_TYPE.remove();
    }
}
