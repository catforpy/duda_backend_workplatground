-- =============================================
-- 多租户SAAS模式 - 完整数据库表创建脚本
-- =============================================
-- 创建时间: 2026-03-22
-- 版本: v1.1
-- 说明: 创建商户管理、商户用户映射、开放API相关表
--
-- 设计原则:
-- 1. 充分考虑扩展性，预留扩展字段
-- 2. 使用JSON字段存储灵活配置
-- 3. 合理的索引设计，兼顾查询性能
-- 4. 详细的字段注释，便于维护
-- 5. 软删除设计，保留数据历史
--
-- 密钥管理职责划分:
-- - open_api_keys: 外部系统调用我们的API时的认证（我们是服务方）
-- - merchants表中的微信支付密钥: 我们调用微信支付API（我们是调用方）
-- - user_api_keys表: 用户个人云存储密钥（用户调用云存储API）
-- - 系统OSS密钥: 存储在application.yml配置文件中
-- =============================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 1. 商户信息表 (merchants)
-- =============================================
-- 说明: 存储所有商户（服务商）的基本信息、微信配置、支付配置等
-- 核心设计: 一个公司可以有多个商户（小程序）
DROP TABLE IF EXISTS `merchants`;
CREATE TABLE `merchants` (
  -- ========== 主键 ==========
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法生成，全局唯一）',

  -- ========== 基本信息 ==========
  `merchant_code` VARCHAR(50) NOT NULL COMMENT '商户编码（全局唯一，如：M001、M002）',
  `merchant_name` VARCHAR(200) NOT NULL COMMENT '商户名称',
  `merchant_short_name` VARCHAR(100) COMMENT '商户简称',
  `merchant_type` VARCHAR(20) NOT NULL DEFAULT 'mini_program' COMMENT '商户类型：mini_program-小程序, app-移动应用, web-网站, h5-H5应用, desktop-桌面应用',
  `merchant_category` VARCHAR(50) COMMENT '商户分类：retail-零售, catering-餐饮, service-服务, education-教育, medical-医疗, other-其他',

  -- ========== 联系信息 ==========
  `contact_person` VARCHAR(100) COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) COMMENT '联系人电话',
  `contact_email` VARCHAR(100) COMMENT '联系人邮箱',
  `contact_wechat` VARCHAR(100) COMMENT '联系人微信号',
  `contact_qq` VARCHAR(20) COMMENT '联系人QQ号',

  -- ========== 公司信息（关联companies表） ==========
  `company_id` BIGINT COMMENT '关联公司ID（user_nexus.companies表，如果有）',
  `company_name` VARCHAR(200) COMMENT '公司名称（冗余字段，便于查询）',
  `license_no` VARCHAR(100) COMMENT '营业执照号',
  `license_url` VARCHAR(500) COMMENT '营业执照图片URL',
  `legal_person` VARCHAR(100) COMMENT '法人代表姓名',
  `legal_person_id` VARCHAR(18) COMMENT '法人身份证号',

  -- ========== 地址信息 ==========
  `province` VARCHAR(50) COMMENT '省份',
  `province_code` VARCHAR(20) COMMENT '省份代码（国标行政区划代码）',
  `city` VARCHAR(50) COMMENT '城市',
  `city_code` VARCHAR(20) COMMENT '城市代码',
  `district` VARCHAR(50) COMMENT '区县',
  `district_code` VARCHAR(20) COMMENT '区县代码',
  `address` VARCHAR(255) COMMENT '详细地址',
  `longitude` DECIMAL(10,7) COMMENT '经度',
  `latitude` DECIMAL(10,7) COMMENT '纬度',

  -- ========== 微信小程序配置 ==========
  `mini_app_appid` VARCHAR(100) COMMENT '微信小程序AppID',
  `mini_app_secret` VARCHAR(500) COMMENT '微信小程序AppSecret（AES加密存储）',
  `mini_app_name` VARCHAR(200) COMMENT '小程序名称',
  `mini_app_avatar` VARCHAR(500) COMMENT '小程序头像URL',
  `mini_app_qrcode` VARCHAR(500) COMMENT '小程序二维码URL',
  `mini_app_intro` TEXT COMMENT '小程序简介',
  `mini_app_tags` VARCHAR(500) COMMENT '小程序标签（逗号分隔）',

  -- ========== 微信支付配置（专项存储）==========
  -- 说明: 我们调用微信支付API时需要的密钥（我们是调用方，微信是服务方）
  `wechat_pay_mch_id` VARCHAR(100) COMMENT '微信支付商户号',
  `wechat_pay_api_key` VARCHAR(500) COMMENT '微信支付API密钥（AES加密）',
  `wechat_pay_cert_path` VARCHAR(500) COMMENT '微信支付证书路径',
  `wechat_pay_cert_p12_path` VARCHAR(500) COMMENT '微信支付证书p12路径',
  `wechat_pay_sub_mch_id` VARCHAR(100) COMMENT '微信支付子商户号（分账用）',
  `wechat_pay_key` VARCHAR(500) COMMENT '微信支付Key（AES加密）',
  `wechat_pay_serial_no` VARCHAR(100) COMMENT '微信支付证书序列号',
  `wechat_pay_notify_url` VARCHAR(500) COMMENT '微信支付回调地址',
  `wechat_pay_refund_notify_url` VARCHAR(500) COMMENT '微信退款回调地址',

  -- ========== 支付宝支付配置（专项存储，预留）==========
  -- 说明: 我们调用支付宝API时需要的密钥（我们是调用方，支付宝是服务方）
  `alipay_app_id` VARCHAR(100) COMMENT '支付宝应用ID',
  `alipay_private_key` TEXT COMMENT '支付宝应用私钥（AES加密）',
  `alipay_public_key` TEXT COMMENT '支付宝公钥（AES加密）',
  `alipay_notify_url` VARCHAR(500) COMMENT '支付宝回调地址',

  -- ========== 分账配置 ==========
  `profit_sharing_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用分账：0-否 1-是',
  `profit_sharing_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '平台分账比例（0.10 = 10%，范围0.00-1.00）',
  `profit_sharing_account` VARCHAR(200) COMMENT '平台分账账号',
  `profit_sharing_name` VARCHAR(200) COMMENT '平台分账账户名称',
  `settlement_cycle` VARCHAR(20) DEFAULT 'T+1' COMMENT '结算周期：T+0-实时, T+1-次日, T+7-周结, M+1-月结',
  `min_settlement_amount` DECIMAL(10,2) DEFAULT 100.00 COMMENT '最低结算金额（元）',

  -- ========== 业务配置 ==========
  `business_scope` TEXT COMMENT '经营范围',
  `industry` VARCHAR(100) COMMENT '所属行业',
  `tags` VARCHAR(500) COMMENT '商户标签（逗号分隔）',
  `keywords` VARCHAR(500) COMMENT '关键词（逗号分隔，便于搜索）',
  `description` TEXT COMMENT '商户详细描述',

  -- ========== 扩展配置（JSON格式，便于灵活扩展） ==========
  `ext_config` JSON COMMENT '扩展配置（JSON格式）：{"custom_field1": "value1", "custom_field2": "value2"}',
  `business_hours` JSON COMMENT '营业时间（JSON格式）：{"monday": "09:00-18:00", "tuesday": "09:00-18:00"}',
  `service_config` JSON COMMENT '服务配置（JSON格式）：{"auto_reply": true, "welcome_message": "欢迎光临"}',

  -- ========== 状态管理 ==========
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待审核, active-已激活, suspended-暂停, deleted-已删除',
  `audit_status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '审核状态：pending-待审核, approved-已通过, rejected-已拒绝',
  `audit_time` DATETIME COMMENT '审核时间',
  `audit_by` BIGINT COMMENT '审核人ID',
  `audit_remark` VARCHAR(500) COMMENT '审核备注',

  -- ========== 时间信息 ==========
  `register_time` DATETIME COMMENT '注册时间',
  `activate_time` DATETIME COMMENT '激活时间',
  `expire_time` DATETIME COMMENT '服务过期时间（NULL表示永久）',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',

  -- ========== 统计信息 ==========
  `total_users` INT DEFAULT 0 COMMENT '总用户数',
  `total_orders` INT DEFAULT 0 COMMENT '总订单数',
  `total_revenue` DECIMAL(12,2) DEFAULT 0.00 COMMENT '总营收（元）',
  `visit_count` BIGINT DEFAULT 0 COMMENT '访问次数',

  -- ========== 其他信息 ==========
  `remark` VARCHAR(500) COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) COMMENT '创建人',
  `update_by` VARCHAR(50) COMMENT '更新人',
  `version` INT DEFAULT 0 COMMENT '乐观锁版本号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_code` (`merchant_code`),
  UNIQUE KEY `uk_mini_app_appid` (`mini_app_appid`),
  UNIQUE KEY `uk_wechat_pay_mch_id` (`wechat_pay_mch_id`),
  KEY `idx_merchant_name` (`merchant_name`),
  KEY `idx_merchant_type` (`merchant_type`),
  KEY `idx_status` (`status`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_contact_phone` (`contact_phone`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_province_city` (`province`, `city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户信息表';


-- =============================================
-- 2. 商户用户映射表 (merchant_users)
-- =============================================
-- 说明: 核心表！实现"一个用户，在不同商户有不同虚拟ID"的映射关系
-- 多租户SAAS模式的核心设计
DROP TABLE IF EXISTS `merchant_users`;
CREATE TABLE `merchant_users` (
  -- ========== 主键 ==========
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',

  -- ========== 关联关系 ==========
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID（关联merchants表）',
  `platform_user_id` BIGINT NOT NULL COMMENT '平台用户ID（关联user_nexus.users_XX表，全局唯一）',
  `platform_user_shard` TINYINT NOT NULL COMMENT '平台用户分片ID（0-99，用于快速定位users_XX表）',
  `merchant_user_id` VARCHAR(50) NOT NULL COMMENT '商户小程序内的用户ID（虚拟ID，如：M1001_001）',

  -- ========== 微信相关（每个商户的小程序OpenID不同） ==========
  `mini_app_openid` VARCHAR(100) COMMENT '该商户小程序的OpenID（每个商户不同）',
  `mini_app_unionid` VARCHAR(100) COMMENT '微信UnionID（跨小程序唯一，可用于识别同一用户）',
  `mini_app_session_key` VARCHAR(500) COMMENT '微信SessionKey（AES加密）',
  `wechat_openid` VARCHAR(100) COMMENT '微信OpenID（兼容字段）',
  `wechat_unionid` VARCHAR(100) COMMENT '微信UnionID（兼容字段）',

  -- ========== 支付宝相关（预留） ==========
  `alipay_user_id` VARCHAR(100) COMMENT '支付宝用户ID',
  `alipay_openid` VARCHAR(100) COMMENT '支付宝OpenID',

  -- ========== 商户小程序内的用户信息 ==========
  `nickname` VARCHAR(50) COMMENT '商户小程序内的昵称',
  `avatar` VARCHAR(255) COMMENT '商户小程序内的头像',
  `gender` TINYINT COMMENT '性别：0-未知 1-男 2-女',
  `birthday` DATE COMMENT '生日',
  `country` VARCHAR(50) COMMENT '国家',
  `province` VARCHAR(50) COMMENT '省份',
  `city` VARCHAR(50) COMMENT '城市',
  `language` VARCHAR(20) COMMENT '语言',

  -- ========== 用户来源 ==========
  `source_type` VARCHAR(20) NOT NULL DEFAULT 'mini_program' COMMENT '来源类型：mini_program-小程序, app-App, web-Web, h5-H5, api-API',
  `source_channel` VARCHAR(100) COMMENT '来源渠道（如：微信分享、搜索引擎等）',
  `source_scene` INT COMMENT '场景值（小程序场景值）',

  -- ========== 用户标签（商户自定义） ==========
  `user_tags` VARCHAR(500) COMMENT '用户标签（逗号分隔，商户可自定义，如：VIP、新客户、老客户）',
  `user_remark` VARCHAR(500) COMMENT '用户备注（商户可自定义）',
  `user_level` VARCHAR(20) DEFAULT 'normal' COMMENT '用户等级：normal-普通, vip-VIP, svip-SVIP（商户可自定义）',
  `user_group` VARCHAR(50) COMMENT '用户分组（商户可自定义，如：活跃用户、沉睡用户）',
  `user_score` INT DEFAULT 0 COMMENT '用户积分（商户可自定义）',
  `user_balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '用户余额（商户可自定义）',

  -- ========== 扩展配置（JSON格式） ==========
  `ext_profile` JSON COMMENT '扩展用户信息（JSON格式）：{"custom_field1": "value1"}',
  `preference_config` JSON COMMENT '偏好配置（JSON格式）：{"notification": true, "language": "zh-CN"}',

  -- ========== 状态管理 ==========
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 2-禁用 3-已删除',
  `follow_status` TINYINT DEFAULT 0 COMMENT '关注状态：0-未关注 1-已关注',
  `subscribe_status` TINYINT DEFAULT 0 COMMENT '订阅状态：0-未订阅 1-已订阅',

  -- ========== 访问统计 ==========
  `visit_count` INT DEFAULT 0 COMMENT '访问次数',
  `order_count` INT DEFAULT 0 COMMENT '订单数量',
  `consumption_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '消费金额（元）',
  `last_visit_time` DATETIME COMMENT '最后访问时间',
  `last_visit_ip` VARCHAR(50) COMMENT '最后访问IP',
  `first_visit_time` DATETIME COMMENT '首次访问时间',

  -- ========== 时间信息 ==========
  `bind_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间（用户首次访问该商户小程序）',
  `unbind_time` DATETIME COMMENT '解绑时间',
  `last_order_time` DATETIME COMMENT '最后下单时间',
  `last_pay_time` DATETIME COMMENT '最后支付时间',

  -- ========== 其他信息 ==========
  `remark` VARCHAR(500) COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_merchant_user` (`merchant_id`, `merchant_user_id`),
  UNIQUE KEY `uk_merchant_openid` (`merchant_id`, `mini_app_openid`),
  KEY `idx_platform_user` (`platform_user_id`),
  KEY `idx_platform_user_shard` (`platform_user_shard`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_unionid` (`mini_app_unionid`),
  KEY `idx_wechat_unionid` (`wechat_unionid`),
  KEY `idx_status` (`status`),
  KEY `idx_user_level` (`user_level`),
  KEY `idx_bind_time` (`bind_time`),
  KEY `idx_last_visit_time` (`last_visit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户用户映射表（多租户核心表）';


-- =============================================
-- 3. 开放API密钥表 (open_api_keys)
-- =============================================
-- 说明: 为PHP后端等第三方系统提供API访问密钥
-- 职责: 外部系统调用我们的API时的认证（我们是服务方，外部系统是调用方）
DROP TABLE IF EXISTS `open_api_keys`;
CREATE TABLE `open_api_keys` (
  -- ========== 主键 ==========
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',

  -- ========== 基本信息 ==========
  `app_id` VARCHAR(50) NOT NULL COMMENT '应用ID（全局唯一，如：php_backend_001、java_backend_002）',
  `app_secret` VARCHAR(128) NOT NULL COMMENT '应用密钥（SHA256加密存储）',
  `app_name` VARCHAR(200) NOT NULL COMMENT '应用名称',
  `app_code` VARCHAR(50) COMMENT '应用代码（英文标识）',

  -- ========== 应用类型 ==========
  `app_type` VARCHAR(20) NOT NULL DEFAULT 'backend' COMMENT '应用类型：backend-后端系统, frontend-前端应用, third_party-第三方应用, mobile-移动应用',
  `app_category` VARCHAR(50) COMMENT '应用分类：internal-内部, external-外部, partner-合作伙伴',
  `app_owner` VARCHAR(100) COMMENT '应用所有者',
  `app_owner_id` BIGINT COMMENT '应用所有者ID（关联user_nexus.users_XX表）',

  -- ========== 应用描述 ==========
  `app_description` TEXT COMMENT '应用详细描述',
  `app_icon` VARCHAR(500) COMMENT '应用图标URL',
  `app_url` VARCHAR(500) COMMENT '应用地址',
  `app_callback_url` VARCHAR(500) COMMENT '应用回调地址',

  -- ========== 权限控制（JSON格式存储权限列表） ==========
  `permissions` JSON COMMENT '权限列表（JSON格式）：["user:read", "user:info", "auth:check-phone", "auth:bind-account"]',
  `permission_scope` VARCHAR(20) DEFAULT 'read' COMMENT '权限范围：read-只读, write-读写, admin-管理员',
  `allowed_resources` JSON COMMENT '允许访问的资源（JSON格式）：["users", "orders", "products"]',
  `denied_resources` JSON COMMENT '禁止访问的资源（JSON格式）：["admin", "config"]',

  -- ========== 限流配置 ==========
  `rate_limit_enabled` TINYINT DEFAULT 1 COMMENT '是否启用限流：0-否 1-是',
  `rate_limit_per_second` INT DEFAULT 100 COMMENT '每秒请求次数限制',
  `rate_limit_per_minute` INT DEFAULT 1000 COMMENT '每分钟请求次数限制',
  `rate_limit_per_hour` INT DEFAULT 10000 COMMENT '每小时请求次数限制',
  `rate_limit_per_day` INT DEFAULT 100000 COMMENT '每天请求次数限制',
  `burst_limit` INT DEFAULT 200 COMMENT '突发流量限制',

  -- ========== IP白名单/黑名单（JSON格式） ==========
  `ip_whitelist_enabled` TINYINT DEFAULT 0 COMMENT '是否启用IP白名单：0-否 1-是',
  `ip_whitelist` JSON COMMENT 'IP白名单（JSON格式）：["192.168.1.100", "192.168.1.101", "192.168.1.*"]',
  `ip_blacklist` JSON COMMENT 'IP黑名单（JSON格式）：["xxx.xxx.xxx.xxx"]',
  `ip_whitelist_type` VARCHAR(20) DEFAULT 'allow' COMMENT 'IP白名单类型：allow-白名单模式, deny-黑名单模式',

  -- ========== 访问统计 ==========
  `total_requests` BIGINT DEFAULT 0 COMMENT '总请求次数',
  `success_requests` BIGINT DEFAULT 0 COMMENT '成功请求次数',
  `failed_requests` BIGINT DEFAULT 0 COMMENT '失败请求次数',
  `last_request_time` DATETIME COMMENT '最后请求时间',
  `last_request_ip` VARCHAR(50) COMMENT '最后请求IP',
  `last_request_path` VARCHAR(500) COMMENT '最后请求路径',
  `avg_response_time` INT COMMENT '平均响应时间（毫秒）',

  -- ========== 流量统计 ==========
  `today_requests` INT DEFAULT 0 COMMENT '今日请求次数',
  `today_traffic` BIGINT DEFAULT 0 COMMENT '今日流量（字节）',
  `month_requests` INT DEFAULT 0 COMMENT '本月请求次数',
  `month_traffic` BIGINT DEFAULT 0 COMMENT '本月流量（字节）',
  `total_traffic` BIGINT DEFAULT 0 COMMENT '总流量（字节）',

  -- ========== 状态管理 ==========
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用 2-审核中 3-已拒绝',
  `audit_status` VARCHAR(20) DEFAULT 'approved' COMMENT '审核状态：pending-待审核, approved-已通过, rejected-已拒绝',
  `audit_time` DATETIME COMMENT '审核时间',
  `audit_by` BIGINT COMMENT '审核人ID',
  `audit_remark` VARCHAR(500) COMMENT '审核备注',

  -- ========== 有效期 ==========
  `expire_time` DATETIME COMMENT '过期时间（NULL表示永不过期）',
  `expire_notification_sent` TINYINT DEFAULT 0 COMMENT '是否已发送过期通知：0-否 1-是',

  -- ========== 安全配置 ==========
  `secret_rotation_enabled` TINYINT DEFAULT 0 COMMENT '是否启用密钥轮换：0-否 1-是',
  `secret_rotation_days` INT DEFAULT 90 COMMENT '密钥轮换周期（天）',
  `last_secret_rotation` DATETIME COMMENT '上次密钥轮换时间',
  `require_signature` TINYINT DEFAULT 1 COMMENT '是否要求签名：0-否 1-是',
  `signature_algorithm` VARCHAR(20) DEFAULT 'HMAC-SHA256' COMMENT '签名算法：HMAC-SHA256, HMAC-SHA512',

  -- ========== 扩展配置 ==========
  `ext_config` JSON COMMENT '扩展配置（JSON格式）：{"custom_field": "value"}',

  -- ========== 其他信息 ==========
  `description` TEXT COMMENT '应用描述',
  `remark` VARCHAR(500) COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) COMMENT '创建人',
  `update_by` VARCHAR(50) COMMENT '更新人',
  `version` INT DEFAULT 0 COMMENT '乐观锁版本号',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_id` (`app_id`),
  KEY `idx_app_type` (`app_type`),
  KEY `idx_app_category` (`app_category`),
  KEY `idx_status` (`status`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_app_owner_id` (`app_owner_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开放API密钥表（外部系统调用我们的API）';


-- =============================================
-- 4. API调用日志表 (api_call_logs)
-- =============================================
-- 说明: 记录所有API调用日志，用于监控、审计和分析
DROP TABLE IF EXISTS `api_call_logs`;
CREATE TABLE `api_call_logs` (
  -- ========== 主键 ==========
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',

  -- ========== 关联信息 ==========
  `app_id` VARCHAR(50) NOT NULL COMMENT '应用ID（关联open_api_keys表）',
  `request_id` VARCHAR(50) NOT NULL COMMENT '请求ID（全局唯一，用于追踪，如：UUID）',
  `trace_id` VARCHAR(50) COMMENT '追踪ID（用于分布式追踪）',

  -- ========== 请求信息 ==========
  `request_method` VARCHAR(10) NOT NULL COMMENT '请求方法：GET/POST/PUT/DELETE/PATCH',
  `request_protocol` VARCHAR(10) DEFAULT 'HTTPS' COMMENT '请求协议：HTTP/HTTPS',
  `request_domain` VARCHAR(200) COMMENT '请求域名',
  `request_path` VARCHAR(500) NOT NULL COMMENT '请求路径（如：/api/user/info）',
  `request_uri` VARCHAR(1000) COMMENT '完整请求URI（包含查询参数）',
  `request_params` TEXT COMMENT '请求参数（Query参数，JSON格式）',
  `request_body` TEXT COMMENT '请求体（Body，JSON格式）',
  `request_headers` TEXT COMMENT '请求头（JSON格式，敏感信息已脱敏）',
  `request_size` BIGINT COMMENT '请求大小（字节）',

  -- ========== 响应信息 ==========
  `response_status` INT NOT NULL COMMENT 'HTTP状态码：200/400/401/403/404/500等',
  `response_body` TEXT COMMENT '响应体（JSON格式，敏感信息已脱敏）',
  `response_size` BIGINT COMMENT '响应大小（字节）',
  `response_headers` TEXT COMMENT '响应头（JSON格式）',

  -- ========== 客户端信息 ==========
  `client_ip` VARCHAR(50) COMMENT '客户端IP',
  `client_port` INT COMMENT '客户端端口',
  `client_country` VARCHAR(50) COMMENT '客户端国家',
  `client_province` VARCHAR(50) COMMENT '客户端省份',
  `client_city` VARCHAR(50) COMMENT '客户端城市',
  `user_agent` VARCHAR(500) COMMENT 'User-Agent',
  `device_type` VARCHAR(20) COMMENT '设备类型：ios/android/web/miniapp/unknown',
  `browser_type` VARCHAR(50) COMMENT '浏览器类型',
  `os_type` VARCHAR(50) COMMENT '操作系统类型',

  -- ========== 认证信息 ==========
  `auth_type` VARCHAR(20) COMMENT '认证类型：api_key/jwt/oauth2/none',
  `user_id` BIGINT COMMENT '用户ID（如果有）',
  `api_key_id` BIGINT COMMENT 'API密钥ID（关联open_api_keys.id）',

  -- ========== 结果信息 ==========
  `call_result` VARCHAR(20) NOT NULL COMMENT '调用结果：success-成功, failed-失败, error-错误, timeout-超时',
  `error_code` VARCHAR(50) COMMENT '错误码',
  `error_message` VARCHAR(500) COMMENT '错误信息',
  `error_stack` TEXT COMMENT '错误堆栈（仅开发环境）',

  -- ========== 时间信息 ==========
  `request_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
  `response_time` DATETIME COMMENT '响应时间',
  `duration` INT COMMENT '耗时（毫秒）',

  -- ========== 其他信息 ==========
  `remark` VARCHAR(500) COMMENT '备注',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_request_id` (`request_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_request_time` (`request_time`),
  KEY `idx_call_result` (`call_result`),
  KEY `idx_client_ip` (`client_ip`),
  KEY `idx_response_status` (`response_status`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_api_key_id` (`api_key_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API调用日志表';


-- =============================================
-- 5. 跨平台账户绑定表 (cross_platform_bindings)
-- =============================================
-- 说明: 可选表！用于Java后端和PHP后端的账户绑定
DROP TABLE IF EXISTS `cross_platform_bindings`;
CREATE TABLE `cross_platform_bindings` (
  -- ========== 主键 ==========
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',

  -- ========== 基本信息 ==========
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号（全局唯一）',
  `email` VARCHAR(100) COMMENT '邮箱（全局唯一）',

  -- ========== Java后端用户信息 ==========
  `java_user_id` BIGINT NOT NULL COMMENT 'Java后端用户ID（关联user_nexus.users_XX表）',
  `java_user_shard` TINYINT NOT NULL COMMENT 'Java后端用户分片ID（0-99）',
  `java_username` VARCHAR(50) COMMENT 'Java后端用户名（冗余字段）',

  -- ========== PHP后端用户信息 ==========
  `php_user_id` BIGINT NOT NULL COMMENT 'PHP后端用户ID',
  `php_user_shard` TINYINT COMMENT 'PHP后端用户分片ID（如果有）',
  `php_username` VARCHAR(50) COMMENT 'PHP后端用户名（冗余字段）',

  -- ========== 其他平台用户信息（预留） ==========
  `python_user_id` BIGINT COMMENT 'Python后端用户ID（预留）',
  `go_user_id` BIGINT COMMENT 'Go后端用户ID（预留）',
  `node_user_id` BIGINT COMMENT 'Node后端用户ID（预留）',

  -- ========== 绑定信息 ==========
  `bind_type` VARCHAR(20) NOT NULL DEFAULT 'auto' COMMENT '绑定类型：auto-自动绑定, manual-手动绑定, system-系统绑定',
  `bind_source` VARCHAR(20) NOT NULL COMMENT '绑定来源：java-Java后端发起, php-PHP后端发起, admin-管理员操作',
  `bind_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `verify_code` VARCHAR(10) COMMENT '验证码',
  `verify_ip` VARCHAR(50) COMMENT '验证IP',
  `verify_method` VARCHAR(20) COMMENT '验证方式：sms-短信, email-邮件, admin-管理员',

  -- ========== 操作信息 ==========
  `operator_id` BIGINT COMMENT '操作人ID',
  `operator_name` VARCHAR(50) COMMENT '操作人姓名',
  `operation_remark` VARCHAR(500) COMMENT '操作备注',

  -- ========== 状态信息 ==========
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-已绑定 2-已解绑 3-待验证',
  `unbind_time` DATETIME COMMENT '解绑时间',
  `unbind_reason` VARCHAR(200) COMMENT '解绑原因',
  `unbind_operator_id` BIGINT COMMENT '解绑操作人ID',

  -- ========== 同步状态 ==========
  `sync_status` VARCHAR(20) DEFAULT 'synced' COMMENT '同步状态：synced-已同步, pending-待同步, failed-同步失败',
  `last_sync_time` DATETIME COMMENT '最后同步时间',
  `sync_error_message` VARCHAR(500) COMMENT '同步错误信息',

  -- ========== 扩展信息 ==========
  `ext_info` JSON COMMENT '扩展信息（JSON格式）',

  -- ========== 其他信息 ==========
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_java_user_id` (`java_user_id`),
  KEY `idx_php_user_id` (`php_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_bind_time` (`bind_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跨平台账户绑定表（可选）';


-- =============================================
-- 初始化测试数据
-- =============================================

-- 初始化商户数据
INSERT INTO `merchants` (
  `id`, `merchant_code`, `merchant_name`, `merchant_short_name`, `merchant_type`,
  `mini_app_appid`, `mini_app_name`, `contact_person`, `contact_phone`,
  `profit_sharing_rate`, `settlement_cycle`, `status`, `audit_status`
) VALUES
(1001, 'M001', '测试商户A-餐饮', '商户A', 'mini_program',
 'wx_appid_001', '测试餐饮小程序A', '张三', '13900139001',
 0.10, 'T+1', 'active', 'approved'),
(1002, 'M002', '测试商户B-零售', '商户B', 'mini_program',
 'wx_appid_002', '测试零售小程序B', '李四', '13900139002',
 0.10, 'T+1', 'active', 'approved'),
(1003, 'M003', '测试商户C-服务', '商户C', 'mini_program',
 'wx_appid_003', '测试服务小程序C', '王五', '13900139003',
 0.10, 'T+1', 'active', 'approved');

-- 初始化开放API密钥数据
INSERT INTO `open_api_keys` (
  `id`, `app_id`, `app_secret`, `app_name`, `app_type`, `permissions`,
  `rate_limit_per_second`, `ip_whitelist`, `status`, `audit_status`
) VALUES
(1, 'php_backend_001', SHA2('php_secret_key_2026', 256), 'PHP后端系统', 'backend',
 '["user:read", "user:info", "auth:check-phone", "auth:bind-account"]',
 100, '["192.168.1.100", "192.168.1.101"]', 1, 'approved'),
(2, 'test_frontend_001', SHA2('test_secret_key_2026', 256), '测试前端应用', 'frontend',
 '["user:read", "user:info"]',
 50, '[]', 1, 'approved');

-- =============================================
-- 验证表结构
-- =============================================
SET FOREIGN_KEY_CHECKS = 1;

-- 显示创建的表
SELECT '多租户SAAS数据库表创建完成！' AS message;
SELECT
  TABLE_NAME AS '表名',
  TABLE_COMMENT AS '表说明',
  TABLE_ROWS AS '行数'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'duda_nexus'
  AND TABLE_NAME IN ('merchants', 'merchant_users', 'open_api_keys', 'api_call_logs', 'cross_platform_bindings')
ORDER BY TABLE_NAME;

-- 显示表字段统计
SELECT
  TABLE_NAME AS '表名',
  COUNT(*) AS '字段数量'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'duda_nexus'
  AND TABLE_NAME IN ('merchants', 'merchant_users', 'open_api_keys', 'api_call_logs', 'cross_platform_bindings')
GROUP BY TABLE_NAME
ORDER BY TABLE_NAME;
