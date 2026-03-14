-- =============================================
-- Bucket流量统计日志表
-- 用途：按时间记录流量使用情况，用于费用计算、流量分析
-- =============================================
CREATE TABLE `bucket_traffic_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  -- 关联信息
  `bucket_name` VARCHAR(100) NOT NULL COMMENT 'Bucket名称',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `tenant_id` BIGINT COMMENT '租户ID',

  -- 流量数据
  `upload_traffic` BIGINT DEFAULT 0 COMMENT '上传流量（字节）',
  `download_traffic` BIGINT DEFAULT 0 COMMENT '下载流量（字节）',
  `total_traffic` BIGINT DEFAULT 0 COMMENT '总流量（字节）',

  -- 请求数据
  `request_count` INT DEFAULT 0 COMMENT '总请求次数',
  `upload_count` INT DEFAULT 0 COMMENT '上传次数',
  `download_count` INT DEFAULT 0 COMMENT '下载次数',
  `head_count` INT DEFAULT 0 COMMENT 'HEAD请求次数',
  `other_count` INT DEFAULT 0 COMMENT '其他请求次数',

  -- 统计维度（按文件类型）
  `image_traffic` BIGINT DEFAULT 0 COMMENT '图片流量（字节）',
  `video_traffic` BIGINT DEFAULT 0 COMMENT '视频流量（字节）',
  `document_traffic` BIGINT DEFAULT 0 COMMENT '文档流量（字节）',
  `other_traffic` BIGINT DEFAULT 0 COMMENT '其他流量（字节）',

  -- 统计维度（按时间）
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `stat_hour` TINYINT COMMENT '统计小时（0-23，NULL表示日统计）',

  -- 审计字段
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (`id`),
  KEY `idx_bucket_date` (`bucket_name`, `stat_date`),
  KEY `idx_user_date` (`user_id`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_tenant_date` (`tenant_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bucket流量统计日志表';
