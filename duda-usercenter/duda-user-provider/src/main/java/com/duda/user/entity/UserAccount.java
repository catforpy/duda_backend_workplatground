package com.duda.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户登录账号实体
 *
 * 支持多种登录方式：
 * - username: 都达网账号（用户名+密码）
 * - email: 邮箱登录（邮箱+密码）
 * - phone: 手机号登录（手机号+验证码）
 * - wechat: 微信登录（OpenID）
 * - qq: QQ登录（OpenID）
 * - alipay: 支付宝登录
 * - weibo: 微博登录
 *
 * @author DudaNexus
 * @since 2026-03-12
 */
@Data
@TableName("user_accounts")
@Schema(description = "用户登录账号实体")
public class UserAccount {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 关联的用户ID
     */
    @Schema(description = "关联的用户ID")
    private Long userId;

    /**
     * 用户分片ID
     */
    @Schema(description = "用户分片ID")
    private Integer userShard;

    /**
     * 登录方式
     * username: 都达网账号
     * email: 邮箱
     * phone: 手机号
     * wechat: 微信
     * qq: QQ
     * alipay: 支付宝
     * weibo: 微博
     */
    @Schema(description = "登录方式")
    private String loginType;

    /**
     * 登录账号
     * 根据login_type不同，存储内容不同：
     * - username: 用户名
     * - email: 邮箱地址
     * - phone: 手机号
     * - wechat: 微信OpenID
     * - qq: QQ OpenID
     * - alipay: 支付宝用户ID
     * - weibo: 微博UID
     */
    @Schema(description = "登录账号")
    private String loginAccount;

    /**
     * 密码
     * 仅username和email类型有密码，其他类型为NULL
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 是否已验证
     */
    @Schema(description = "是否已验证")
    private Boolean verified;

    /**
     * 是否为主账号
     * 一个用户可以有多个登录方式，其中一个标记为主账号
     */
    @Schema(description = "是否为主账号")
    private Boolean isPrimary;

    /**
     * 账号状态
     * active: 激活
     * inactive: 未激活
     * suspended: 暂停
     * deleted: 已删除
     */
    @Schema(description = "账号状态")
    private String status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
