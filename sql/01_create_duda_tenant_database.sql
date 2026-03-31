-- ===================================================================
-- 租户管理数据库创建脚本
-- ===================================================================
-- 数据库名: duda_tenant
-- 用途: 多租户SaaS平台的租户管理
-- 创建时间: 2026-03-27
-- 执行人员: 用户
-- 验证标准:
--   1. 数据库创建成功
--   2. 7张表创建成功
--   3. 默认租户数据插入成功
--   4. 所有索引创建成功
-- ===================================================================

-- ===================================================================
-- 1. 创建数据库
-- ===================================================================
CREATE DATABASE IF NOT EXISTS `duda_tenant`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci
COMMENT '租户管理数据库';

USE `duda_tenant`;

-- ===================================================================
-- 2. 创建租户表 (tenants)
-- ===================================================================
-- 说明: 租户基础信息表，存储租户的基本配置和状态
DROP TABLE IF EXISTS `tenants`;
CREATE TABLE `tenants` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '租户ID',
  `tenant_code` VARCHAR(50) NOT NULL COMMENT '租户编码（全局唯一，如：DEFAULT、TENANT001）',
  `tenant_name` VARCHAR(200) NOT NULL COMMENT '租户名称',
  `tenant_type` VARCHAR(20) NOT NULL DEFAULT 'trial' COMMENT '租户类型：trial-试用版/basic-基础版/premium-高级版/enterprise-企业版',
  `tenant_status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '租户状态：active-激活/suspended-暂停/expired-过期',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间（NULL表示永久有效）',
  `max_users` INT NOT NULL DEFAULT 100 COMMENT '最大用户数限制',
  `max_storage_size` BIGINT NOT NULL DEFAULT 10737418240 COMMENT '最大存储容量（字节，默认10GB）',
  `max_api_calls_per_day` INT NOT NULL DEFAULT 10000 COMMENT '每日最大API调用次数',
  `contact_person` VARCHAR(100) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
  `extend_fields` JSON DEFAULT NULL COMMENT '扩展字段（JSON格式，用于存储自定义配置）',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号（乐观锁）',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_code`),
  INDEX `idx_tenant_status` (`tenant_status`),
  INDEX `idx_tenant_type` (`tenant_type`),
  INDEX `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='租户表';

-- ===================================================================
-- 3. 创建租户配置表 (tenant_configs)
-- ===================================================================
-- 说明: 存储租户级别的各类配置（支付、租赁、合作、同步等）
DROP TABLE IF EXISTS `tenant_configs`;
CREATE TABLE `tenant_configs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键（如：payment_config、system_config）',
  `config_value` JSON NOT NULL COMMENT '配置值（JSON格式，存储具体的配置内容）',
  `config_type` VARCHAR(20) NOT NULL COMMENT '配置类型：payment-支付/lease-租赁/cooperation-合作/sync-同步/system-系统',
  `config_desc` VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
  `is_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号（乐观锁）',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_config` (`tenant_id`, `config_key`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_config_type` (`config_type`),
  INDEX `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='租户配置表';

-- ===================================================================
-- 4. 创建租户套餐表 (tenant_packages)
-- ===================================================================
-- 说明: 租户套餐定义，控制不同套餐的功能和资源限制
DROP TABLE IF EXISTS `tenant_packages`;
CREATE TABLE `tenant_packages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
  `package_code` VARCHAR(50) NOT NULL COMMENT '套餐编码（如：TRIAL、BASIC、PREMIUM、ENTERPRISE）',
  `package_name` VARCHAR(100) NOT NULL COMMENT '套餐名称',
  `package_type` VARCHAR(20) NOT NULL COMMENT '套餐类型：trial-试用/basic-基础/premium-高级/enterprise-企业',
  `max_users` INT NOT NULL COMMENT '最大用户数',
  `max_storage_size` BIGINT NOT NULL COMMENT '最大存储容量（字节）',
  `max_api_calls_per_day` INT NOT NULL COMMENT '每日最大API调用次数',
  `price_monthly` DECIMAL(10,2) NOT NULL COMMENT '月付价格（元）',
  `price_yearly` DECIMAL(10,2) NOT NULL COMMENT '年付价格（元）',
  `features` JSON DEFAULT NULL COMMENT '功能列表（JSON格式）',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
  `is_active` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_package_code` (`package_code`),
  INDEX `idx_package_type` (`package_type`),
  INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='租户套餐表';

-- ===================================================================
-- 5. 创建租户统计表 (tenant_statistics)
-- ===================================================================
-- 说明: 记录租户的资源使用统计（用户数、存储用量、API调用次数等）
DROP TABLE IF EXISTS `tenant_statistics`;
CREATE TABLE `tenant_statistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `stat_hour` TINYINT DEFAULT NULL COMMENT '统计小时（0-23，NULL表示日统计）',
  `user_count` INT NOT NULL DEFAULT 0 COMMENT '用户总数',
  `merchant_count` INT NOT NULL DEFAULT 0 COMMENT '商户总数',
  `mini_program_count` INT NOT NULL DEFAULT 0 COMMENT '小程序总数',
  `storage_used_size` BIGINT NOT NULL DEFAULT 0 COMMENT '已用存储容量（字节）',
  `api_call_count` INT NOT NULL DEFAULT 0 COMMENT 'API调用次数',
  `order_count` INT NOT NULL DEFAULT 0 COMMENT '订单总数',
  `order_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额（元）',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_date_hour` (`tenant_id`, `stat_date`, `stat_hour`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='租户统计表';

-- ===================================================================
-- 6. 创建租户用户关系表 (tenant_user_relations)
-- ===================================================================
-- 说明: 用户与租户的关联关系，支持一个用户属于多个租户
DROP TABLE IF EXISTS `tenant_user_relations`;
CREATE TABLE `tenant_user_relations` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_shard` TINYINT NOT NULL COMMENT '用户分片ID（0-99）',
  `role_code` VARCHAR(50) NOT NULL DEFAULT 'TENANT_USER' COMMENT '角色编码：TENANT_ADMIN-租户管理员/TENANT_USER-租户用户',
  `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-激活/inactive-未激活',
  `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_user` (`tenant_id`, `user_id`),
  INDEX `idx_user_id` (`user_id`, `user_shard`),
  INDEX `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='租户用户关系表';

-- ===================================================================
-- 7. 创建租户API密钥表 (tenant_api_keys)
-- ===================================================================
-- 说明: 租户级别的API密钥管理，用于API访问控制
DROP TABLE IF EXISTS `tenant_api_keys`;
CREATE TABLE `tenant_api_keys` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '密钥ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `access_key` VARCHAR(100) NOT NULL COMMENT '访问密钥（Access Key）',
  `secret_key` VARCHAR(200) NOT NULL COMMENT '密钥（Secret Key，加密存储）',
  `key_name` VARCHAR(100) NOT NULL COMMENT '密钥名称',
  `key_status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '密钥状态：active-激活/disabled-禁用/expired-过期',
  `permissions` JSON DEFAULT NULL COMMENT '权限列表（JSON格式）',
  `ip_whitelist` JSON DEFAULT NULL COMMENT 'IP白名单（JSON格式）',
  `rate_limit` INT NOT NULL DEFAULT 1000 COMMENT '速率限制（每分钟请求数）',
  `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间（NULL表示永久有效）',
  `last_used_time` DATETIME DEFAULT NULL COMMENT '最后使用时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-否 1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_access_key` (`access_key`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_key_status` (`key_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='租户API密钥表';

-- ===================================================================
-- 8. 创建租户操作日志表 (tenant_operation_logs)
-- ===================================================================
-- 说明: 记录租户的操作日志，用于审计和问题排查
DROP TABLE IF EXISTS `tenant_operation_logs`;
CREATE TABLE `tenant_operation_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `tenant_id` BIGINT NOT NULL COMMENT '租户ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：CREATE_TENANT/UPDATE_CONFIG/DELETE_USER等',
  `operation_desc` VARCHAR(500) NOT NULL COMMENT '操作描述',
  `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(100) NOT NULL COMMENT '操作人姓名',
  `operator_ip` VARCHAR(50) DEFAULT NULL COMMENT '操作人IP',
  `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` TEXT DEFAULT NULL COMMENT '请求参数（JSON格式）',
  `response_result` VARCHAR(20) NOT NULL COMMENT '响应结果：success/failed',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  INDEX `idx_tenant_id` (`tenant_id`),
  INDEX `idx_operation_type` (`operation_type`),
  INDEX `idx_operation_time` (`operation_time`),
  INDEX `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='租户操作日志表';

-- ===================================================================
-- 9. 插入默认租户数据
-- ===================================================================
-- 9.1 插入默认租户
INSERT INTO `tenants` (
  `tenant_code`,
  `tenant_name`,
  `tenant_type`,
  `tenant_status`,
  `max_users`,
  `max_storage_size`,
  `max_api_calls_per_day`,
  `extend_fields`
) VALUES (
  'DEFAULT',
  '默认租户',
  'enterprise',
  'active',
  10000,
  107374182400,
  1000000,
  JSON_OBJECT(
    'is_default', true,
    'can_be_deleted', false,
    'system_tenant', true
  )
);

-- 9.2 插入默认租户配置
INSERT INTO `tenant_configs` (`tenant_id`, `config_key`, `config_value`, `config_type`, `config_desc`) VALUES
(1, 'payment_config', JSON_OBJECT(
  'default_channel', 'wechat',
  'enable_profit_sharing', true,
  'settlement_cycle', 'T+1',
  'min_settlement_amount', 100.00
), 'payment', '支付配置'),

(1, 'system_config', JSON_OBJECT(
  'enable_register', true,
  'enable_sms_login', true,
  'enable_wechat_login', true,
  'session_timeout', 7200
), 'system', '系统配置'),

(1, 'lease_config', JSON_OBJECT(
  'enable_lease', true,
  'max_lease_items', 100,
  'default_lease_duration_days', 365
), 'lease', '租赁配置'),

(1, 'cooperation_config', JSON_OBJECT(
  'enable_cooperation', true,
  'require_qualification_audit', true
), 'cooperation', '合作配置');

-- 9.3 插入套餐数据
INSERT INTO `tenant_packages` (
  `package_code`, `package_name`, `package_type`,
  `max_users`, `max_storage_size`, `max_api_calls_per_day`,
  `price_monthly`, `price_yearly`, `features`, `sort_order`
) VALUES
('TRIAL', '试用版', 'trial', 10, 1073741824, 1000, 0.00, 0.00,
 JSON_ARRAY('基础功能', '技术支持'), 1),

('BASIC', '基础版', 'basic', 100, 10737418240, 10000, 99.00, 990.00,
 JSON_ARRAY('基础功能', '邮件支持', '月度报表'), 2),

('PREMIUM', '高级版', 'premium', 1000, 107374182400, 100000, 499.00, 4990.00,
 JSON_ARRAY('所有功能', '7x24支持', '实时报表', 'API访问'), 3),

('ENTERPRISE', '企业版', 'enterprise', 10000, 1073741824000, 1000000, 1999.00, 19990.00,
 JSON_ARRAY('所有功能', '专属客服', '定制开发', 'SLA保障'), 4);

-- 9.4 插入默认租户用户关系（假设用户ID=1是管理员）
-- 注意：这里假设用户ID=1存在，实际执行时需要根据user_nexus数据库的实际情况调整
-- INSERT INTO `tenant_user_relations` (`tenant_id`, `user_id`, `user_shard`, `role_code`)
-- VALUES (1, 1, 1, 'TENANT_ADMIN');

-- ===================================================================
-- 10. 验证数据插入
-- ===================================================================
SELECT '=== 租户数据验证 ===' AS verification_item;

SELECT
  '租户表' AS table_name,
  COUNT(*) AS row_count,
  COUNT(CASE WHEN tenant_code = 'DEFAULT' THEN 1 END) AS default_tenant_exists
FROM tenants
UNION ALL
SELECT
  '租户配置表' AS table_name,
  COUNT(*) AS row_count,
  COUNT(CASE WHEN tenant_id = 1 THEN 1 END) AS default_config_exists
FROM tenant_configs
UNION ALL
SELECT
  '租户套餐表' AS table_name,
  COUNT(*) AS row_count,
  COUNT(CASE WHEN package_code = 'TRIAL' THEN 1 END) AS trial_package_exists
FROM tenant_packages;

-- ===================================================================
-- 11. 显示表结构（用于验证）
-- ===================================================================
SELECT '=== 租户表结构 ===' AS table_info;
DESC tenants;

SELECT '=== 租户配置表结构 ===' AS table_info;
DESC tenant_configs;

-- ===================================================================
-- 脚本执行完成
-- ===================================================================
SELECT '✅ duda_tenant数据库创建完成！' AS completion_message;
