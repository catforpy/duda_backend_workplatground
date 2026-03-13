
-- ================================
-- duda-file模块数据库表设计
-- 版本: v1.0
-- 创建时间: 2025-03-13
-- 说明: 支持100万级Bucket、亿级文件存储的文件管理系统
-- ================================

-- ================================
-- 1. Bucket配置表 (已存在,补充字段)
-- ================================
-- 说明: 存储Bucket的配置信息,支持多云存储
-- 操作: 读写操作都在主库

USE `your_database_name`;

-- ================================
-- 2. 对象元数据表
-- ================================
CREATE TABLE IF NOT EXISTS `object_metadata` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bucket_name` VARCHAR(255) NOT NULL COMMENT '存储空间名称',
  `object_key` VARCHAR(1024) NOT NULL COMMENT '对象键',
  `version_id` VARCHAR(255) DEFAULT NULL COMMENT '对象版本ID(开启版本控制时)',
  `file_size` BIGINT(20) DEFAULT NULL COMMENT '文件大小(字节)',
  `file_name` VARCHAR(500) DEFAULT NULL COMMENT '原始文件名',
  `content_type` VARCHAR(255) DEFAULT NULL COMMENT 'MIME类型',
  `content_md5` VARCHAR(32) DEFAULT NULL COMMENT 'MD5值',
  `crc64` BIGINT(20) DEFAULT NULL COMMENT 'CRC64校验值',
  `storage_class` VARCHAR(20) DEFAULT 'STANDARD' COMMENT '存储类型:STANDARD, IA, ARCHIVE, COLD_ARCHIVE',
  `object_type` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '对象类型: NORMAL-普通, APPENDABLE-追加, SYMLINK-软链接',
  `is_directory` TINYINT(1) DEFAULT 0 COMMENT '是否为目录',
  `is_symlink` TINYINT(1) DEFAULT 0 COMMENT '是否为软链接',
  `symlink_target` VARCHAR(1024) DEFAULT NULL COMMENT '软链接目标',
  `acl` VARCHAR(20) DEFAULT 'PRIVATE' COMMENT '访问权限: PRIVATE, PUBLIC_READ, PUBLIC_READ_WRITE',
  `upload_id` VARCHAR(255) DEFAULT NULL COMMENT '分片上传ID',
  `part_count` INT(11) DEFAULT NULL COMMENT '分片数量(分片上传时)',
  `position` BIGINT(20) DEFAULT NULL COMMENT '追加位置(追加上传时)',
  `restore_status` VARCHAR(20) DEFAULT NULL COMMENT '恢复状态: IN_PROGRESS, COMPLETED',
  `expiry_time` DATETIME DEFAULT NULL COMMENT '归档恢复过期时间',
  `etag` VARCHAR(255) DEFAULT NULL COMMENT '对象ETag',
  `user_metadata` JSON DEFAULT NULL COMMENT '用户自定义元数据(JSON格式)',
  `tags` JSON DEFAULT NULL COMMENT '对象标签(JSON格式)',
  `upload_ip` VARCHAR(50) DEFAULT NULL COMMENT '上传IP',
  `upload_time` DATETIME DEFAULT NULL COMMENT '上传时间',
  `last_access_time` DATETIME DEFAULT NULL COMMENT '最后访问时间',
  `access_count` BIGINT(20) DEFAULT 0 COMMENT '访问次数',
  `download_count` BIGINT(20) DEFAULT 0 COMMENT '下载次数',
  `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态: active-正常, deleted-已删除',
  `created_by` BIGINT(20) DEFAULT NULL COMMENT '创建者用户ID',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bucket_object_version` (`bucket_name`, `object_key`, `version_id`),
  KEY `idx_bucket_name` (`bucket_name`),
  KEY `idx_status` (`status`),
  KEY `idx_upload_time` (`upload_time`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_object_key` (`object_key`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对象元数据表';

-- ================================
-- 3. 上传记录表
-- ================================
CREATE TABLE IF NOT EXISTS `upload_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bucket_name` VARCHAR(255) NOT NULL COMMENT '存储空间名称',
  `object_key` VARCHAR(1024) NOT NULL COMMENT '对象键',
  `upload_id` VARCHAR(255) DEFAULT NULL COMMENT '分片上传ID(断点续传用)',
  `user_id` BIGINT(20) NOT NULL COMMENT '上传用户ID',
  `user_shard` TINYINT(4) NOT NULL COMMENT '用户分片编号',
  `file_name` VARCHAR(500) DEFAULT NULL COMMENT '原始文件名',
  `file_size` BIGINT(20) NOT NULL COMMENT '文件大小(字节)',
  `file_md5` VARCHAR(32) DEFAULT NULL COMMENT '文件MD5',
  `content_type` VARCHAR(255) DEFAULT NULL COMMENT 'MIME类型',
  `upload_method` VARCHAR(20) NOT NULL COMMENT '上传方式: simple, multipart, append, form, sts, presigned',
  `part_count` INT(11) DEFAULT 0 COMMENT '分片数量',
  `part_size` BIGINT(20) DEFAULT NULL COMMENT '分片大小',
  `uploaded_parts` INT(11) DEFAULT 0 COMMENT '已上传分片数',
  `upload_status` VARCHAR(20) NOT NULL COMMENT '上传状态: INIT, IN_PROGRESS, COMPLETED, FAILED, CANCELLED',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `upload_ip` VARCHAR(50) DEFAULT NULL COMMENT '上传IP',
  `client_type` VARCHAR(20) DEFAULT NULL COMMENT '客户端类型: web, app, mini_program',
  `device_id` VARCHAR(100) DEFAULT NULL COMMENT '设备ID',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_bucket_object` (`bucket_name`, `object_key`),
  KEY `idx_upload_id` (`upload_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_shard` (`user_shard`),
  KEY `idx_status` (`upload_status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传记录表';

-- ================================
-- 4. 文件统计表
-- ================================
CREATE TABLE IF NOT EXISTS `file_statistics` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bucket_name` VARCHAR(255) NOT NULL COMMENT '存储空间名称',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `stat_type` VARCHAR(20) NOT NULL COMMENT '统计类型: daily-每日, monthly-每月',
  `total_files` BIGINT(20) DEFAULT 0 COMMENT '文件总数',
  `total_size` BIGINT(20) DEFAULT 0 COMMENT '总存储量(字节)',
  `upload_count` BIGINT(20) DEFAULT 0 COMMENT '上传次数',
  `download_count` BIGINT(20) DEFAULT 0 COMMENT '下载次数',
  `traffic_upload` BIGINT(20) DEFAULT 0 COMMENT '上传流量(字节)',
  `traffic_download` BIGINT(20) DEFAULT 0 COMMENT '下载流量(字节)',
  `new_files` BIGINT(20) DEFAULT 0 COMMENT '新增文件数',
  `deleted_files` BIGINT(20) DEFAULT 0 COMMENT '删除文件数',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bucket_stat_date_type` (`bucket_name`, `stat_date`, `stat_type`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_stat_type` (`stat_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件统计表';

-- ================================
-- 5. 断点续传记录表
-- ================================
CREATE TABLE IF NOT EXISTS `resume_upload_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_id` VARCHAR(64) NOT NULL COMMENT '断点续传记录ID(UUID)',
  `bucket_name` VARCHAR(255) NOT NULL COMMENT '存储空间名称',
  `object_key` VARCHAR(1024) NOT NULL COMMENT '对象键',
  `local_file_path` VARCHAR(500) NOT NULL COMMENT '本地文件路径',
  `file_size` BIGINT(20) NOT NULL COMMENT '文件大小(字节)',
  `part_size` BIGINT(20) DEFAULT NULL COMMENT '分片大小',
  `total_part_count` INT(11) DEFAULT NULL COMMENT '总分片数',
  `uploaded_parts` JSON DEFAULT NULL COMMENT '已上传分片列表(JSON)',
  `upload_status` VARCHAR(20) NOT NULL COMMENT '状态: INIT, IN_PROGRESS, PAUSED, COMPLETED, FAILED, CANCELLED',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `user_shard` TINYINT(4) NOT NULL COMMENT '用户分片编号',
  `client_type` VARCHAR(20) DEFAULT NULL COMMENT '客户端类型',
  `device_id` VARCHAR(100) DEFAULT NULL COMMENT '设备ID',
  `concurrent_threads` INT(11) DEFAULT 3 COMMENT '并发线程数',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `last_update_time` DATETIME DEFAULT NULL COMMENT '最后更新时间',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_id` (`record_id`),
  KEY `idx_bucket_object` (`bucket_name`, `object_key`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_shard` (`user_shard`),
  KEY `idx_status` (`upload_status`),
  KEY `idx_last_update` (`last_update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='断点续传记录表';

-- ================================
-- 6. 安全配置表
-- ================================
CREATE TABLE IF NOT EXISTS `security_config` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bucket_name` VARCHAR(255) NOT NULL COMMENT '存储空间名称',
  `encryption_type` VARCHAR(32) DEFAULT NULL COMMENT '加密类型: SSE_OSS, SSE_KMS',
  `kms_key_id` VARCHAR(255) DEFAULT NULL COMMENT 'KMS密钥ID',
  `enable_encryption` TINYINT(1) DEFAULT 0 COMMENT '是否启用服务端加密',
  `enable_client_encryption` TINYINT(1) DEFAULT 0 COMMENT '是否启用客户端加密',
  `tls_version` VARCHAR(16) DEFAULT 'TLSv1.3' COMMENT 'TLS版本: TLSv1.0, TLSv1.1, TLSv1.2, TLSv1.3',
  `integrity_check_type` VARCHAR(16) DEFAULT 'CRC64' COMMENT '完整性校验类型: NONE, MD5, CRC64, BOTH',
  `enable_content_detection` TINYINT(1) DEFAULT 0 COMMENT '是否启用内容安全检测',
  `enable_virus_scan` TINYINT(1) DEFAULT 0 COMMENT '是否启用病毒检测',
  `enable_sensitive_data_scan` TINYINT(1) DEFAULT 0 COMMENT '是否启用敏感数据扫描',
  `content_detection_threshold` DECIMAL(5,2) DEFAULT 90.00 COMMENT '内容安全检测阈值(0-100)',
  `virus_scan_action` VARCHAR(32) DEFAULT 'DELETE' COMMENT '病毒检测后操作: DELETE, QUARANTINE, MARK',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bucket_name` (`bucket_name`),
  KEY `idx_enable_content_detection` (`enable_content_detection`),
  KEY `idx_enable_virus_scan` (`enable_virus_scan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全配置表';

-- ================================
-- 7. 病毒扫描记录表
-- ================================
CREATE TABLE IF NOT EXISTS `virus_scan_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` VARCHAR(255) NOT NULL COMMENT '任务ID',
  `bucket_name` VARCHAR(255) NOT NULL COMMENT '存储空间名称',
  `object_key` VARCHAR(1024) NOT NULL COMMENT '对象键',
  `file_size` BIGINT(20) DEFAULT NULL COMMENT '文件大小(字节)',
  `file_version_id` VARCHAR(255) DEFAULT NULL COMMENT '文件版本ID',
  `status` VARCHAR(32) NOT NULL COMMENT '状态: SCANNING, SUCCESS, FAILED',
  `virus_found` TINYINT(1) DEFAULT 0 COMMENT '是否发现病毒',
  `virus_type` VARCHAR(64) DEFAULT NULL COMMENT '病毒类型',
  `virus_name` VARCHAR(255) DEFAULT NULL COMMENT '病毒名称',
  `scan_time` BIGINT(20) DEFAULT NULL COMMENT '扫描耗时(毫秒)',
  `action_taken` VARCHAR(32) DEFAULT NULL COMMENT '采取的操作: DELETE, QUARANTINE, MARK',
  `error_message` VARCHAR(1024) DEFAULT NULL COMMENT '错误信息',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_bucket_object` (`bucket_name`, `object_key`(255)),
  KEY `idx_status` (`status`),
  KEY `idx_virus_found` (`virus_found`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病毒扫描记录表';

-- ================================
-- 8. 内容安全检测记录表
-- ================================
CREATE TABLE IF NOT EXISTS `content_detection_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` VARCHAR(255) NOT NULL COMMENT '任务ID',
  `bucket_name` VARCHAR(255) NOT NULL COMMENT '存储空间名称',
  `object_key` VARCHAR(1024) NOT NULL COMMENT '对象键',
  `detection_type` VARCHAR(32) NOT NULL COMMENT '检测类型: IMAGE, AVATAR, AIGC',
  `status` VARCHAR(32) NOT NULL COMMENT '状态: PROCESSING, SUCCESS, FAILED',
  `porn_label` VARCHAR(32) DEFAULT NULL COMMENT '色情标签',
  `porn_score` DECIMAL(5,2) DEFAULT NULL COMMENT '色情分数',
  `porn_confidence` DECIMAL(5,2) DEFAULT NULL COMMENT '色情置信度',
  `politics_label` VARCHAR(32) DEFAULT NULL COMMENT '政治标签',
  `politics_score` DECIMAL(5,2) DEFAULT NULL COMMENT '政治分数',
  `politics_confidence` DECIMAL(5,2) DEFAULT NULL COMMENT '政治置信度',
  `terrorism_label` VARCHAR(32) DEFAULT NULL COMMENT '恐怖标签',
  `terrorism_score` DECIMAL(5,2) DEFAULT NULL COMMENT '恐怖分数',
  `terrorism_confidence` DECIMAL(5,2) DEFAULT NULL COMMENT '恐怖置信度',
  `ad_label` VARCHAR(32) DEFAULT NULL COMMENT '广告标签',
  `ad_score` DECIMAL(5,2) DEFAULT NULL COMMENT '广告分数',
  `ad_confidence` DECIMAL(5,2) DEFAULT NULL COMMENT '广告置信度',
  `is_aigc` TINYINT(1) DEFAULT 0 COMMENT '是否为AIGC生成',
  `aigc_confidence` DECIMAL(5,2) DEFAULT NULL COMMENT 'AIGC置信度',
  `aigc_tool_type` VARCHAR(64) DEFAULT NULL COMMENT 'AIGC工具类型',
  `risk_level` VARCHAR(16) DEFAULT NULL COMMENT '风险等级: LOW, MEDIUM, HIGH',
  `action_taken` VARCHAR(32) DEFAULT NULL COMMENT '采取的操作: APPROVE, REJECT, REVIEW',
  `error_message` VARCHAR(1024) DEFAULT NULL COMMENT '错误信息',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_bucket_object` (`bucket_name`, `object_key`(255)),
  KEY `idx_detection_type` (`detection_type`),
  KEY `idx_status` (`status`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容安全检测记录表';

-- ================================
-- 9. 敏感数据扫描记录表
-- ================================
CREATE TABLE IF NOT EXISTS `sensitive_data_scan_record` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` VARCHAR(255) NOT NULL COMMENT '任务ID',
  `bucket_name` VARCHAR(255) NOT NULL COMMENT '存储空间名称',
  `scan_prefix` VARCHAR(1024) DEFAULT NULL COMMENT '扫描前缀',
  `status` VARCHAR(32) NOT NULL COMMENT '状态: SCANNING, SUCCESS, FAILED',
  `total_files` BIGINT(20) DEFAULT 0 COMMENT '总文件数',
  `scanned_files` BIGINT(20) DEFAULT 0 COMMENT '已扫描文件数',
  `sensitive_files_found` BIGINT(20) DEFAULT 0 COMMENT '发现敏感文件数',
  `total_scan_size` BIGINT(20) DEFAULT 0 COMMENT '总扫描大小(字节)',
  `sensitive_data_types` JSON DEFAULT NULL COMMENT '敏感数据类型统计',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `duration_seconds` BIGINT(20) DEFAULT NULL COMMENT '扫描耗时(秒)',
  `error_message` VARCHAR(1024) DEFAULT NULL COMMENT '错误信息',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_bucket_prefix` (`bucket_name`, `scan_prefix`(255)),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感数据扫描记录表';

-- ================================
-- 10. 敏感数据详情表
-- ================================
CREATE TABLE IF NOT EXISTS `sensitive_data_detail` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scan_task_id` VARCHAR(255) NOT NULL COMMENT '扫描任务ID',
  `bucket_name` VARCHAR(255) NOT NULL COMMENT '存储空间名称',
  `object_key` VARCHAR(1024) NOT NULL COMMENT '对象键',
  `data_type` VARCHAR(64) NOT NULL COMMENT '敏感数据类型: PHONE, EMAIL, ID_CARD, BANK_CARD, PASSWORD, etc.',
  `data_level` VARCHAR(8) NOT NULL COMMENT '敏感等级: S1, S2, S3, S4',
  `data_count` BIGINT(20) DEFAULT 0 COMMENT '敏感数据数量',
  `data_sample` TEXT DEFAULT NULL COMMENT '敏感数据样本(脱敏)',
  `occurrence_position` JSON DEFAULT NULL COMMENT '出现位置(行号、列号等)',
  `risk_level` VARCHAR(16) DEFAULT NULL COMMENT '风险等级: LOW, MEDIUM, HIGH, CRITICAL',
  `action_taken` VARCHAR(32) DEFAULT NULL COMMENT '采取的操作: NONE, MASK, ENCRYPT, DELETE',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_scan_task_id` (`scan_task_id`),
  KEY `idx_bucket_object` (`bucket_name`, `object_key`(255)),
  KEY `idx_data_type` (`data_type`),
  KEY `idx_data_level` (`data_level`),
  KEY `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感数据详情表';

-- ================================
-- 11. 文件访问日志表
-- ================================
CREATE TABLE IF NOT EXISTS `file_access_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bucket_name` VARCHAR(255) NOT NULL COMMENT '存储空间名称',
  `object_key` VARCHAR(1024) NOT NULL COMMENT '对象键',
  `operation` VARCHAR(20) NOT NULL COMMENT '操作类型: UPLOAD, DOWNLOAD, DELETE, COPY, RENAME',
  `user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
  `user_shard` TINYINT(4) DEFAULT NULL COMMENT '用户分片编号',
  `client_ip` VARCHAR(50) DEFAULT NULL COMMENT '客户端IP',
  `device_id` VARCHAR(100) DEFAULT NULL COMMENT '设备ID',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
  `file_size` BIGINT(20) DEFAULT NULL COMMENT '文件大小(字节)',
  `result_status` VARCHAR(20) NOT NULL COMMENT '结果状态: SUCCESS, FAILED',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `duration_ms` BIGINT(20) DEFAULT NULL COMMENT '耗时(毫秒)',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_bucket_object` (`bucket_name`, `object_key`(255)),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation` (`operation`),
  KEY `idx_result_status` (`result_status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件访问日志表';

-- ================================
-- 索引优化说明
-- ================================
-- 1. 所有表都有主键和created_time索引
-- 2. 频繁查询的字段都添加了索引(bucket_name, object_key, user_id, status等)
-- 3. 使用联合索引优化查询性能
-- 4. 大字段(file_path, user_agent)放在最后
-- 5. JSON字段用于存储灵活数据(tags, user_metadata等)

-- ================================
-- 主从分离说明
-- ================================
-- 写操作(INSERT/UPDATE/DELETE): 主库
-- 读操作(SELECT): 从库
-- 事务: 主库
-- 报表: 从库

-- 示例:
-- INSERT INTO upload_record (写操作) → 主库
-- SELECT * FROM object_metadata WHERE bucket_name = ? (读操作) → 从库
-- 统计分析 → 从库
