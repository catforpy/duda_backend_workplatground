-- =============================================
-- Bucket配置表（支持多云存储 + 多租户管理）
-- =============================================
CREATE TABLE `bucket_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',


  -- ==================== 基本信息 ====================
  `bucket_name` VARCHAR(100) NOT NULL COMMENT 'Bucket名称（全局唯一）',
  `bucket_display_name` VARCHAR(200) COMMENT 'Bucket显示名称（用户自定义）',
  `storage_type` VARCHAR(20) NOT NULL COMMENT '存储类型：aliyun-oss/tencent-cos/qiniu-kodo/minio',
  `region` VARCHAR(50) NOT NULL COMMENT '地域编码：cn-hangzhou/ap-guangzhou等',

  -- ==================== 多租户信息 ====================
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `user_type` VARCHAR(20) NOT NULL COMMENT '用户类型：platform-admin/service_provider/platform_account',
  `tenant_id` BIGINT COMMENT '租户ID（如果是多租户场景）',

  -- ==================== 存储配置 ====================
  `storage_class` VARCHAR(20) DEFAULT 'STANDARD' COMMENT '存储类型：STANDARD/IA/ARCHIVE/COLD_ARCHIVE',
  `data_redundancy_type` VARCHAR(20) DEFAULT 'LRS' COMMENT '数据冗余类型：LRS/ZRS',
  `acl_type` VARCHAR(20) NOT NULL DEFAULT 'PRIVATE' COMMENT '权限类型：PRIVATE/PUBLIC_READ/PUBLIC_READ_WRITE',

  -- ==================== 容量与限制 ====================
  `max_file_size` BIGINT DEFAULT 10737418240 COMMENT '最大文件大小（字节，默认10GB）',
  `max_file_count` INT DEFAULT 100000 COMMENT '最大文件数量',
  `max_storage_size` BIGINT COMMENT '最大存储容量（字节）',
  `allowed_file_types` VARCHAR(500) COMMENT '允许的文件类型（逗号分隔）',
  `blocked_file_types` VARCHAR(500) COMMENT '禁止的文件类型（逗号分隔）',

  -- ==================== 访问配置 ====================
  `domain_name` VARCHAR(200) COMMENT '自定义域名',
  `cdn_enabled` TINYINT DEFAULT 0 COMMENT '是否启用CDN：0-否 1-是',
  `cdn_domain` VARCHAR(200) COMMENT 'CDN域名',

  -- ==================== 功能配置 ====================
  `versioning_enabled` TINYINT DEFAULT 0 COMMENT '是否开启版本控制：0-否 1-是',
  `cors_enabled` TINYINT DEFAULT 0 COMMENT '是否开启CORS：0-否 1-是',
  `watermark_enabled` TINYINT DEFAULT 0 COMMENT '是否启用水印：0-否 1-1',
  `encryption_enabled` TINYINT DEFAULT 0 COMMENT '是否开启加密：0-否 1-是',
  `lifecycle_enabled` TINYINT DEFAULT 0 COMMENT '是否开启生命周期：0-否 1-是',

  -- ==================== 配额与用量 ====================
  `current_file_count` INT DEFAULT 0 COMMENT '当前文件数量',
  `current_storage_size` BIGINT DEFAULT 0 COMMENT '当前已用存储（字节）',
  `storage_used_quota` BIGINT COMMENT '存储配额（字节）',

  -- ==================== API密钥配置（加密存储） ====================
  `access_key_id` VARCHAR(500) COMMENT 'AccessKey ID（AES加密）',
  `access_key_secret` VARCHAR(500) COMMENT 'AccessKey Secret（AES加密）',
  `secret_key` VARCHAR(500) COMMENT '千牛云SecretKey（AES加密）',
  `endpoint` VARCHAR(200) COMMENT '接入点地址',
  `sts_role_arn` VARCHAR(200) COMMENT 'STS角色ARN',
  `sts_external_id` VARCHAR(200) COMMENT 'STS外部ID',

  -- ==================== 状态管理 ====================
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE/FROZEN/DELETING',
  `auto_renew_enabled` TINYINT DEFAULT 0 COMMENT '是否自动续费（预留）',

  -- ==================== 标签和分类 ====================
  `tags` JSON COMMENT '标签（JSON格式）',
  `category` VARCHAR(50) COMMENT '分类：documents/images/videos/audits/others',
  `description` VARCHAR(500) COMMENT 'Bucket描述',

  -- ==================== 审计字段 ====================
  `created_by` BIGINT COMMENT '创建人',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT COMMENT '更新人',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_time` DATETIME COMMENT '删除时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否 1-是',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bucket_name` (`bucket_name`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_storage_type` (`storage_type`),
  KEY `idx_status` (`status`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bucket配置表（支持多云存储+多租户）';

-- 创建索引
CREATE INDEX idx_bucket_name ON bucket_config(bucket_name);
CREATE INDEX idx_user_type_status ON bucket_config(user_type, status);

-- 添加注释
ALTER TABLE bucket_config COMMENT = 'Bucket配置表：支持多云存储（阿里云OSS/腾讯云COS/千牛云Kodo）和多租户管理';
