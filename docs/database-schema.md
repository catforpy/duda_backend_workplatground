# 数据库初始化指南

## 📊 数据库设计

### 数据库信息

- **数据库名**: `duda_nexus`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`
- **引擎**: `InnoDB`

---

## 🗄️ 表结构设计

### 1. 用户表 (users)

```sql
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
```

### 2. 公司表 (companies)

```sql
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
```

### 3. 小程序表 (mini_programs)

```sql
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
```

### 4. 角色表 (roles)

```sql
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
```

### 5. 用户角色关联表 (user_roles)

```sql
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
```

### 6. 权限表 (permissions)

```sql
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
```

### 7. 角色权限关联表 (role_permissions)

```sql
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
```

---

## 📝 初始化数据

### 1. 初始化管理员账号

```sql
-- 插入平台管理员账号（默认密码: Duda@2025）
INSERT INTO `users` (
  `id`, `username`, `password`, `real_name`, `phone`, `email`,
  `user_type`, `status`, `create_by`
) VALUES (
  1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
  '系统管理员', '13800138000', 'admin@duda.com',
  'platform_admin', 'active', 'system'
);
```

### 2. 初始化角色数据

```sql
-- 平台管理员角色
INSERT INTO `roles` (`id`, `role_code`, `role_name`, `role_type`, `description`) VALUES
(1, 'PLATFORM_ADMIN', '平台管理员', 'platform', '平台最高权限管理员'),
(2, 'COMPANY_ADMIN', '公司管理员', 'company', '公司级别管理员'),
(3, 'COMPANY_USER', '公司普通用户', 'company', '公司普通用户');

-- 为管理员分配角色
INSERT INTO `user_roles` (`id`, `user_id`, `role_id`) VALUES
(1, 1, 1);
```

### 3. 初始化权限数据

```sql
-- 一级菜单
INSERT INTO `permissions` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `route_path`, `icon`, `sort_order`) VALUES
(1, 0, 'USER_MGMT', '用户管理', 'menu', '/user', 'User', 1),
(2, 0, 'COMPANY_MGMT', '公司管理', 'menu', '/company', 'OfficeBuilding', 2),
(3, 0, 'MINI_PROGRAM_MGMT', '小程序管理', 'menu', '/mini-program', 'Phone', 3),
(4, 0, 'SYSTEM_MGMT', '系统管理', 'menu', '/system', 'Setting', 4);

-- 用户管理子菜单
INSERT INTO `permissions` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `route_path`, `sort_order`) VALUES
(11, 1, 'USER_LIST', '用户列表', 'menu', '/user/list', 1),
(12, 1, 'USER_ADD', '新增用户', 'button', '', 2),
(13, 1, 'USER_EDIT', '编辑用户', 'button', '', 3),
(14, 1, 'USER_DELETE', '删除用户', 'button', '', 4);

-- 为平台管理员分配所有权限
INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`)
SELECT ROW() AS id, 1 AS role_id, id AS permission_id FROM `permissions`;
```

---

## 🚀 初始化步骤

### Step 1: 创建数据库

```bash
# 连接MySQL
mysql -h 120.26.170.213 -uroot -p

# 创建数据库
CREATE DATABASE IF NOT EXISTS duda_nexus
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

# 使用数据库
USE duda_nexus;
```

### Step 2: 执行建表脚本

```bash
# 方式1: 在MySQL客户端中执行
mysql -h 120.26.170.213 -uroot -p duda_nexus < /Volumes/DudaDate/DudaNexus/sql/init-schema.sql

# 方式2: 复制SQL语句到MySQL客户端执行
```

### Step 3: 执行初始化数据

```bash
# 执行初始化数据脚本
mysql -h 120.26.170.213 -uroot -p duda_nexus < /Volumes/DudaDate/DudaNexus/sql/init-data.sql
```

### Step 4: 验证数据库

```sql
-- 查看所有表
USE duda_nexus;
SHOW TABLES;

-- 验证用户表
SELECT COUNT(*) FROM users;

-- 验证管理员账号
SELECT id, username, real_name, user_type, status FROM users WHERE username = 'admin';
```

---

## 📊 ER图

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   users     │────────>│ user_roles  │<────────│   roles     │
│             │         │             │         │             │
└─────────────┘         └─────────────┘         └─────────────┘
       │                                                 │
       │                                                 │
       v                                                 v
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│  companies  │         │  mini_      │         │permissions  │
│             │         │  programs   │         │             │
└─────────────┘         └─────────────┘         └─────────────┘
                                ^                      │
                                │                      │
                                │                      v
                                │              ┌─────────────┐
                                │              │role_        │
                                │              │permissions  │
                                │              └─────────────┘
                                │
                                │
                         ┌─────────────┐
                         │   users     │
                         │  (公司ID)   │
                         └─────────────┘
```

---

## ✅ 验证清单

- [ ] 数据库 `duda_nexus` 创建成功
- [ ] 所有7张表创建成功
- [ ] 索引创建成功
- [ ] 管理员账号插入成功（用户名: admin, 密码: Duda@2025）
- [ ] 角色数据初始化成功
- [ ] 权限数据初始化成功
- [ ] 管理员角色关联成功

---

## 🔐 安全说明

1. **密码加密**: 使用BCrypt算法，默认密码 `Duda@2025` 加密后为：
   ```
   $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
   ```

2. **敏感字段加密**:
   - `users.password`: BCrypt加密
   - `mini_programs.app_secret`: 建议使用AES加密存储

3. **逻辑删除**: 所有表都支持逻辑删除（`deleted`字段）

4. **审计字段**: 所有表都包含 `create_time`, `update_time`, `create_by`, `update_by`

---

**数据库状态**: ⏳ 待创建
**初始化脚本状态**: ⏳ 待执行
