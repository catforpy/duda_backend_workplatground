package com.duda.common.mq;

/**
 * MQ Topic 命名常量
 * 统一管理所有 RocketMQ Topic 名称
 *
 * 使用方法：
 * <pre>
 * &#64;Autowired
 * private RocketMQTemplate rocketMQTemplate;
 *
 * // 发送消息
 * rocketMQTemplate.syncSend(
 *     MqTopicConstants.USER_CACHE_ASYNC_DELETE,
 *     message,
 *     "user-group"
 * );
 * </pre>
 *
 * @author DudaNexus
 * @ @since 2026-03-10
 */
public class MqTopicConstants {

    // ==================== 用户服务 Topic ====================

    /**
     * 用户缓存异步删除Topic
     * 用于用户信息变更时，同步删除Redis缓存
     */
    public static final String USER_CACHE_ASYNC_DELETE = "UserCacheAsyncDelete";

    /**
     * 用户注册Topic
     * 用于新用户注册后的消息通知
     */
    public static final String USER_REGISTER = "UserRegister";

    /**
     * 用户登录Topic
     * 用于用户登录后的消息通知
     */
    public static final String USER_LOGIN = "UserLogin";

    /**
     * 用户注销Topic
     * 用于用户注销后的消息通知
     */
    public static final String USER_LOGOUT = "UserLogout";

    // ==================== 认证服务 Topic ====================

    /**
     * Token刷新Topic
     */
    public static final String TOKEN_REFRESH = "TokenRefresh";

    /**
     * Token失效Topic
     */
    public static final String TOKEN_INVALIDATE = "TokenInvalidate";

    // ==================== 订单服务 Topic ====================

    /**
     * 订单创建Topic
     */
    public static final String ORDER_CREATE = "OrderCreate";

    /**
     * 订单支付成功Topic
     */
    public static final String ORDER_PAID = "OrderPaid";

    /**
     * 订单取消Topic
     */
    public static final String ORDER_CANCEL = "OrderCancel";

    /**
     * 订单超时Topic
     */
    public static final String ORDER_TIMEOUT = "OrderTimeout";

    // ==================== 内容服务 Topic ====================

    /**
     * 内容发布Topic
     */
    public static final String CONTENT_PUBLISH = "ContentPublish";

    /**
     * 内容审核Topic
     */
    public static final String CONTENT_AUDIT = "ContentAudit";

    // ==================== 通知服务 Topic ====================

    /**
     * 系统通知Topic
     */
    public static final String SYSTEM_NOTIFICATION = "SystemNotification";

    /**
     * 站内消息Topic
     */
    public static final String STATION_MESSAGE = "StationMessage";

    // ==================== 搜索服务 Topic ====================

    /**
     * ES数据同步Topic
     * 用于将数据同步到Elasticsearch
     */
    public static final String ES_DATA_SYNC = "EsDataSync";
}
