package com.duda.user.provider.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.duda.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商户用户PO
 *
 * 表名: merchant_users
 * 说明: 商户用户关系表，记录商户与平台用户的绑定关系
 * 租户隔离: 是（通过tenant_id字段）
 *
 * @author Claude
 * @date 2026-03-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("merchant_users")
public class MerchantUserPO extends BaseEntity {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 租户ID（租户隔离字段）
     */
    private Long tenantId;

    /**
     * 商户ID（关联merchants.id）
     */
    private Long merchantId;

    /**
     * 平台用户ID（关联user_nexus.users_XX.id）
     */
    private Long platformUserId;

    /**
     * 平台用户分片编号（0-99，用于定位users_XX表）
     */
    private Byte platformUserShard;

    /**
     * 商户用户ID（商户内唯一标识）
     * 示例: M1001_001
     */
    private String merchantUserId;

    /**
     * 小程序OpenID
     */
    private String miniAppOpenid;

    /**
     * 小程序UnionID
     */
    private String miniAppUnionid;

    /**
     * 小程序SessionKey（AES加密）
     */
    private String miniAppSessionKey;

    /**
     * 微信OpenID（公众号）
     */
    private String wechatOpenid;

    /**
     * 微信UnionID
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
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别
     * 0-未知, 1-男, 2-女
     */
    private Byte gender;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 国家
     */
    private String country;

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
     * mini_program-小程序, app-App应用, web-网站, h5-H5应用, api-API接口
     */
    private String sourceType;

    /**
     * 来源渠道
     */
    private String sourceChannel;

    /**
     * 来源场景码
     */
    private Integer sourceScene;

    /**
     * 用户标签（逗号分隔）
     * 示例: VIP会员,活跃用户
     */
    private String userTags;

    /**
     * 用户备注（商户备注）
     */
    private String userRemark;

    /**
     * 用户等级
     * normal-普通, vip-VIP, svip-SVIP等
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
     * 扩展资料（JSON格式）
     * 示例: {"custom_field1": "value1"}
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object extProfile;

    /**
     * 偏好配置（JSON格式）
     * 示例: {"notification": true, "language": "zh-CN"}
     */
    @TableField(typeHandler = com.duda.common.database.handler.JsonTypeHandler.class)
    private Object preferenceConfig;

    /**
     * 状态
     * 1-正常, 2-禁用, 3-已删除
     */
    private Byte status;

    /**
     * 关注状态
     * 0-未关注, 1-已关注
     */
    private Byte followStatus;

    /**
     * 订阅状态
     * 0-未订阅, 1-已订阅
     */
    private Byte subscribeStatus;

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
    private Date lastVisitTime;

    /**
     * 最后访问IP
     */
    private String lastVisitIp;

    /**
     * 首次访问时间
     */
    private Date firstVisitTime;

    /**
     * 绑定时间（加入商户时间）
     */
    private Date bindTime;

    /**
     * 解绑时间
     */
    private Date unbindTime;

    /**
     * 最后下单时间
     */
    private Date lastOrderTime;

    /**
     * 最后支付时间
     */
    private Date lastPayTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标记
     * 0-正常, 1-已删除
     */
    private Byte deleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
