-- =============================================
-- 修复数据库注释乱码问题
-- 重新创建表结构并修复注释
-- =============================================

USE duda_nexus;

-- 1. 修复 users 表注释
ALTER TABLE users COMMENT='用户表';

ALTER TABLE users MODIFY COLUMN id BIGINT COMMENT '用户ID（雪花算法）';
ALTER TABLE users MODIFY COLUMN username VARCHAR(50) COMMENT '用户名';
ALTER TABLE users MODIFY COLUMN password VARCHAR(255) COMMENT '密码（BCrypt加密）';
ALTER TABLE users MODIFY COLUMN real_name VARCHAR(100) COMMENT '真实姓名';
ALTER TABLE users MODIFY COLUMN phone VARCHAR(20) COMMENT '手机号';
ALTER TABLE users MODIFY COLUMN email VARCHAR(100) COMMENT '邮箱';
ALTER TABLE users MODIFY COLUMN avatar VARCHAR(500) COMMENT '头像URL';
ALTER TABLE users MODIFY COLUMN user_type VARCHAR(20) COMMENT '用户类型: platform_admin-平台管理员, service_provider-服务商, platform_account-都达账户, backend_admin-后台管理员';
ALTER TABLE users MODIFY COLUMN status VARCHAR(20) COMMENT '状态: active-激活, inactive-未激活, suspended-暂停, deleted-已删除';
ALTER TABLE users MODIFY COLUMN company_id BIGINT COMMENT '所属公司ID';
ALTER TABLE users MODIFY COLUMN department VARCHAR(100) COMMENT '部门';
ALTER TABLE users MODIFY COLUMN position VARCHAR(100) COMMENT '职位';
ALTER TABLE users MODIFY COLUMN province VARCHAR(50) COMMENT '省份';
ALTER TABLE users MODIFY COLUMN city VARCHAR(50) COMMENT '城市';
ALTER TABLE users MODIFY COLUMN address VARCHAR(255) COMMENT '详细地址';
ALTER TABLE users MODIFY COLUMN last_login_time DATETIME COMMENT '最后登录时间';
ALTER TABLE users MODIFY COLUMN last_login_ip VARCHAR(50) COMMENT '最后登录IP';
ALTER TABLE users MODIFY COLUMN remark VARCHAR(500) COMMENT '备注';
ALTER TABLE users MODIFY COLUMN deleted TINYINT COMMENT '逻辑删除: 0-未删除, 1-已删除';
ALTER TABLE users MODIFY COLUMN create_time DATETIME COMMENT '创建时间';
ALTER TABLE users MODIFY COLUMN update_time DATETIME COMMENT '更新时间';
ALTER TABLE users MODIFY COLUMN create_by VARCHAR(50) COMMENT '创建人';
ALTER TABLE users MODIFY COLUMN update_by VARCHAR(50) COMMENT '更新人';

-- 2. 修复 companies 表注释
ALTER TABLE companies COMMENT='公司表';
ALTER TABLE companies MODIFY COLUMN company_name VARCHAR(200) COMMENT '公司名称';
ALTER TABLE companies MODIFY COLUMN company_code VARCHAR(50) COMMENT '公司编码';
ALTER TABLE companies MODIFY COLUMN license_no VARCHAR(100) COMMENT '营业执照号';
ALTER TABLE companies MODIFY COLUMN legal_person VARCHAR(100) COMMENT '法人代表';
ALTER TABLE companies MODIFY COLUMN legal_person_phone VARCHAR(20) COMMENT '法人电话';
ALTER TABLE companies MODIFY COLUMN contact_person VARCHAR(100) COMMENT '联系人';
ALTER TABLE companies MODIFY COLUMN contact_phone VARCHAR(20) COMMENT '联系电话';
ALTER TABLE companies MODIFY COLUMN contact_email VARCHAR(100) COMMENT '联系邮箱';
ALTER TABLE companies MODIFY COLUMN company_type VARCHAR(20) COMMENT '公司类型: service_provider-服务商, platform-平台方';

-- 3. 修复 mini_programs 表注释
ALTER TABLE mini_programs COMMENT='小程序表';
ALTER TABLE mini_programs MODIFY COLUMN app_id VARCHAR(100) COMMENT '微信AppID';
ALTER TABLE mini_programs MODIFY COLUMN app_secret VARCHAR(255) COMMENT '微信AppSecret（加密存储）';
ALTER TABLE mini_programs MODIFY COLUMN mini_program_name VARCHAR(200) COMMENT '小程序名称';
ALTER TABLE mini_programs MODIFY COLUMN mini_program_code VARCHAR(50) COMMENT '小程序编码';
ALTER TABLE mini_programs MODIFY COLUMN company_id BIGINT COMMENT '所属公司ID';
ALTER TABLE mini_programs MODIFY COLUMN mini_program_type VARCHAR(20) COMMENT '小程序类型: retail-零售, service-服务, catering-餐饮, other-其他';

-- 4. 修复 roles 表注释
ALTER TABLE roles COMMENT='角色表';
ALTER TABLE roles MODIFY COLUMN role_code VARCHAR(50) COMMENT '角色编码';
ALTER TABLE roles MODIFY COLUMN role_name VARCHAR(100) COMMENT '角色名称';
ALTER TABLE roles MODIFY COLUMN role_type VARCHAR(20) COMMENT '角色类型: platform-平台, company-公司';

-- 5. 修复 permissions 表注释
ALTER TABLE permissions COMMENT='权限表';
ALTER TABLE permissions MODIFY COLUMN parent_id BIGINT COMMENT '父权限ID';
ALTER TABLE permissions MODIFY COLUMN permission_code VARCHAR(100) COMMENT '权限编码';
ALTER TABLE permissions MODIFY COLUMN permission_name VARCHAR(100) COMMENT '权限名称';
ALTER TABLE permissions MODIFY COLUMN permission_type VARCHAR(20) COMMENT '权限类型: menu-菜单, button-按钮, api-接口';
ALTER TABLE permissions MODIFY COLUMN route_path VARCHAR(200) COMMENT '路由路径';
ALTER TABLE permissions MODIFY COLUMN component VARCHAR(200) COMMENT '组件路径';
ALTER TABLE permissions MODIFY COLUMN icon VARCHAR(100) COMMENT '图标';
ALTER TABLE permissions MODIFY COLUMN sort_order INT COMMENT '排序';

-- 6. 修复 user_roles 表注释
ALTER TABLE user_roles COMMENT='用户角色关联表';
ALTER TABLE user_roles MODIFY COLUMN user_id BIGINT COMMENT '用户ID';
ALTER TABLE user_roles MODIFY COLUMN role_id BIGINT COMMENT '角色ID';

-- 7. 修复 role_permissions 表注释
ALTER TABLE role_permissions COMMENT='角色权限关联表';
ALTER TABLE role_permissions MODIFY COLUMN role_id BIGINT COMMENT '角色ID';
ALTER TABLE role_permissions MODIFY COLUMN permission_id BIGINT COMMENT '权限ID';

-- 验证修复结果
SELECT '表注释修复完成！' AS message;

-- 显示所有表注释
SELECT
  TABLE_NAME AS '表名',
  TABLE_COMMENT AS '表注释'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'duda_nexus'
ORDER BY TABLE_NAME;
