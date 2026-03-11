-- =============================================
-- DudaNexus 数据库初始化数据脚本
-- 插入初始数据：管理员、角色、权限
-- =============================================

USE duda_nexus;

-- =============================================
-- 1. 插入平台管理员账号
-- 默认用户名: admin
-- 默认密码: Duda@2025 (BCrypt加密后)
-- =============================================
INSERT INTO `users` (
  `id`, `username`, `password`, `real_name`, `phone`, `email`,
  `user_type`, `status`, `create_by`
) VALUES (
  1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
  '系统管理员', '13800138000', 'admin@duda.com',
  'platform_admin', 'active', 'system'
);

-- =============================================
-- 2. 插入角色数据
-- =============================================
INSERT INTO `roles` (`id`, `role_code`, `role_name`, `role_type`, `description`, `status`) VALUES
(1, 'PLATFORM_ADMIN', '平台管理员', 'platform', '平台最高权限管理员，拥有所有权限', 'active'),
(2, 'COMPANY_ADMIN', '公司管理员', 'company', '公司级别管理员，管理公司内部用户和小程序', 'active'),
(3, 'COMPANY_USER', '公司普通用户', 'company', '公司普通用户，只有基本查看权限', 'active'),
(4, 'BACKEND_ADMIN', '后台管理员', 'platform', '后台运维管理员，负责系统监控和维护', 'active'),
(5, 'SERVICE_PROVIDER', '服务商', 'company', '服务商用户，可管理自己的小程序', 'active');

-- =============================================
-- 3. 为管理员分配角色
-- =============================================
INSERT INTO `user_roles` (`id`, `user_id`, `role_id`) VALUES
(1, 1, 1);  -- admin用户 -> 平台管理员角色

-- =============================================
-- 4. 插入权限数据（菜单树结构）
-- =============================================

-- 4.1 一级菜单
INSERT INTO `permissions` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `route_path`, `icon`, `sort_order`, `visible`) VALUES
(1, 0, 'DASHBOARD', '控制台', 'menu', '/dashboard', 'Dashboard', 0, 1),
(2, 0, 'USER_MGMT', '用户管理', 'menu', '/user', 'User', 1, 1),
(3, 0, 'COMPANY_MGMT', '公司管理', 'menu', '/company', 'OfficeBuilding', 2, 1),
(4, 0, 'MINI_PROGRAM_MGMT', '小程序管理', 'menu', '/mini-program', 'Phone', 3, 1),
(5, 0, 'AUTH_MGMT', '权限管理', 'menu', '/auth', 'Lock', 4, 1),
(6, 0, 'SYSTEM_MGMT', '系统管理', 'menu', '/system', 'Setting', 5, 1);

-- 4.2 用户管理子菜单和按钮
INSERT INTO `permissions` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `route_path`, `component`, `sort_order`, `visible`) VALUES
(11, 2, 'USER_LIST', '用户列表', 'menu', '/user/list', 'user/UserList', 1, 1),
(12, 2, 'USER_ADD', '新增用户', 'button', '', '', 2, 0),
(13, 2, 'USER_EDIT', '编辑用户', 'button', '', '', 3, 0),
(14, 2, 'USER_DELETE', '删除用户', 'button', '', '', 4, 0),
(15, 2, 'USER_RESET_PWD', '重置密码', 'button', '', '', 5, 0),
(16, 2, 'USER_EXPORT', '导出用户', 'button', '', '', 6, 0);

-- 4.3 公司管理子菜单和按钮
INSERT INTO `permissions` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `route_path`, `component`, `sort_order`, `visible`) VALUES
(21, 3, 'COMPANY_LIST', '公司列表', 'menu', '/company/list', 'company/CompanyList', 1, 1),
(22, 3, 'COMPANY_ADD', '新增公司', 'button', '', '', 2, 0),
(23, 3, 'COMPANY_EDIT', '编辑公司', 'button', '', '', 3, 0),
(24, 3, 'COMPANY_AUDIT', '审核公司', 'button', '', '', 4, 0),
(25, 3, 'COMPANY_DELETE', '删除公司', 'button', '', '', 5, 0),
(26, 3, 'COMPANY_EXPORT', '导出公司', 'button', '', '', 6, 0);

-- 4.4 小程序管理子菜单和按钮
INSERT INTO `permissions` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `route_path`, `component`, `sort_order`, `visible`) VALUES
(31, 4, 'MINI_PROGRAM_LIST', '小程序列表', 'menu', '/mini-program/list', 'miniprogram/MiniProgramList', 1, 1),
(32, 4, 'MINI_PROGRAM_ADD', '新增小程序', 'button', '', '', 2, 0),
(33, 4, 'MINI_PROGRAM_EDIT', '编辑小程序', 'button', '', '', 3, 0),
(34, 4, 'MINI_PROGRAM_DELETE', '删除小程序', 'button', '', '', 4, 0),
(35, 4, 'MINI_PROGRAM_AUTH', '小程序授权', 'button', '', '', 5, 0),
(36, 4, 'MINI_PROGRAM_CONFIG', '小程序配置', 'button', '', '', 6, 0);

