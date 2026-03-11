package com.duda.common.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * MQ 消息体基类
 * 所有 MQ 消息都应该继承此类
 *
 * 使用方法：
 * <pre>
 * &#64;Data
 * public class UserRegisterMsg extends BaseMqMsg {
 *     private Long userId;
 *     private String username;
 * }
 *
 * // 发送消息
 * UserRegisterMsg msg = new UserRegisterMsg();
 * msg.setUserId(1L);
 * msg.setUsername("test");
 * rocketMQUtils.syncSend(MqTopicConstants.USER_REGISTER, msg, "user-register-group");
 * </pre>
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Data
public class BaseMqMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息生成时间戳
     */
    private Long timestamp;

    /**
     * 消息ID（雪花算法生成）
     */
    private Long messageId;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 来源服务
     */
    private String source;

    /**
     * 目标服务
     */
    private String target;

    /**
     * 业务数据（JSON）
     */
    private Object payload;

    public BaseMqMsg() {
        this.timestamp = System.currentTimeMillis();
        this.messageId = com.duda.common.util.IdGenerator.nextId();
    }
}
