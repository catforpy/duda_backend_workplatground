-- =====================================================
-- duda_tenant 数据库初始化脚本
-- 功能: 创建租户管理中心数据库
-- 数据库: duda_tenant
-- 说明: 本数据库用于租户管理，不涉及租户业务数据隔离
-- =====================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS duda_tenant
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE duda_tenant;

-- =====================================================
-- 2. 租户核心表
-- =====================================================

-- 2.1 租户表 (tenants)
DROP TABLE IF EXISTS tenants;
CREATE TABLE tenants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    tenant_code VARCHAR(50) NOT NULL COMMENT '租户编码，全局唯一，用于业务系统识别租户',
    tenant_name VARCHAR(200) NOT NULL COMMENT '租户名称',
    tenant_type VARCHAR(20) NOT NULL DEFAULT 'trial' COMMENT '租户类型（trial-试用版，basic-基础版，professional-专业版，enterprise-企业版）',
    package_id BIGINT COMMENT '当前套餐ID，关联tenant_packages.id',
    package_expire_time DATETIME COMMENT '套餐到期时间',
    tenant_status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '租户状态（active-激活，suspended-暂停，frozen-冻结，expired-过期，deleted-已删除）',
    expire_time DATETIME COMMENT '过期时间，NULL表示永久有效',
    max_users INT NOT NULL DEFAULT 100 COMMENT '最大用户数限制',
    max_admins INT NOT NULL DEFAULT 5 COMMENT '最大管理员数量限制',
    max_storage_size BIGINT NOT NULL DEFAULT 10737418240 COMMENT '最大存储空间（字节），默认10GB',
    max_api_calls_per_day INT NOT NULL DEFAULT 10000 COMMENT '每日最大API调用次数限制',
    contact_person VARCHAR(100) COMMENT '联系人姓名',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    extend_fields JSON COMMENT '扩展字段（JSON格式），存储自定义配置',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号（乐观锁）',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常，1-已删除）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_tenant_code (tenant_code),
    INDEX idx_tenant_type (tenant_type),
    INDEX idx_tenant_status (tenant_status),
    INDEX idx_package_id (package_id),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';

