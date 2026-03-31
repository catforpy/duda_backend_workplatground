-- =====================================================
-- 租户Schema初始化脚本
-- 功能: 为新租户创建独立的数据库Schema
-- 使用: TenantSchemaManager.createTenantSchema(tenantId, tenantCode)
-- 数据库: MySQL 8.0+
-- =====================================================

-- 参数说明:
-- $1 = tenantCode (租户编码,如: company001)
-- $2 = tenantId (租户ID)
-- 注意: 本脚本由TenantSchemaManager动态执行，会替换$1和$2占位符

-- 3. 创建用户表
CREATE TABLE @tenant_code.users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    real_name VARCHAR(100) COMMENT '真实姓名',
    avatar VARCHAR(500) COMMENT '头像URL',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '用户状态: ACTIVE-激活, LOCKED-锁定, DELETED-删除',
    user_type VARCHAR(20) DEFAULT 'MEMBER' COMMENT '用户类型: ADMIN-管理员, MEMBER-普通成员',
    last_login_at DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_username (tenant_id, username),
    INDEX idx_email (tenant_id, email),
    INDEX idx_phone (tenant_id, phone),
    INDEX idx_status (tenant_id, status)
) COMMENT='用户表';

-- 4. 创建配置表
CREATE TABLE @tenant_code.configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(20) DEFAULT 'STRING' COMMENT '配置类型: STRING-字符串, NUMBER-数字, BOOLEAN-布尔, JSON-JSON对象',
    description VARCHAR(500) COMMENT '配置描述',
    is_system BOOLEAN DEFAULT FALSE COMMENT '是否系统配置',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_tenant_key (tenant_id, config_key),
    INDEX idx_tenant_id (tenant_id)
) COMMENT='租户配置表';

-- 5. 创建订单表
CREATE TABLE @tenant_code.orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update时间',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_order_no (tenant_id, order_no),
    INDEX idx_customer_id (tenant_id, customer_id)
) COMMENT='订单表';

-- 6. 创建产品表
CREATE TABLE @tenant_code.products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '产品ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
    price DECIMAL(10,2) NOT NULL COMMENT '产品价格',
    description TEXT COMMENT '产品描述',
    stock INT DEFAULT 0 COMMENT '库存数量',
    status VARCHAR(20) DEFAULT 'ON_SALE' COMMENT '产品状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_status (tenant_id, status)
) COMMENT='产品表';

-- 7. 初始化默认配置
INSERT INTO @tenant_code.configs (tenant_id, config_key, config_value, config_type, description, is_system) VALUES
(@tenant_id, 'system.name', '租户管理系统', 'STRING', '系统名称', TRUE),
(@tenant_id, 'timezone', 'Asia/Shanghai', 'STRING', '时区设置', TRUE),
(@tenant_id, 'language', 'zh-CN', 'STRING', '语言设置', TRUE),
(@tenant_id, 'max.users', '10000', 'NUMBER', '最大用户数限制', TRUE),
(@tenant_id, 'max.storage.size', '107374182400', 'NUMBER', '最大存储空间(字节)', TRUE),
(@tenant_id, 'session.timeout', '7200', 'NUMBER', '会话超时时间(秒)', TRUE);

-- 8. 创建Schema使用说明
-- 在该Schema中执行SQL时,需要先设置搜索路径
-- SET SEARCH_PATH TO @tenant_code;
