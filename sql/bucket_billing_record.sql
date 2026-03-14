-- =============================================
-- Bucket费用账单表
-- 用途：记录每个Bucket的费用账单，用于计费、对账
-- =============================================
CREATE TABLE `bucket_billing_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  -- 关联信息
  `bucket_name` VARCHAR(100) NOT NULL COMMENT 'Bucket名称',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `tenant_id` BIGINT COMMENT '租户ID',

  -- 账单周期
  `billing_cycle` VARCHAR(20) NOT NULL COMMENT '计费周期：daily/weekly/monthly',
  `cycle_start` DATE NOT NULL COMMENT '周期开始日期',
  `cycle_end` DATE NOT NULL COMMENT '周期结束日期',

  -- 使用量汇总
  `avg_storage_size` BIGINT DEFAULT 0 COMMENT '平均存储量（字节）',
  `total_traffic` BIGINT DEFAULT 0 COMMENT '总流量（字节）',
  `total_requests` INT DEFAULT 0 COMMENT '总请求次数',
  `total_files` INT DEFAULT 0 COMMENT '文件数量',

  -- 费用明细
  `storage_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '存储费用（元）',
  `traffic_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '流量费用（元）',
  `request_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '请求费用（元）',
  `other_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '其他费用（元）',
  `discount_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额（元）',
  `total_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '总费用（元）',

  -- 支付信息
  `payment_status` VARCHAR(20) DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID-未支付/PAID-已支付/OVERDUE-逾期',
  `payment_time` DATETIME COMMENT '支付时间',
  `payment_method` VARCHAR(50) COMMENT '支付方式：alipay/wechat/bank_transfer',
  `transaction_id` VARCHAR(100) COMMENT '交易流水号',
  `remark` VARCHAR(500) COMMENT '备注',

  -- 审计字段
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否 1-是',

  PRIMARY KEY (`id`),
  KEY `idx_bucket_cycle` (`bucket_name`, `billing_cycle`),
  KEY `idx_user_status` (`user_id`, `payment_status`),
  KEY `idx_cycle_end` (`cycle_end`),
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bucket费用账单表';