-- 2.2 租户配置表 (tenant_configs)
DROP TABLE IF EXISTS tenant_configs;
CREATE TABLE tenant_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    tenant_id BIGINT NOT NULL COMMENT '租户ID，关联tenants.id',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键（如：logo_url, primary_color, smtp_config等）',
    config_value JSON NOT NULL COMMENT '配置值（JSON格式），支持复杂配置结构',
    config_type VARCHAR(20) NOT NULL COMMENT '配置类型（ui-界面配置，email-邮件配置，sms-短信配置，storage-存储配置等）',
    config_desc VARCHAR(500) COMMENT '配置描述',
    is_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用（0-禁用，1-启用）',
    version INT NOT NULL DEFAULT 1 COMMENT '版本号（乐观锁）',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常，1-已删除）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_tenant_config (tenant_id, config_key),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_config_key (config_key),
    INDEX idx_config_type (config_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户配置表';

-- 2.3 租户套餐表 (tenant_packages)
DROP TABLE IF EXISTS tenant_packages;
CREATE TABLE tenant_packages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    package_code VARCHAR(50) NOT NULL COMMENT '套餐编码（如：trial, basic, professional, enterprise）',
    package_name VARCHAR(100) NOT NULL COMMENT '套餐名称（如：试用版、基础版、专业版、企业版）',
    package_type VARCHAR(20) NOT NULL COMMENT '套餐类型（trial-试用版，paid-付费版，custom-定制版）',
    max_users INT NOT NULL COMMENT '最大用户数',
    max_storage_size BIGINT NOT NULL COMMENT '最大存储空间（字节）',
    max_api_calls_per_day INT NOT NULL COMMENT '每日最大API调用次数',
    price_monthly DECIMAL(10,2) NOT NULL COMMENT '月付价格',
    price_yearly DECIMAL(10,2) NOT NULL COMMENT '年付价格',
    features JSON COMMENT '套餐特性列表（JSON数组格式），如：["数据导出", "高级报表", "API访问"]',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序序号',
    is_active TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用（0-禁用，1-启用）',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常，1-已删除）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_package_code (package_code),
    INDEX idx_package_type (package_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户套餐表';

-- 2.4 租户API密钥表 (tenant_api_keys)
DROP TABLE IF EXISTS tenant_api_keys;
CREATE TABLE tenant_api_keys (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    tenant_id BIGINT NOT NULL COMMENT '租户ID，关联tenants.id',
    access_key VARCHAR(100) NOT NULL COMMENT '访问密钥（Access Key），全局唯一',
    secret_key VARCHAR(200) NOT NULL COMMENT '密钥（Secret Key），加密存储',
    key_name VARCHAR(100) NOT NULL COMMENT '密钥名称（如：生产环境、测试环境）',
    key_status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '密钥状态（active-激活，suspended-暂停，revoked-已吊销，expired-已过期）',
    permissions JSON COMMENT '权限列表（JSON数组格式），如：["user.read", "user.write"]',
    ip_whitelist JSON COMMENT 'IP白名单（JSON数组格式），限制访问来源IP',
    rate_limit INT NOT NULL DEFAULT 1000 COMMENT '速率限制（每分钟最大请求数）',
    expire_time DATETIME COMMENT '过期时间，NULL表示永久有效',
    last_used_time DATETIME COMMENT '最后使用时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常，1-已删除）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_access_key (access_key),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_key_status (key_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户API密钥表';

-- 2.5 租户统计表 (tenant_statistics)
DROP TABLE IF EXISTS tenant_statistics;
CREATE TABLE tenant_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    tenant_id BIGINT NOT NULL COMMENT '租户ID，关联tenants.id',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_hour TINYINT COMMENT '统计小时（0-23），NULL表示全天统计',
    data_source VARCHAR(20) NOT NULL DEFAULT 'auto' COMMENT '数据来源（auto-自动统计，manual-手动录入）',
    last_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    user_count INT NOT NULL DEFAULT 0 COMMENT '用户数量',
    merchant_count INT NOT NULL DEFAULT 0 COMMENT '商户数量',
    mini_program_count INT NOT NULL DEFAULT 0 COMMENT '小程序数量',
    storage_used_size BIGINT NOT NULL DEFAULT 0 COMMENT '已使用存储空间（字节）',
    api_call_count INT NOT NULL DEFAULT 0 COMMENT 'API调用次数',
    order_count INT NOT NULL DEFAULT 0 COMMENT '订单数量',
    order_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '订单金额',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_tenant_date_hour (tenant_id, stat_date, stat_hour),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_stat_date (stat_date),
    INDEX idx_data_source (data_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户统计表';

-- 2.6 租户操作日志表 (tenant_operation_logs)
DROP TABLE IF EXISTS tenant_operation_logs;
CREATE TABLE tenant_operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    tenant_id BIGINT NOT NULL COMMENT '租户ID，关联tenants.id',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型（create-创建，update-更新，delete-删除，suspend-暂停，activate-激活等）',
    operation_desc VARCHAR(500) NOT NULL COMMENT '操作描述',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) NOT NULL COMMENT '操作人姓名',
    operator_type VARCHAR(20) NOT NULL DEFAULT 'platform' COMMENT '操作人类型（platform-平台管理员，tenant-租户管理员）',
    tenant_admin_id BIGINT COMMENT '如果operator_type=tenant，记录租户管理员ID',
    operator_ip VARCHAR(50) COMMENT '操作人IP地址',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数（JSON格式）',
    response_result VARCHAR(20) NOT NULL COMMENT '操作结果（success-成功，failed-失败，partial-部分成功）',
    error_message VARCHAR(500) COMMENT '错误信息',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',

    INDEX idx_tenant_id (tenant_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operator_id (operator_id),
    INDEX idx_operator_type (operator_type),
    INDEX idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户操作日志表';

-- 2.7 租户用户关系表 (tenant_user_relations)
DROP TABLE IF EXISTS tenant_user_relations;
CREATE TABLE tenant_user_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    tenant_id BIGINT NOT NULL COMMENT '租户ID，关联tenants.id',
    user_id BIGINT NOT NULL COMMENT '用户ID，关联duda_nexus.users_XX.id',
    user_shard TINYINT NOT NULL COMMENT '用户分片编号（0-99），用于定位用户所在分片表',
    role_code VARCHAR(50) NOT NULL DEFAULT 'TENANT_USER' COMMENT '角色编码（TENANT_ADMIN-租户管理员，TENANT_USER-租户用户等）',
    is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '是否为主租户（1-是，0-否）',
    status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态（active-激活，inactive-未激活，suspended-暂停，deleted-已删除）',
    join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入租户时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常，1-已删除）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_tenant_user (tenant_id, user_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_user_id (user_id, user_shard),
    INDEX idx_role_code (role_code),
    INDEX idx_is_primary (is_primary)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户用户关系表';

-- =====================================================
-- 3. 扩展业务表
-- =====================================================

-- 3.1 租户套餐变更历史表 (tenant_package_history)
DROP TABLE IF EXISTS tenant_package_history;
CREATE TABLE tenant_package_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    tenant_id BIGINT NOT NULL COMMENT '租户ID，关联tenants.id',
    tenant_code VARCHAR(50) NOT NULL COMMENT '租户编码（冗余字段，方便查询）',
    old_package_id BIGINT COMMENT '原套餐ID',
    old_package_code VARCHAR(50) COMMENT '原套餐编码（冗余字段）',
    old_package_name VARCHAR(100) COMMENT '原套餐名称（冗余字段）',
    new_package_id BIGINT NOT NULL COMMENT '新套餐ID',
    new_package_code VARCHAR(50) NOT NULL COMMENT '新套餐编码（冗余字段）',
    new_package_name VARCHAR(100) NOT NULL COMMENT '新套餐名称（冗余字段）',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型（upgrade-升级，downgrade-降级，renew-续费）',
    change_amount DECIMAL(10,2) COMMENT '变更金额（正数表示补差价，负数表示退款）',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) NOT NULL COMMENT '操作人姓名',
    operator_type VARCHAR(20) NOT NULL DEFAULT 'platform' COMMENT '操作人类型（platform-平台管理员，tenant-租户管理员）',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_tenant_id (tenant_id),
    INDEX idx_tenant_code (tenant_code),
    INDEX idx_change_type (change_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户套餐变更历史表';

-- 3.2 租户订单表 (tenant_orders)
DROP TABLE IF EXISTS tenant_orders;
CREATE TABLE tenant_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    tenant_id BIGINT NOT NULL COMMENT '租户ID，关联tenants.id',
    tenant_code VARCHAR(50) NOT NULL COMMENT '租户编码（冗余字段，方便查询）',
    order_no VARCHAR(50) NOT NULL COMMENT '订单号（格式：TNT + yyyyMMddHHmmss + 6位随机数）',
    package_id BIGINT NOT NULL COMMENT '套餐ID，关联tenant_packages.id',
    package_code VARCHAR(50) NOT NULL COMMENT '套餐编码（冗余字段）',
    package_name VARCHAR(100) NOT NULL COMMENT '套餐名称（冗余字段）',
    order_type VARCHAR(20) NOT NULL COMMENT '订单类型（new-新购，renew-续费，upgrade-升级）',
    order_amount DECIMAL(10,2) NOT NULL COMMENT '订单金额（单位：元）',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额（单位：元）',
    actual_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额（单位：元）',
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '货币类型（CNY-人民币，USD-美元）',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'unpaid' COMMENT '支付状态（unpaid-未支付，paid-已支付，cancelled-已取消）',
    payment_time DATETIME COMMENT '支付时间',
    payment_method VARCHAR(20) COMMENT '支付方式（alipay-支付宝，wechat-微信支付等）',
    payment_no VARCHAR(100) COMMENT '第三方支付流水号',
    start_time DATETIME NOT NULL COMMENT '套餐开始时间',
    end_time DATETIME NOT NULL COMMENT '套餐结束时间',
    duration_months INT NOT NULL COMMENT '订阅时长（月）',
    contact_person VARCHAR(100) COMMENT '联系人姓名',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    invoice_required TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要发票（0-否，1-是）',
    invoice_status VARCHAR(20) COMMENT '发票状态（not_required-不需要，pending-待开具，issued-已开具）',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常，1-已删除）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_tenant_code (tenant_code),
    INDEX idx_payment_status (payment_status),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户订单表';

-- 3.3 租户API密钥使用日志表 (tenant_api_key_usage_logs)
DROP TABLE IF EXISTS tenant_api_key_usage_logs;
CREATE TABLE tenant_api_key_usage_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    tenant_id BIGINT NOT NULL COMMENT '租户ID，关联tenants.id',
    api_key_id BIGINT NOT NULL COMMENT 'API密钥ID，关联tenant_api_keys.id',
    access_key VARCHAR(100) NOT NULL COMMENT '访问密钥（冗余字段，方便查询）',
    request_id VARCHAR(50) NOT NULL COMMENT '请求ID（全局唯一，可用于关联其他日志）',
    trace_id VARCHAR(50) COMMENT '链路追踪ID',
    request_path VARCHAR(500) COMMENT '请求路径（如：/api/user/info）',
    request_method VARCHAR(10) COMMENT '请求方法（GET/POST/PUT/DELETE/PATCH）',
    request_params TEXT COMMENT '请求参数（JSON格式）',
    response_status INT COMMENT 'HTTP响应状态码（如：200/400/401/403/404/500）',
    response_time INT COMMENT '响应时间（毫秒）',
    response_size BIGINT COMMENT '响应大小（字节）',
    error_code VARCHAR(50) COMMENT '错误码（如果请求失败）',
    error_message VARCHAR(500) COMMENT '错误消息（如果请求失败）',
    client_ip VARCHAR(50) COMMENT '客户端IP地址',
    client_port INT COMMENT '客户端端口',
    user_agent VARCHAR(500) COMMENT 'User-Agent',
    device_type VARCHAR(20) COMMENT '设备类型（ios/android/web/miniapp/unknown）',
    browser_type VARCHAR(50) COMMENT '浏览器类型',
    os_type VARCHAR(50) COMMENT '操作系统类型',
    request_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',

    INDEX idx_tenant_id (tenant_id),
    INDEX idx_api_key_id (api_key_id),
    INDEX idx_request_id (request_id),
    INDEX idx_request_time (request_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API密钥使用日志表';

-- 3.4 租户配置变更历史表 (tenant_config_history)
DROP TABLE IF EXISTS tenant_config_history;
CREATE TABLE tenant_config_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    tenant_id BIGINT NOT NULL COMMENT '租户ID，关联tenants.id',
    config_id BIGINT NOT NULL COMMENT '原配置ID，关联tenant_configs.id',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键（冗余字段，方便查询）',
    config_type VARCHAR(20) NOT NULL COMMENT '配置类型（冗余字段）',
    old_value JSON COMMENT '旧值（JSON格式）',
    new_value JSON COMMENT '新值（JSON格式）',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型（create-新增，update-修改，delete-删除）',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) NOT NULL COMMENT '操作人姓名',
    operator_type VARCHAR(20) NOT NULL DEFAULT 'platform' COMMENT '操作人类型（platform-平台管理员，tenant-租户管理员）',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_tenant_id (tenant_id),
    INDEX idx_config_id (config_id),
    INDEX idx_config_key (config_key),
    INDEX idx_config_type (config_type),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户配置变更历史表';

-- 3.5 租户数据字典表 (tenant_data_dict)
DROP TABLE IF EXISTS tenant_data_dict;
CREATE TABLE tenant_data_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型（tenant_type, package_type, tenant_status, order_type等）',
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码（trial, basic, professional, enterprise）',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典值（试用版，基础版，专业版，企业版）',
    dict_order INT NOT NULL DEFAULT 0 COMMENT '排序序号（数字越小越靠前）',
    dict_desc VARCHAR(500) COMMENT '字典描述',
    is_active TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用（0-禁用，1-启用）',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常，1-已删除）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_type_code (dict_type, dict_code, deleted),
    INDEX idx_dict_type (dict_type),
    INDEX idx_dict_order (dict_order),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户数据字典表';

-- 3.6 租户备份表 (tenant_backups)
DROP TABLE IF EXISTS tenant_backups;
CREATE TABLE tenant_backups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID，自增',
    tenant_id BIGINT NOT NULL COMMENT '租户ID，关联tenants.id',
    tenant_code VARCHAR(50) NOT NULL COMMENT '租户编码（冗余字段，方便查询）',
    backup_type VARCHAR(20) NOT NULL COMMENT '备份类型（full-全量备份，incremental-增量备份）',
    backup_size BIGINT COMMENT '备份大小（字节）',
    backup_status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '备份状态（pending-待执行，running-执行中，success-成功，failed-失败）',
    backup_path VARCHAR(500) COMMENT '备份文件路径（OSS路径）',
    backup_url VARCHAR(500) COMMENT '备份文件下载URL（临时有效）',
    backup_tables TEXT COMMENT '备份的表列表（JSON数组格式）',
    backup_time DATETIME COMMENT '备份时间',
    complete_time DATETIME COMMENT '完成时间',
    error_message VARCHAR(500) COMMENT '错误消息（如果备份失败）',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) NOT NULL COMMENT '操作人姓名',
    operator_type VARCHAR(20) NOT NULL DEFAULT 'platform' COMMENT '操作人类型（platform-平台管理员，tenant-租户管理员，system-系统自动）',
    retention_days INT NOT NULL DEFAULT 30 COMMENT '保留天数（过期后自动删除）',
    remark VARCHAR(500) COMMENT '备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常，1-已删除）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_tenant_id (tenant_id),
    INDEX idx_tenant_code (tenant_code),
    INDEX idx_backup_status (backup_status),
    INDEX idx_backup_time (backup_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户备份表';

-- =====================================================
-- 4. 初始化数据
-- =====================================================

-- 4.1 初始化租户类型字典
INSERT INTO tenant_data_dict (dict_type, dict_code, dict_value, dict_order, dict_desc, is_active) VALUES
('tenant_type', 'trial', '试用版', 1, '试用版租户，功能受限', 1),
('tenant_type', 'basic', '基础版', 2, '基础版租户', 1),
('tenant_type', 'professional', '专业版', 3, '专业版租户', 1),
('tenant_type', 'enterprise', '企业版', 4, '企业版租户，全功能', 1);

-- 4.2 初始化租户状态字典
INSERT INTO tenant_data_dict (dict_type, dict_code, dict_value, dict_order, dict_desc, is_active) VALUES
('tenant_status', 'active', '激活', 1, '租户激活状态', 1),
('tenant_status', 'suspended', '暂停', 2, '租户暂停状态', 1),
('tenant_status', 'frozen', '冻结', 3, '租户冻结状态', 1),
('tenant_status', 'expired', '过期', 4, '租户过期状态', 1),
('tenant_status', 'deleted', '已删除', 5, '租户已删除状态', 1);

-- 4.3 初始化套餐类型字典
INSERT INTO tenant_data_dict (dict_type, dict_code, dict_value, dict_order, dict_desc, is_active) VALUES
('package_type', 'trial', '试用版', 1, '试用套餐', 1),
('package_type', 'paid', '付费版', 2, '付费套餐', 1),
('package_type', 'custom', '定制版', 3, '定制套餐', 1);

-- 4.4 初始化订单类型字典
INSERT INTO tenant_data_dict (dict_type, dict_code, dict_value, dict_order, dict_desc, is_active) VALUES
('order_type', 'new', '新购', 1, '新购订单', 1),
('order_type', 'renew', '续费', 2, '续费订单', 1),
('order_type', 'upgrade', '升级', 3, '升级订单', 1);

-- 4.5 初始化套餐数据
INSERT INTO tenant_packages (package_code, package_name, package_type, max_users, max_storage_size, max_api_calls_per_day, price_monthly, price_yearly, features, sort_order, is_active) VALUES
('trial', '试用版', 'trial', 10, 1073741824, 1000, 0.00, 0.00, '["基础功能", "7天试用"]', 1, 1),
('basic', '基础版', 'paid', 100, 10737418240, 10000, 199.00, 1999.00, '["基础功能", "邮件支持"]', 2, 1),
('professional', '专业版', 'paid', 500, 107374182400, 50000, 999.00, 9999.00, '["高级功能", "优先支持", "数据导出"]', 3, 1),
('enterprise', '企业版', 'paid', 9999, 1073741824000, 1000000, 9999.00, 99999.00, '["全功能", "专属客服", "定制开发", "SLA保障"]', 4, 1);

-- =====================================================
-- 5. 创建索引优化查询性能
-- =====================================================

-- 为tenant_statistics创建复合索引，优化统计查询
CREATE INDEX idx_tenant_date ON tenant_statistics(tenant_id, stat_date);

-- 为tenant_operation_logs创建复合索引，优化日志查询
CREATE INDEX idx_tenant_time ON tenant_operation_logs(tenant_id, operation_time);

-- =====================================================
-- 6. 说明
-- =====================================================
-- 本数据库创建完成
-- 用途: 租户管理中心
-- 下一步: 当创建租户时，使用 init-tenant-schema.sql 创建租户独立Schema
