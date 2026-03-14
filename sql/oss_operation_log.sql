-- =============================================
-- OSS操作日志表
-- 记录所有OSS操作（上传、下载、删除、修改配置等）
-- =============================================
CREATE TABLE IF NOT EXISTS `oss_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',

  -- ==================== 基本信息 ====================
  `bucket_name` VARCHAR(100) NOT NULL COMMENT 'Bucket名称',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型：upload/download/delete/copy/rename/config/etc',
  `operation_category` VARCHAR(50) COMMENT '操作分类：file/bucket/config/policy',
  `object_key` VARCHAR(500) COMMENT '对象Key（文件操作时记录）',

  -- ==================== 操作详情 ====================
  `operation_desc` VARCHAR(500) COMMENT '操作描述',
  `request_params` TEXT COMMENT '请求参数（JSON格式）',
  `response_data` TEXT COMMENT '响应数据（JSON格式）',

  -- ==================== 操作结果 ====================
  `status` VARCHAR(20) NOT NULL COMMENT '操作状态：SUCCESS/FAILED/PARTIAL',
  `error_code` VARCHAR(50) COMMENT '错误码',
  `error_message` TEXT COMMENT '错误信息',

  -- ==================== 文件信息（文件操作时） ====================
  `file_size` BIGINT COMMENT '文件大小（字节）',
  `file_type` VARCHAR(100) COMMENT '文件类型（MIME）',
  `file_etag` VARCHAR(100) COMMENT '文件ETag',
  `file_version_id` VARCHAR(100) COMMENT '文件版本ID',

  -- ==================== 配置变更（配置操作时） ====================
  `config_field` VARCHAR(100) COMMENT '配置字段名',
  `old_value` TEXT COMMENT '旧值（JSON格式）',
  `new_value` TEXT COMMENT '新值（JSON格式）',

  -- ==================== 用户信息 ====================
  `operator_type` VARCHAR(20) COMMENT '操作者类型：SYSTEM/USER/API',
  `operator_id` BIGINT COMMENT '操作者ID',
  `operator_name` VARCHAR(100) COMMENT '操作者名称',
  `tenant_id` BIGINT COMMENT '租户ID',

  -- ==================== 请求信息 ====================
  `request_id` VARCHAR(100) COMMENT '请求ID（阿里云返回）',
  `client_ip` VARCHAR(50) COMMENT '客户端IP',
  `user_agent` VARCHAR(500) COMMENT 'User-Agent',

  -- ==================== 时间信息 ====================
  `start_time` DATETIME COMMENT '操作开始时间',
  `end_time` DATETIME COMMENT '操作结束时间',
  `duration_ms` BIGINT COMMENT '操作耗时（毫秒）',

  -- ==================== 其他信息 ====================
  `extra_info` TEXT COMMENT '额外信息（JSON格式）',
  `tags` VARCHAR(200) COMMENT '标签（逗号分隔）',

  -- ==================== 时间戳 ====================
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  KEY `idx_bucket_name` (`bucket_name`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_status` (`status`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_object_key` (`object_key`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OSS操作日志表';

-- =============================================
-- 索引说明
-- =============================================
-- bucket_name: 按Bucket查询操作历史
-- operation_type: 按操作类型查询
-- status: 按状态查询（成功/失败）
-- operator_id: 按操作者查询
-- tenant_id: 按租户查询
-- created_time: 按时间范围查询
-- object_key: 按文件查询操作历史

-- =============================================
-- 使用示例
-- =============================================
-- 1. 查询某个Bucket的所有操作
-- SELECT * FROM oss_operation_log WHERE bucket_name = 'duda-java-backend-test' ORDER BY created_time DESC;
--
-- 2. 查询文件上传操作
-- SELECT * FROM oss_operation_log WHERE operation_type = 'upload' AND status = 'SUCCESS';
--
-- 3. 查询某个文件的所有操作
-- SELECT * FROM oss_operation_log WHERE object_key = 'test/example.jpg' ORDER BY created_time DESC;
--
-- 4. 统计操作类型分布
-- SELECT operation_type, COUNT(*) as count FROM oss_operation_log GROUP BY operation_type;
--
-- 5. 查询失败的操作
-- SELECT * FROM oss_operation_log WHERE status = 'FAILED' ORDER BY created_time DESC;
-- =============================================
