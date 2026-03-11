-- =============================================
-- DudaNexus 数据库初始化脚本
-- 创建数据库和所有表结构
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS duda_nexus
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE duda_nexus;

-- =============================================
-- 1. 用户表 (users)
-- =============================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` BIGINT NOT NULL COMMENT '用户ID（雪花算法）',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` VARCHAR(100) COMMENT '真实姓名',
  `phone` VARCHAR(20) COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `avatar` VARCHAR(500) COMMENT '头像URL',
  `user_type` VARCHAR(20) NOT NULL DEFAULT 'platform_account' COMMENT '用户类型: platform_admin-平台管理员, service_provider-服务商, platform_account-都达账户, backend_admin-后台管理员',
  `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active-激活, inactive-未激活, suspended-暂停, deleted-已删除',
  `company_id` BIGINT COMMENT '所属公司ID',
  `department` VARCHAR(100) COMMENT '部门',
  `position` VARCHAR(100) COMMENT '职位',
  `province` VARCHAR(50) COMMENT '省份',
  `city` VARCHAR(50) COMMENT '城市',
  `address` VARCHAR(255) COMMENT '详细地址',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
  `remark` VARCHAR(500) COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) COMMENT '创建人',
  `update_by` VARCHAR(50) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_status` (`status`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 公司表 (companies)
-- =============================================
DROP TABLE IF EXISTS `companies`;
CREATE TABLE `companies` (
  `id` BIGINT NOT NULL COMMENT '公司ID（雪花算法）',
  `company_name` VARCHAR(200) NOT NULL COMMENT '公司名称',
  `company_code` VARCHAR(50) NOT NULL COMMENT '公司编码',
  `license_no` VARCHAR(100) COMMENT '营业执照号',
  `legal_person` VARCHAR(100) COMMENT '法人代表',
  `legal_person_phone` VARCHAR(20) COMMENT '法人电话',
  `contact_person` VARCHAR(100) COMMENT '联系人',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `contact_email` VARCHAR(100) COMMENT '联系邮箱',
  `province` VARCHAR(50) COMMENT '省份',
  `city` VARCHAR(50) COMMENT '城市',
  `district` VARCHAR(50) COMMENT '区县',
  `address` VARCHAR(255) COMMENT '详细地址',
  `business_scope` TEXT COMMENT '经营范围',
  `company_type` VARCHAR(20) NOT NULL DEFAULT 'service_provider' COMMENT '公司类型: service_provider-服务商, platform-平台方',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending-待审核, active-激活, suspended-暂停, deleted-已删除',
  `audit_status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '审核状态: pending-待审核, approved-已通过, rejected-已拒绝',
  `audit_time` DATETIME COMMENT '审核时间',
  `audit_by` BIGINT COMMENT '审核人ID',
  `audit_remark` VARCHAR(500) COMMENT '审核备注',
  `registration_date` DATE COMMENT '注册日期',
  `remark` VARCHAR(500) COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) COMMENT '创建人',
  `update_by` VARCHAR(50) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_code` (`company_code`),
  KEY `idx_company_name` (`company_name`),
  KEY `idx_status` (`status`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司表';

-- =============================================
-- 3. 小程序表 (mini_programs)
-- =============================================
DROP TABLE IF EXISTS `mini_programs`;
CREATE TABLE `mini_programs` (
  `id` BIGINT NOT NULL COMMENT '小程序ID（雪花算法）',
  `app_id` VARCHAR(100) NOT NULL COMMENT '微信AppID',
  `app_secret` VARCHAR(255) NOT NULL COMMENT '微信AppSecret（加密存储）',
  `mini_program_name` VARCHAR(200) NOT NULL COMMENT '小程序名称',
  `mini_program_code` VARCHAR(50) NOT NULL COMMENT '小程序编码',
  `company_id` BIGINT NOT NULL COMMENT '所属公司ID',
  `mini_program_type` VARCHAR(20) NOT NULL DEFAULT 'retail' COMMENT '小程序类型: retail-零售, service-服务, catering-餐饮, other-其他',
  `industry` VARCHAR(100) COMMENT '所属行业',
  `logo` VARCHAR(500) COMMENT '小程序Logo',
  `description` TEXT COMMENT '小程序描述',
  `status` VARCHAR(20) NOT NULL DEFAULT 'inactive' COMMENT '状态: inactive-未激活, active-已激活, suspended-暂停, deleted-已删除',
  `expire_time` DATE COMMENT '授权过期时间',
  `qr_code` VARCHAR(500) COMMENT '小程序二维码URL',
  `remark` VARCHAR(500) COMMENT '备注',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` VARCHAR(50) COMMENT '创建人',
  `update_by` VARCHAR(50) COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_id` (`app_id`),
  UNIQUE KEY `uk_mini_program_code` (`mini_program_code`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序表';

-- =============================================
-- 4. 角色表 (roles)
-- =============================================
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `id` BIGINT NOT NULL COMMENT '角色ID（雪花算法）',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
  `role_type` VARCHAR(20) NOT NULL COMMENT '角色类型: platform-平台, company-公司',
  `description` VARCHAR(500) COMMENT '角色描述',
  `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active-激活, inactive-未激活',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_role_type` (`role_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- =============================================
-- 5. 用户角色关联表 (user_roles)
-- =============================================
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =============================================
-- 6. 权限表 (permissions)
-- =============================================
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
  `id` BIGINT NOT NULL COMMENT '权限ID（雪花算法）',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `permission_type` VARCHAR(20) NOT NULL COMMENT '权限类型: menu-菜单, button-按钮, api-接口',
  `route_path` VARCHAR(200) COMMENT '路由路径',
  `component` VARCHAR(200) COMMENT '组件路径',
  `icon` VARCHAR(100) COMMENT '图标',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见: 1-可见, 0-隐藏',
  `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active-激活, inactive-未激活',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_permission_type` (`permission_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- =============================================
-- 7. 角色权限关联表 (role_permissions)
-- =============================================
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions` (
  `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- =============================================
-- 验证表结构
-- =============================================
SELECT 'Database schema created successfully!' AS message;
SHOW TABLES;
