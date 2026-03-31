-- =============================================
-- 多租户SAAS模式数据库设计
-- 创建时间: 2026-03-22
-- 说明: 商户、商户用户映射、开放API相关表
-- =============================================

USE duda_nexus;

-- =============================================
-- 1. 商户信息表 (merchants)
-- =============================================
-- 说明：存储所有商户（服务商）的基本信息
DROP TABLE IF EXISTS `merchants`;
CREATE TABLE `merchants` (
  `id` BIGINT NOT NULL COMMENT '商户ID（雪花算法生成）',
  `merchant_code` VARCHAR(50) NOT NULL COMMENT '商户编码（全局唯一，如：M001）',
  `merchant_name` VARCHAR(200) NOT NULL COMMENT '商户名称',
  `merchant_type` VARCHAR(20) NOT NULL DEFAULT 'mini_program' COMMENT '商户类型：mini_program-小程序, app-移动应用, web-网站, h5-H5应用',

  -- 联系信息
  `contact_person` VARCHAR(100) COMMENT '联系人',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `contact_email` VARCHAR(100) COMMENT '联系邮箱',

  -- 公司信息
  `company_id` BIGINT COMMENT '关联公司ID（如果有）',
  `company_name` VARCHAR(200) COMMENT '公司名称',
  `license_no` VARCHAR(100) COMMENT '营业执照号',

  -- 地址信息
  `province` VARCHAR(50) COMMENT '省份',
  `city` VARCHAR(50) COMMENT '城市',
  `district` VARCHAR(50) COMMENT '区县',
  `address` VARCHAR(255) COMMENT '详细地址',

  -- 微信小程序配置
  `mini_app_appid` VARCHAR(100) COMMENT '微信小程序AppID',
  `mini_app_secret` VARCHAR(500) COMMENT '微信小程序AppSecret（AES加密存储）',
  `mini_app_name` VARCHAR(200) COMMENT '小程序名称',
  `mini_app_avatar` VARCHAR(500) COMMENT '小程序头像',

  -- 微信支付配置
  `wechat_pay_mch_id` VARCHAR(100) COMMENT '微信支付商户号',
  `wechat_pay_api_key` VARCHAR(500) COMMENT '微信支付API密钥（AES加密）',
  `wechat_pay_cert_path` VARCHAR(500) COMMENT '微信支付证书路径',
  `wechat_pay_sub_mch_id` VARCHAR(100) COMMENT '微信支付子商户号（分账用）',

  -- 分账配置
  `profit_sharing_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用分账：0-否 1-是',
  `profit_sharing_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '平台分账比例（0.10 = 10%）',
  `profit_sharing_account` VARCHAR(200) COMMENT '平台分账账号',

  -- 业务配置
  `business_scope` TEXT COMMENT '经营范围',
  `industry` VARCHAR(100) COMMENT '所属行业',
  `tags` VARCHAR(500) COMMENT '商户标签（逗号分隔）',

  -- 状态管理
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待审核, active-已激活, suspended-暂停, deleted-已删除',
  `audit_status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '审核状态：pending-待审核, approved-已通过, rejected-已拒绝',
  `audit_time` DATETIME COMMENT '审核时间',
  `audit_by` BIGINT COMMENT '审核人ID',
  `audit_remark` VARCHAR(500) COMMENT '审核备注',

  -- 时间信息
  `expire_time` DATE COMMENT '服务过期时间',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `remark` VARCHAR(500) COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) COMMENT '创建人',
  `update_by` VARCHAR(50) COMMENT '更新人',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_code` (`merchant_code`),
  UNIQUE KEY `uk_mini_app_appid` (`mini_app_appid`),
  UNIQUE KEY `uk_wechat_pay_mch_id` (`wechat_pay_mch_id`),
  KEY `idx_merchant_name` (`merchant_name`),
  KEY `idx_status` (`status`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户信息表';


-- =============================================
-- 2. 商户用户映射表 (merchant_users)
-- =============================================
-- 说明：核心表！实现"一个用户，在不同商户有不同虚拟ID"的映射关系
-- 这是多租户SAAS模式的核心设计
DROP TABLE IF EXISTS `merchant_users`;
CREATE TABLE `merchant_users` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `merchant_id` BIGINT NOT NULL COMMENT '商户ID（关联merchants表）',
  `platform_user_id` BIGINT NOT NULL COMMENT '平台用户ID（关联users_XX表，全局唯一）',
  `merchant_user_id` VARCHAR(50) NOT NULL COMMENT '商户小程序内的用户ID（虚拟ID，如：M1001_001）',

  -- 微信相关（每个商户的小程序OpenID不同）
  `mini_app_openid` VARCHAR(100) COMMENT '该商户小程序的OpenID',
  `mini_app_unionid` VARCHAR(100) COMMENT '微信UnionID（跨小程序唯一，可用于识别同一用户）',
  `mini_app_session_key` VARCHAR(500) COMMENT '微信SessionKey（AES加密）',

  -- 商户小程序内的用户信息
  `nickname` VARCHAR(50) COMMENT '商户小程序内的昵称',
  `avatar` VARCHAR(255) COMMENT '商户小程序内的头像',
  `gender` TINYINT COMMENT '性别：0-未知 1-男 2-女',
  `country` VARCHAR(50) COMMENT '国家',
  `province` VARCHAR(50) COMMENT '省份',
  `city` VARCHAR(50) COMMENT '城市',
  `language` VARCHAR(20) COMMENT '语言',

  -- 用户来源
  `source_type` VARCHAR(20) NOT NULL DEFAULT 'mini_program' COMMENT '来源类型：mini_program-小程序, app-App, web-Web, h5-H5',
  `source_channel` VARCHAR(100) COMMENT '来源渠道',

  -- 用户标签（商户自定义）
  `user_tags` VARCHAR(500) COMMENT '用户标签（逗号分隔，商户可自定义）',
  `user_remark` VARCHAR(500) COMMENT '用户备注（商户可自定义）',
  `user_level` VARCHAR(20) DEFAULT 'normal' COMMENT '用户等级：normal-普通, vip-VIP, svip-SVIP（商户可自定义）',

  -- 状态管理
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常 2-禁用 3-已删除',
  `follow_status` TINYINT DEFAULT 0 COMMENT '关注状态：0-未关注 1-已关注',

  -- 访问统计
  `visit_count` INT DEFAULT 0 COMMENT '访问次数',
  `last_visit_time` DATETIME COMMENT '最后访问时间',
  `first_visit_time` DATETIME COMMENT '首次访问时间',

  -- 时间信息
  `bind_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间（用户首次访问该商户小程序）',
  `unbind_time` DATETIME COMMENT '解绑时间',
  `remark` VARCHAR(500) COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_merchant_user` (`merchant_id`, `merchant_user_id`),
  UNIQUE KEY `uk_merchant_openid` (`merchant_id`, `mini_app_openid`),
  KEY `idx_platform_user` (`platform_user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_unionid` (`mini_app_unionid`),
  KEY `idx_status` (`status`),
  KEY `idx_bind_time` (`bind_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户用户映射表（多租户核心表）';


-- =============================================
-- 3. 开放API密钥表 (api_keys)
-- =============================================
-- 说明：为PHP后端等第三方系统提供API访问密钥
DROP TABLE IF EXISTS `api_keys`;
CREATE TABLE `api_keys` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `app_id` VARCHAR(50) NOT NULL COMMENT '应用ID（全局唯一，如：php_backend_001）',
  `app_secret` VARCHAR(128) NOT NULL COMMENT '应用密钥（SHA256加密存储）',
  `app_name` VARCHAR(200) NOT NULL COMMENT '应用名称',

  -- 应用类型
  `app_type` VARCHAR(20) NOT NULL DEFAULT 'backend' COMMENT '应用类型：backend-后端系统, frontend-前端应用, third_party-第三方应用',
  `app_owner` VARCHAR(100) COMMENT '应用所有者',

  -- 权限控制（JSON格式存储权限列表）
  `permissions` JSON COMMENT '权限列表：["user:read", "user:info", "auth:check-phone", "auth:bind-account"]',

  -- 限流配置
  `rate_limit_per_second` INT DEFAULT 100 COMMENT '每秒请求次数限制',
  `rate_limit_per_minute` INT DEFAULT 1000 COMMENT '每分钟请求次数限制',
  `rate_limit_per_hour` INT DEFAULT 10000 COMMENT '每小时请求次数限制',

  -- IP白名单（JSON格式）
  `ip_whitelist` JSON COMMENT 'IP白名单：["192.168.1.100", "192.168.1.101"]',
  `ip_blacklist` JSON COMMENT 'IP黑名单：["xxx.xxx.xxx.xxx"]',

  -- 访问统计
  `total_requests` BIGINT DEFAULT 0 COMMENT '总请求次数',
  `success_requests` BIGINT DEFAULT 0 COMMENT '成功请求次数',
  `failed_requests` BIGINT DEFAULT 0 COMMENT '失败请求次数',
  `last_request_time` DATETIME COMMENT '最后请求时间',
  `last_request_ip` VARCHAR(50) COMMENT '最后请求IP',

  -- 状态管理
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用 2-禁用',
  `audit_status` VARCHAR(20) DEFAULT 'approved' COMMENT '审核状态：pending-待审核, approved-已通过, rejected-已拒绝',

  -- 有效期
  `expire_time` DATETIME COMMENT '过期时间（NULL表示永不过期）',

  -- 备注
  `description` TEXT COMMENT '应用描述',
  `remark` VARCHAR(500) COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) COMMENT '创建人',
  `update_by` VARCHAR(50) COMMENT '更新人',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_id` (`app_id`),
  KEY `idx_app_type` (`app_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='开放API密钥表';


-- =============================================
-- 4. API调用日志表 (api_call_logs)
-- =============================================
-- 说明：记录所有API调用日志，用于监控和审计
DROP TABLE IF EXISTS `api_call_logs`;
CREATE TABLE `api_call_logs` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `app_id` VARCHAR(50) NOT NULL COMMENT '应用ID',
  `request_id` VARCHAR(50) NOT NULL COMMENT '请求ID（全局唯一，用于追踪）',

  -- 请求信息
  `request_method` VARCHAR(10) NOT NULL COMMENT '请求方法：GET/POST/PUT/DELETE',
  `request_path` VARCHAR(500) NOT NULL COMMENT '请求路径',
  `request_params` TEXT COMMENT '请求参数（JSON格式）',
  `request_body` TEXT COMMENT '请求体（JSON格式）',
  `request_headers` TEXT COMMENT '请求头（JSON格式）',

  -- 响应信息
  `response_status` INT NOT NULL COMMENT 'HTTP状态码：200/400/401/403/404/500等',
  `response_body` TEXT COMMENT '响应体（JSON格式）',
  `response_time` INT COMMENT '响应时间（毫秒）',

  -- 客户端信息
  `client_ip` VARCHAR(50) COMMENT '客户端IP',
  `user_agent` VARCHAR(500) COMMENT 'User-Agent',
  `device_type` VARCHAR(20) COMMENT '设备类型',

  -- 结果
  `call_result` VARCHAR(20) NOT NULL COMMENT '调用结果：success-成功, failed-失败, error-错误',
  `error_message` VARCHAR(500) COMMENT '错误信息',

  -- 时间信息
  `request_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
  `response_time` DATETIME COMMENT '响应时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_request_id` (`request_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_request_time` (`request_time`),
  KEY `idx_call_result` (`call_result`),
  KEY `idx_client_ip` (`client_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API调用日志表';


-- =============================================
-- 5. 跨平台账户绑定表 (cross_platform_bindings)
-- =============================================
-- 说明：可选！用于Java后端和PHP后端的账户绑定
DROP TABLE IF EXISTS `cross_platform_bindings`;
CREATE TABLE `cross_platform_bindings` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号（全局唯一）',

  -- Java后端用户信息
  `java_user_id` BIGINT NOT NULL COMMENT 'Java后端用户ID',
  `java_user_shard` TINYINT NOT NULL COMMENT 'Java后端用户分片ID',

  -- PHP后端用户信息
  `php_user_id` BIGINT NOT NULL COMMENT 'PHP后端用户ID',
  `php_user_shard` TINYINT COMMENT 'PHP后端用户分片ID（如果有）',

  -- 绑定信息
  `bind_type` VARCHAR(20) NOT NULL DEFAULT 'auto' COMMENT '绑定类型：auto-自动绑定, manual-手动绑定',
  `bind_source` VARCHAR(20) NOT NULL COMMENT '绑定来源：java-Java后端, php-PHP后端',
  `bind_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `verify_code` VARCHAR(10) COMMENT '验证码',
  `verify_ip` VARCHAR(50) COMMENT '验证IP',

  -- 状态
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-已绑定 2-已解绑',
  `unbind_time` DATETIME COMMENT '解绑时间',
  `unbind_reason` VARCHAR(200) COMMENT '解绑原因',

  -- 备注
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_java_user_id` (`java_user_id`),
  KEY `idx_php_user_id` (`php_user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='跨平台账户绑定表（可选）';


-- =============================================
-- 初始化测试数据
-- =============================================

-- 初始化商户数据
INSERT INTO `merchants` (
  `id`, `merchant_code`, `merchant_name`, `merchant_type`,
  `mini_app_appid`, `mini_app_name`, `contact_person`, `contact_phone`,
  `profit_sharing_rate`, `status`, `audit_status`
) VALUES
(1001, 'M001', '测试商户A', 'mini_program',
 'wx_appid_001', '测试小程序A', '张三', '13900139001',
 0.10, 'active', 'approved'),
(1002, 'M002', '测试商户B', 'mini_program',
 'wx_appid_002', '测试小程序B', '李四', '13900139002',
 0.10, 'active', 'approved'),
(1003, 'M003', '测试商户C', 'mini_program',
 'wx_appid_003', '测试小程序C', '王五', '13900139003',
 0.10, 'active', 'approved');

-- 初始化API密钥数据
INSERT INTO `api_keys` (
  `id`, `app_id`, `app_secret`, `app_name`, `app_type`, `permissions`,
  `rate_limit_per_second`, `status`, `audit_status`
) VALUES
(1, 'php_backend_001', SHA2('php_secret_key_2026', 256), 'PHP后端系统', 'backend',
 '["user:read", "user:info", "auth:check-phone", "auth:bind-account"]',
 100, 1, 'approved');

-- =============================================
-- 验证表结构
-- =============================================
SELECT '多租户SAAS数据库创建成功！' AS message;
SHOW TABLES LIKE 'merchant%' OR SHOW TABLES LIKE 'api_%' OR SHOW TABLES LIKE 'cross_platform%';
