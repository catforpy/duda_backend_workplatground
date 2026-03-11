package com.duda.user.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户第三方账号绑定PO
 *
 * 存储用户与第三方平台的绑定关系
 * 与users表是一对多关系
 *
 * @author DudaNexus
 * @since 2026-03-11
 */
@Data
@TableName("user_third_parties")
public class UserThirdPartyPO {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联users表）
     */
    private Long userId;

    /**
     * 第三方平台：wechat-微信, qq-QQ, alipay-支付宝, weibo-微博
     */
    private String platform;

    /**
     * 第三方OpenID
     */
    private String openid;

    /**
     * 第三方UnionID
     */
    private String unionid;

    /**
     * 第三方昵称
     */
    private String nickname;

    /**
     * 第三方头像
     */
    private String avatarUrl;

    /**
     * 绑定时间
     */
    private LocalDateTime bindTime;

    /**
     * 解绑时间
     */
    private LocalDateTime unbindTime;

    /**
     * 状态：1-已绑定, 0-已解绑
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
