-- =============================================
-- Bucket状态统计表
-- 实时记录Bucket的使用状态
-- =============================================
CREATE TABLE IF NOT EXISTS `bucket_statistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',

  -- ==================== Bucket基本信息 ====================
  `bucket_name` VARCHAR(100) NOT NULL COMMENT 'Bucket名称',
  `region` VARCHAR(50) COMMENT '区域',
  `storage_type` VARCHAR(20) COMMENT '存储类型：STANDARD/IA/ARCHIVE',

  -- ==================== 文件统计 ====================
  `total_file_count` INT DEFAULT 0 COMMENT '文件总数',
  `total_storage_size` BIGINT DEFAULT 0 COMMENT '总存储量（字节）',
  `image_count` INT DEFAULT 0 COMMENT '图片文件数量',
  `video_count` INT DEFAULT 0 COMMENT '视频文件数量',
  `document_count` INT DEFAULT 0 COMMENT '文档文件数量',
  `other_count` INT DEFAULT 0 COMMENT '其他文件数量',

  -- ==================== 流量统计 ====================
  `total_traffic_bytes` BIGINT DEFAULT 0 COMMENT '总流量（字节）',
  `upload_traffic_bytes` BIGINT DEFAULT 0 COMMENT '上传流量（字节）',
  `download_traffic_bytes` BIGINT DEFAULT 0 COMMENT '下载流量（字节）',

  -- ==================== 配置信息 ====================
  `acl_type` VARCHAR(20) COMMENT 'ACL权限类型',
  `domain_name` VARCHAR(200) COMMENT '自定义域名',
  `cdn_enabled` TINYINT DEFAULT 0 COMMENT '是否启用CDN：0-否 1-是',
  `versioning_enabled` TINYINT DEFAULT 0 COMMENT '是否开启版本控制：0-否 1-是',
  `cors_enabled` TINYINT DEFAULT 0 COMMENT '是否开启CORS：0-否 1-是',

  -- ==================== 文件类型限制 ====================
  `allowed_file_types` VARCHAR(500) COMMENT '允许的文件类型（逗号分隔）',
  `blocked_file_types` VARCHAR(500) COMMENT '禁止的文件类型（逗号分隔）',
  `max_file_size` BIGINT COMMENT '最大文件大小（字节）',
  `max_file_count` INT COMMENT '最大文件数量',

  -- ==================== 计费信息 ====================
  `storage_cost` DECIMAL(10,2) DEFAULT 0.00 COMMENT '存储费用（元）',
  `traffic_cost` DECIMAL(10,2) DEFAULT 0.00 COMMENT '流量费用（元）',
  `request_cost` DECIMAL(10,2) DEFAULT 0.00 COMMENT '请求费用（元）',
  `total_cost` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '总费用（元）',

  -- ==================== 统计时间 ====================
  `last_sync_time` DATETIME COMMENT '最后同步时间',
  `last_upload_time` DATETIME COMMENT '最后上传时间',
  `last_download_time` DATETIME COMMENT '最后下载时间',
  `last_delete_time` DATETIME COMMENT '最后删除时间',

  -- ==================== 其他信息 ====================
  `tags` VARCHAR(200) COMMENT '标签（逗号分隔）',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/SUSPENDED/DELETED',

  -- ==================== 时间戳 ====================
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bucket_name` (`bucket_name`),
  KEY `idx_region` (`region`),
  KEY `idx_storage_type` (`storage_type`),
  KEY `idx_status` (`status`),
  KEY `idx_updated_time` (`updated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bucket状态统计表';

-- =============================================
-- 使用示例
-- =============================================
-- 1. 查询所有Bucket状态
-- SELECT * FROM bucket_statistics ORDER BY updated_time DESC;
--
-- 2. 更新文件统计（上传/删除文件后）
-- UPDATE bucket_statistics
-- SET total_file_count = total_file_count + 1,
--     total_storage_size = total_storage_size + 1024000,
--     last_upload_time = NOW()
-- WHERE bucket_name = 'duda-java-backend-test';
--
-- 3. 更新流量统计
-- UPDATE bucket_statistics
-- SET total_traffic_bytes = total_traffic_bytes + 512000,
--     download_traffic_bytes = download_traffic_bytes + 512000,
--     traffic_cost = traffic_cost + 0.25
-- WHERE bucket_name = 'duda-java-backend-test';
--
-- 4. 重置统计（删除Bucket后）
-- UPDATE bucket_statistics
-- SET total_file_count = 0,
--     total_storage_size = 0,
--     image_count = 0,
--     video_count = 0,
--     document_count = 0,
--     other_count = 0
-- WHERE bucket_name = 'duda-java-backend-test';
-- =============================================
