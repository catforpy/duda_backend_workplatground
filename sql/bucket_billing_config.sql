-- =============================================
-- Bucket费用配置表
-- 用途：存储每个Bucket的计费配置、余额、预警阈值
-- =============================================
CREATE TABLE `bucket_billing_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  -- 关联信息
  `bucket_name` VARCHAR(100) NOT NULL COMMENT 'Bucket名称',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `tenant_id` BIGINT COMMENT '租户ID',

  -- 计费配置
  `billing_cycle` VARCHAR(20) DEFAULT 'monthly' COMMENT '计费周期：daily-日结/weekly-周结/monthly-月结',
  `unit_price` DECIMAL(10,4) DEFAULT 0.0000 COMMENT '存储单价（元/GB/天）',
  `traffic_unit_price` DECIMAL(10,4) DEFAULT 0.0000 COMMENT '流量单价（元/GB）',
  `free_quota` BIGINT DEFAULT 0 COMMENT '免费配额（字节）',

  -- 预警配置
  `low_balance_threshold` DECIMAL(10,2) DEFAULT 10.00 COMMENT '余额不足预警阈值（元）',
  `quota_warning_threshold` INT DEFAULT 80 COMMENT '配额预警阈值（百分比）',
  `overdraft_allowed` TINYINT DEFAULT 0 COMMENT '是否允许透支：0-否 1-是',
  `max_overdraft` DECIMAL(10,2) DEFAULT 0.00 COMMENT '最大透支金额（元）',

  -- 通知配置
  `notification_enabled` TINYINT DEFAULT 1 COMMENT '是否启用通知：0-否 1-是',
  `notification_email` VARCHAR(200) COMMENT '通知邮箱',
  `notification_mobile` VARCHAR(20) COMMENT '通知手机号',
  `webhook_url` VARCHAR(500) COMMENT 'Webhook通知URL',

  -- 账户状态
  `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额（元）',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-正常/SUSPENDED-暂停/ARREARS-欠费',
  `suspend_reason` VARCHAR(500) COMMENT '暂停原因',

  -- 审计字段
  `created_by` BIGINT COMMENT '创建人',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT COMMENT '更新人',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否 1-是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bucket_name` (`bucket_name`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bucket费用配置表';
