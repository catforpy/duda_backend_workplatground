package com.duda.user.dto.merchant;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商户用户映射DTO
 *
 * 说明：多租户SAAS模式的核心表！实现"一个用户，在不同商户有不同虚拟ID"
 *
 * @author DudaNexus
 * @since 2026-03-22
 */
@Data
public class MerchantUserDTO implements Serializable {

    /**
     * 主键ID（雪花算法）
     */
    private Long id;

    // ========== 关联关系 ==========

    /**
     * 商户ID（关联merchants表）
     */
    private Long merchantId;

    /**
     * 商户名称（冗余字段）
     */
    private String merchantName;

    /**
     * 平台用户ID（关联user_nexus.users_XX表，全局唯一）
     */
    private Long platformUserId;

    /**
     * 平台用户分片ID（0-99，用于快速定位users_XX表）
     */
    private Integer platformUserShard;

    /**
     * 商户小程序内的用户ID（虚拟ID，如：M1001_001）
     */
    private String merchantUserId;

    // ========== 微信相关 ==========

    /**
     * 该商户小程序的OpenID（每个商户不同）
     */
    private String miniAppOpenid;

    /**
     * 微信UnionID（跨小程序唯一）
     */
    private String miniAppUnionid;

    // ========== 商户小程序内的用户信息 ==========

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

    // ========== 用户来源 ==========

    /**
     * 来源类型：mini_program-小程序, app-App, web-Web, h5-H5, api-API
     */
    private String sourceType;

    /**
     * 来源渠道
     */
    private String sourceChannel;

    // ========== 用户标签（商户自定义） ==========

    /**
     * 用户标签（逗号分隔，如：VIP、新客户、老客户）
     */
    private String userTags;

    /**
     * 用户备注
     */
    private String userRemark;

    /**
     * 用户等级：normal-普通, vip-VIP, svip-SVIP
     */
    private String userLevel;

    /**
     * 用户分组（如：活跃用户、沉睡用户）
     */
    private String userGroup;

    /**
     * 用户积分
     */
    private Integer userScore;

    /**
     * 用户余额（元）
     */
    private BigDecimal userBalance;

    // ========== 状态管理 ==========

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

    // ========== 访问统计 ==========

    /**
     * 访问次数
     */
    private Integer visitCount;

    /**
     * 订单数量
     */
    private Integer orderCount;

    /**
     * 消费金额（元）
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

    // ========== 时间信息 ==========

    /**
     * 绑定时间（用户首次访问该商户小程序）
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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
