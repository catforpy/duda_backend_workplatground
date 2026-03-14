-- =============================================
-- Bucket存储统计日志表
-- 用途：按时间记录存储使用情况，用于生成趋势图、报表
-- =============================================
CREATE TABLE `bucket_storage_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  -- 关联信息
  `bucket_name` VARCHAR(100) NOT NULL COMMENT 'Bucket名称',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `tenant_id` BIGINT COMMENT '租户ID',

  -- 统计数据
  `file_count` INT DEFAULT 0 COMMENT '文件数量',
  `storage_size` BIGINT DEFAULT 0 COMMENT '存储大小（字节）',
  `storage_quota` BIGINT COMMENT '存储配额（字节）',
  `usage_percentage` DECIMAL(5,2) COMMENT '使用率（百分比）',

  -- 分维度统计（按存储类型）
  `standard_size` BIGINT DEFAULT 0 COMMENT '标准存储大小（字节）',
  `ia_size` BIGINT DEFAULT 0 COMMENT '低频存储大小（字节）',
  `archive_size` BIGINT DEFAULT 0 COMMENT '归档存储大小（字节）',
  `cold_archive_size` BIGINT DEFAULT 0 COMMENT '冷归档存储大小（字节）',

  -- 统计维度
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `stat_hour` TINYINT COMMENT '统计小时（0-23，NULL表示日统计）',

  -- 审计字段
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (`id`),
  KEY `idx_bucket_date` (`bucket_name`, `stat_date`),
  KEY `idx_user_date` (`user_id`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_tenant_date` (`tenant_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bucket存储统计日志表';
