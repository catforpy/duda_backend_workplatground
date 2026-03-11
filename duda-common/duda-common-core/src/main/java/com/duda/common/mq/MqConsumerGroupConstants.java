package com.duda.common.mq;

/**
 * MQ 消费者组命名常量
 * 统一管理所有 RocketMQ 消费者组名称
 *
 * 使用方法：
 * <pre>
 * &#64;RocketMQMessageListener(
 *     topic = MqTopicConstants.USER_REGISTER,
 *     consumerGroup = MqConsumerGroupConstants.USER_REGISTER_GROUP
 * )
 * public class UserRegisterListener implements RocketMQListener<UserRegisterMsg> {
 *     ...
 * }
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
public class MqConsumerGroupConstants {

    // ==================== 用户服务消费者组 ====================

    /**
     * 用户注册消费者组
     */
    public static final String USER_REGISTER_GROUP = "user-register-group";

    /**
     * 用户缓存删除消费者组
     */
    public static final String USER_CACHE_DELETE_GROUP = "user-cache-delete-group";

    // ==================== 认证服务消费者组 ====================

    /**
     * Token刷新消费者组
     */
    public static final String TOKEN_REFRESH_GROUP = "token-refresh-group";

    /**
     * Token失效消费者组
     */
    public static final String TOKEN_INVALIDATE_GROUP = "token-invalidate-group";

    // ==================== 订单服务消费者组 ====================

    /**
     * 订单创建消费者组
     */
    public static final String ORDER_CREATE_GROUP = "order-create-group";

    /**
     * 订单支付消费者组
     */
    public static final String ORDER_PAID_GROUP = "order-paid-group";

    /**
     * 订单取消消费者组
     */
    public static final String ORDER_CANCEL_GROUP = "order-cancel-group";

    // ==================== 内容服务消费者组 ====================

    /**
     * 内容发布消费者组
     */
    public static final String CONTENT_PUBLISH_GROUP = "content-publish-group";

    /**
     * 内容同步消费者组（同步到ES）
     */
    public static final String CONTENT_SYNC_GROUP = "content-sync-group";

    // ==================== 通知服务消费者组 ====================

    /**
     * 系统通知消费者组
     */
    public static final String SYSTEM_NOTIFICATION_GROUP = "system-notification-group";

    /**
     * 站内消息消费者组
     */
    public static final String STATION_MESSAGE_GROUP = "station-message-group";

    // ==================== 搜索服务消费者组 ====================

    /**
     * ES数据同步消费者组
     */
    public static final String ES_DATA_SYNC_GROUP = "es-data-sync-group";
}
