package com.duda.user.entity.merchant;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商户用户映射Entity
 *
 * 对应数据库表：merchant_users
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Data
@TableName("merchant_users")
public class MerchantUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 平台用户ID
     */
    private Long platformUserId;

    /**
     * 平台用户分片ID（0-99）
     */
    private Integer platformUserShard;

    /**
     * 商户小程序内的用户ID（虚拟ID）
     */
    private String merchantUserId;

    /**
     * 该商户小程序的OpenID
     */
    private String miniAppOpenid;

    /**
     * 微信UnionID
     */
    private String miniAppUnionid;

    /**
     * 微信SessionKey（AES加密）
     */
    private String miniAppSessionKey;

    /**
     * 微信OpenID（兼容字段）
     */
    private String wechatOpenid;

    /**
     * 微信UnionID（兼容字段）
     */
    private String wechatUnionid;

    /**
     * 支付宝用户ID
     */
    private String alipayUserId;

    /**
     * 支付宝OpenID
     */
    private String alipayOpenid;

    /**
     * 商户小程序内的昵称
     */
    private String nickname;

    /**
     * 商户小程序内的头像
     */
    private String avatar;

    /**
     * 性别：0-未知 1-男 2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 语言
     */
    private String language;

    /**
     * 来源类型
     */
    private String sourceType;

    /**
     * 来源渠道
     */
    private String sourceChannel;

    /**
     * 场景值（小程序场景值）
     */
    private Integer sourceScene;

    /**
     * 用户标签（逗号分隔）
     */
    private String userTags;

    /**
     * 用户备注
     */
    private String userRemark;

    /**
     * 用户等级
     */
    private String userLevel;

    /**
     * 用户分组
     */
    private String userGroup;

    /**
     * 用户积分
     */
    private Integer userScore;

    /**
     * 用户余额
     */
    private BigDecimal userBalance;

    /**
     * 状态：1-正常 2-禁用 3-已删除
     */
    private Integer status;

    /**
     * 关注状态：0-未关注 1-已关注
     */
    private Integer followStatus;

    /**
     * 订阅状态：0-未订阅 1-已订阅
     */
    private Integer subscribeStatus;

    /**
     * 访问次数
     */
    private Integer visitCount;

    /**
     * 订单数量
     */
    private Integer orderCount;

    /**
     * 消费金额
     */
    private BigDecimal consumptionAmount;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastVisitTime;

    /**
     * 最后访问IP
     */
    private String lastVisitIp;

    /**
     * 首次访问时间
     */
    private LocalDateTime firstVisitTime;

    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;

    /**
     * 解绑时间
     */
    private LocalDateTime unbindTime;

    /**
     * 最后下单时间
     */
    private LocalDateTime lastOrderTime;

    /**
     * 最后支付时间
     */
    private LocalDateTime lastPayTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
