-- =============================================
-- DudaNexus 身份多态化改造脚本
-- 支持同一用户拥有多个身份
-- =============================================

USE duda_nexus;

-- =============================================
-- 步骤1：修改users表，移除user_type字段
-- 说明：users表只存储用户基本信息和登录凭证
-- =============================================
ALTER TABLE `users` DROP COLUMN `user_type`;
ALTER TABLE `users` DROP COLUMN `company_id`;
ALTER TABLE `users` DROP COLUMN `department`;
ALTER TABLE `users` DROP COLUMN `position`;

-- 修改索引（因为删除了user_type字段）
ALTER TABLE `users` DROP INDEX `idx_user_type`;
ALTER TABLE `users` DROP INDEX `idx_company_id`;

-- =============================================
-- 步骤2：创建用户身份关联表
-- 说明：一个用户可以有多个身份，每个身份有独立的状态
-- =============================================
DROP TABLE IF EXISTS `user_type_roles`;
CREATE TABLE `user_type_roles` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_type` VARCHAR(50) NOT NULL COMMENT '用户身份类型: platform_admin-平台管理员, service_provider-服务商, platform_account-都达账户, backend_admin-后台管理员',
  `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '身份状态: active-激活, inactive-未激活, suspended-暂停, deleted-已删除',
  `company_id` BIGINT COMMENT '所属公司ID（仅服务商身份需要）',
  `department` VARCHAR(100) COMMENT '部门',
  `position` VARCHAR(100) COMMENT '职位',
  `audit_status` VARCHAR(20) DEFAULT 'approved' COMMENT '审核状态: pending-待审核, approved-已通过, rejected-已拒绝',
  `audit_time` DATETIME COMMENT '审核时间',
  `audit_by` BIGINT COMMENT '审核人ID',
  `audit_remark` VARCHAR(500) COMMENT '审核备注',
  `remark` VARCHAR(500) COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) COMMENT '创建人',
  `update_by` VARCHAR(50) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_type` (`user_id`, `user_type`, `deleted`) COMMENT '同一用户不能重复添加同一身份',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_status` (`status`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户身份关联表（支持一用户多身份）';

-- =============================================
-- 步骤3：创建登录日志表（可选，用于审计）
-- 说明：记录每次登录的用户身份
-- =============================================
DROP TABLE IF EXISTS `user_login_logs`;
CREATE TABLE `user_login_logs` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_type` VARCHAR(50) COMMENT '登录时使用的身份类型',
  `login_type` VARCHAR(50) NOT NULL COMMENT '登录方式: password-账号密码, sms-手机验证码, wechat-微信',
  `login_ip` VARCHAR(50) COMMENT '登录IP',
  `login_device` VARCHAR(100) COMMENT '登录设备',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `logout_time` DATETIME COMMENT '登出时间',
  `session_id` VARCHAR(100) COMMENT '会话ID',
  `status` VARCHAR(20) DEFAULT 'success' COMMENT '登录状态: success-成功, failed-失败',
  `fail_reason` VARCHAR(200) COMMENT '失败原因',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

-- =============================================
-- 步骤4：创建视图，方便查询用户及其身份
-- =============================================
DROP VIEW IF EXISTS `v_users_with_roles`;
CREATE VIEW `v_users_with_roles` AS
SELECT
    u.id AS user_id,
    u.username,
    u.phone,
    u.email,
    u.real_name,
    u.avatar,
    u.status AS user_status,
    utr.user_type,
    utr.status AS role_status,
    utr.company_id,
    utr.department,
    utr.position,
    utr.audit_status,
    u.create_time AS user_create_time,
    utr.create_time AS role_create_time
FROM users u
LEFT JOIN user_type_roles utr ON u.id = utr.user_id AND utr.deleted = 0
WHERE u.deleted = 0;

-- =============================================
-- 验证表结构
-- =============================================
SELECT 'User type multi-role transformation completed!' AS message;
SHOW TABLES;
SELECT 'View user roles:' AS info;
SELECT * FROM v_users_with_roles LIMIT 10;