-- 4.5 权限管理子菜单和按钮
INSERT INTO `permissions` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `route_path`, `component`, `sort_order`, `visible`) VALUES
(41, 5, 'ROLE_MGMT', '角色管理', 'menu', '/auth/role', 'auth/RoleList', 1, 1),
(42, 5, 'PERMISSION_MGMT', '权限管理', 'menu', '/auth/permission', 'auth/PermissionList', 2, 1),
(43, 5, 'ROLE_ADD', '新增角色', 'button', '', '', 3, 0),
(44, 5, 'ROLE_EDIT', '编辑角色', 'button', '', '', 4, 0),
(45, 5, 'ROLE_DELETE', '删除角色', 'button', '', '', 5, 0),
(46, 5, 'ROLE_ASSIGN_PERMISSION', '分配权限', 'button', '', '', 6, 0);

-- 4.6 系统管理子菜单和按钮
INSERT INTO `permissions` (`id`, `parent_id`, `permission_code`, `permission_name`, `permission_type`, `route_path`, `component`, `sort_order`, `visible`) VALUES
(51, 6, 'SYSTEM_CONFIG', '系统配置', 'menu', '/system/config', 'system/Config', 1, 1),
(52, 6, 'SYSTEM_LOG', '操作日志', 'menu', '/system/log', 'system/Log', 2, 1),
(53, 6, 'SYSTEM_MONITOR', '系统监控', 'menu', '/system/monitor', 'system/Monitor', 3, 1),
(54, 6, 'SYSTEM_DICT', '字典管理', 'menu', '/system/dict', 'system/Dict', 4, 1);

-- =============================================
-- 5. 为平台管理员分配所有权限
-- =============================================
INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`)
SELECT
  (@rownum:=@rownum+1) AS id,
  1 AS role_id,
  id AS permission_id
FROM `permissions`, (SELECT @rownum:=0) AS r
WHERE id <= 54;  -- 为所有权限分配给平台管理员

-- =============================================
-- 6. 为公司管理员分配部分权限
-- =============================================
-- 公司管理员权限：查看和编辑用户、小程序管理
INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`)
SELECT
  (@rownum:=@rownum+1) AS id,
  2 AS role_id,  -- 公司管理员
  id AS permission_id
FROM `permissions`, (SELECT @rownum:=54) AS r
WHERE id IN (
  1, 11, 12, 13, 15, 16,  -- 控制台和用户管理（不含删除）
  31, 32, 33, 35, 36       -- 小程序管理（不含删除）
);

-- =============================================
-- 7. 为公司普通用户分配只读权限
-- =============================================
INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`)
SELECT
  (@rownum:=@rownum+1) AS id,
  3 AS role_id,  -- 公司普通用户
  id AS permission_id
FROM `permissions`, (SELECT @rownum:=67) AS r
WHERE id IN (
  1, 11,  -- 控制台和用户列表（只读）
  31       -- 小程序列表（只读）
);

-- =============================================
-- 8. 为服务商分配权限
-- =============================================
INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`)
SELECT
  (@rownum:=@rownum+1) AS id,
  5 AS role_id,  -- 服务商
  id AS permission_id
FROM `permissions`, (SELECT @rownum:=70) AS r
WHERE id IN (
  1, 11, 13, 15,       -- 控制台和用户管理（只能编辑自己和重置密码）
  31, 32, 33, 35, 36   -- 小程序管理（完整权限）
);

-- =============================================
-- 验证数据
-- =============================================
SELECT 'Data initialization completed!' AS message;

SELECT
  'Users' AS table_name,
  COUNT(*) AS record_count
FROM users
UNION ALL
SELECT
  'Roles',
  COUNT(*)
FROM roles
UNION ALL
SELECT
  'Permissions',
  COUNT(*)
FROM permissions
UNION ALL
SELECT
  'User Roles',
  COUNT(*)
FROM user_roles
UNION ALL
SELECT
  'Role Permissions',
  COUNT(*)
FROM role_permissions;

-- 显示管理员账号信息
SELECT
  id AS '用户ID',
  username AS '用户名',
  real_name AS '真实姓名',
  user_type AS '用户类型',
  status AS '状态',
  '默认密码: Duda@2025' AS '密码提示'
FROM users
WHERE username = 'admin';
