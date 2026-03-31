-- ========================================
-- 都达云平台数据库升级脚本
-- 版本: v1.0
-- 日期: 2026-03-22
-- 说明:
--   1. user_nexus数据库字段增强
--   2. user_nexus数据库新增表
--   3. 创建duda_trade数据库并新建表
-- ========================================

-- ========================================
-- 第一部分：user_nexus数据库字段增强
-- ========================================
USE user_nexus;

-- 1.1 companies表字段增强
ALTER TABLE companies
  ADD COLUMN IF NOT EXISTS is_service_provider TINYINT DEFAULT 0 COMMENT '是否为服务商: 0-否 1-是',
  ADD COLUMN IF NOT EXISTS service_provider_level VARCHAR(20) COMMENT '服务商等级: platinum/gold/silver/normal',
  ADD COLUMN IF NOT EXISTS service_provider_expire_time DATETIME COMMENT '服务商授权过期时间',
  ADD COLUMN IF NOT EXISTS service_provider_fee_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '服务费抽成比例（0.10 = 10%）',
  ADD COLUMN IF NOT EXISTS wechat_open_platform_appid VARCHAR(100) COMMENT '微信开放平台AppID',
  ADD COLUMN IF NOT EXISTS wechat_authorization_code VARCHAR(200) COMMENT '第三方授权码（代注册时）',
  ADD COLUMN IF NOT EXISTS company_short_name VARCHAR(100) COMMENT '公司简称',
  ADD COLUMN IF NOT EXISTS license_no_type VARCHAR(20) COMMENT '证件类型',
  ADD COLUMN IF NOT EXISTS legal_person_id VARCHAR(18) COMMENT '法人身份证号',
  ADD COLUMN IF NOT EXISTS legal_person_wechat VARCHAR(100) COMMENT '法人微信号（需绑定银行卡）',
  ADD COLUMN IF NOT EXISTS ext_config JSON COMMENT '扩展配置（JSON格式）';

ALTER TABLE companies ADD INDEX IF NOT EXISTS idx_is_service_provider (is_service_provider);
ALTER TABLE companies ADD INDEX IF NOT EXISTS idx_service_provider_level (service_provider_level);

-- 1.2 mini_programs表字段增强
ALTER TABLE mini_programs
  ADD COLUMN IF NOT EXISTS wechat_token VARCHAR(200) COMMENT '微信消息加密Token',
  ADD COLUMN IF NOT EXISTS wechat_encoding_aes_key VARCHAR(500) COMMENT '微信消息加密Key',
  ADD COLUMN IF NOT EXISTS owner_user_id BIGINT COMMENT '所属用户ID（公司管理员）',
  ADD COLUMN IF NOT EXISTS developer_company_id BIGINT COMMENT '开发者公司ID（代开发模式）',
  ADD COLUMN IF NOT EXISTS business_mode VARCHAR(20) DEFAULT 'development' COMMENT '业务模式: development/template/lease/cooperation',
  ADD COLUMN IF NOT EXISTS template_id BIGINT COMMENT '模板ID（如果使用模板）',
  ADD COLUMN IF NOT EXISTS template_name VARCHAR(200) COMMENT '模板名称',
  ADD COLUMN IF NOT EXISTS template_version VARCHAR(20) COMMENT '模板版本',
  ADD COLUMN IF NOT EXISTS first_category VARCHAR(50) COMMENT '一级类目',
  ADD COLUMN IF NOT EXISTS second_category VARCHAR(50) COMMENT '二级类目',
  ADD COLUMN IF NOT EXISTS category_qualifications JSON COMMENT '类目资质文件URL列表（JSON数组）',
  ADD COLUMN IF NOT EXISTS qrcode_url VARCHAR(500) COMMENT '小程序二维码URL',
  ADD COLUMN IF NOT EXISTS intro TEXT COMMENT '小程序简介（微信后台）',
  ADD COLUMN IF NOT EXISTS service_content_types JSON COMMENT '服务内容类型列表（JSON数组，备案用）',
  ADD COLUMN IF NOT EXISTS wechat_certified TINYINT DEFAULT 0 COMMENT '是否已微信认证: 0-否 1-是',
  ADD COLUMN IF NOT EXISTS certification_status VARCHAR(20) DEFAULT 'none' COMMENT '认证状态: none/pending/approved/rejected',
  ADD COLUMN IF NOT EXISTS certification_time DATETIME COMMENT '认证时间',
  ADD COLUMN IF NOT EXISTS certification_expire_time DATETIME COMMENT '认证过期时间',
  ADD COLUMN IF NOT EXISTS filing_status VARCHAR(20) DEFAULT 'none' COMMENT '备案状态: none/pending/approved/rejected/cancelled',
  ADD COLUMN IF NOT EXISTS filing_no VARCHAR(100) COMMENT '备案号',
  ADD COLUMN IF NOT EXISTS filing_time DATETIME COMMENT '备案时间',
  ADD COLUMN IF NOT EXISTS wechat_pay_enabled TINYINT DEFAULT 0 COMMENT '是否开通微信支付: 0-否 1-是',
  ADD COLUMN IF NOT EXISTS wechat_pay_mch_id VARCHAR(100) COMMENT '微信支付商户号',
  ADD COLUMN IF NOT EXISTS server_domain VARCHAR(200) COMMENT '服务器域名',
  ADD COLUMN IF NOT EXISTS socket_server_domain VARCHAR(200) COMMENT 'Socket服务器域名',
  ADD COLUMN IF NOT EXISTS upload_domain VARCHAR(200) COMMENT '上传文件域名',
  ADD COLUMN IF NOT EXISTS download_domain VARCHAR(200) COMMENT '下载文件域名',
  ADD COLUMN IF NOT EXISTS business_domain VARCHAR(200) COMMENT '业务域名',
  ADD COLUMN IF NOT EXISTS online_status VARCHAR(20) DEFAULT 'dev' COMMENT '上架状态: dev/review/online/offline',
  ADD COLUMN IF NOT EXISTS authorize_time DATETIME COMMENT '授权时间',
  ADD COLUMN IF NOT EXISTS last_publish_time DATETIME COMMENT '最后发布时间',
  ADD COLUMN IF NOT EXISTS tags JSON COMMENT '标签（JSON格式）';

ALTER TABLE mini_programs ADD INDEX IF NOT EXISTS idx_business_mode (business_mode);
ALTER TABLE mini_programs ADD INDEX IF NOT EXISTS idx_online_status (online_status);
ALTER TABLE mini_programs ADD INDEX IF NOT EXISTS idx_certification_status (certification_status);
ALTER TABLE mini_programs ADD INDEX IF NOT EXISTS idx_filing_status (filing_status);
ALTER TABLE mini_programs ADD INDEX IF NOT EXISTS idx_owner_user_id (owner_user_id);
ALTER TABLE mini_programs ADD INDEX IF NOT EXISTS idx_developer_company_id (developer_company_id);

-- 1.3 users_XX表字段增强（批量处理100张分片表）
DELIMITER $$
DROP PROCEDURE IF EXISTS alter_users_tables$$
CREATE PROCEDURE alter_users_tables()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE table_name VARCHAR(20);
    DECLARE sql_text TEXT;

    WHILE i < 100 DO
        SET table_name = CONCAT('users_', LPAD(i, 2, '0'));

        -- 检查表是否存在
        SET @check_exists = (SELECT COUNT(*) FROM information_schema.tables
                            WHERE table_schema = 'user_nexus'
                            AND table_name = table_name);

        IF @check_exists > 0 THEN
            -- 添加字段
            SET sql_text = CONCAT('ALTER TABLE `', table_name, '`
                ADD COLUMN IF NOT EXISTS wechat_openid VARCHAR(100) COMMENT ''微信OpenID（默认应用）'',
                ADD COLUMN IF NOT EXISTS wechat_unionid VARCHAR(100) COMMENT ''微信UnionID（全局唯一，绑定开放平台后获取）'',
                ADD COLUMN IF NOT EXISTS wechat_openid_list JSON COMMENT ''多OpenID存储（JSON格式）'',
                ADD COLUMN IF NOT EXISTS wechat_access_token VARCHAR(500) COMMENT ''微信access_token（AES加密）'',
                ADD COLUMN IF NOT EXISTS wechat_refresh_token VARCHAR(500) COMMENT ''微信refresh_token（AES加密）'',
                ADD COLUMN IF NOT EXISTS wechat_token_expire_time DATETIME COMMENT ''access_token过期时间'',
                ADD COLUMN IF NOT EXISTS wechat_last_auth_time DATETIME COMMENT ''最后授权时间'',
                ADD COLUMN IF NOT EXISTS company_id BIGINT COMMENT ''所属公司ID（如果是公司账户）'',
                ADD COLUMN IF NOT EXISTS service_provider_id BIGINT COMMENT ''所属服务商ID（如果是服务商子账户）'',
                ADD COLUMN IF NOT EXISTS parent_user_id BIGINT COMMENT ''父账户ID（用于子账户场景）'',
                ADD COLUMN IF NOT EXISTS register_ip VARCHAR(50) COMMENT ''注册IP'',
                ADD COLUMN IF NOT EXISTS register_source VARCHAR(20) COMMENT ''注册来源: web/app/mini_program/api''');

            SET @sql = sql_text;
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            -- 添加索引
            SET sql_text = CONCAT('ALTER TABLE `', table_name, '`
                ADD INDEX IF NOT EXISTS uk_wechat_unionid (wechat_unionid),
                ADD INDEX IF NOT EXISTS idx_company_id (company_id),
                ADD INDEX IF NOT EXISTS idx_service_provider_id (service_provider_id)');

            SET @sql = sql_text;
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            SELECT CONCAT('✅ 已更新表: ', table_name) AS result;
        END IF;

        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

-- 执行存储过程
CALL alter_users_tables();

-- 清理存储过程
DROP PROCEDURE IF EXISTS alter_users_tables;

-- ========================================
-- 第二部分：user_nexus数据库新增表
-- ========================================
USE user_nexus;

-- 2.1 微信绑定关系表
CREATE TABLE IF NOT EXISTS user_wechat_binding (
  id BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  user_shard TINYINT NOT NULL COMMENT '用户分片ID（0-99）',
  wechat_openid VARCHAR(100) NOT NULL COMMENT '微信OpenID',
  wechat_unionid VARCHAR(100) COMMENT '微信UnionID',
  wechat_session_key VARCHAR(500) COMMENT '微信SessionKey（AES加密）',
  app_id VARCHAR(50) NOT NULL COMMENT '小程序AppID',
  app_type VARCHAR(20) NOT NULL DEFAULT 'mini_program' COMMENT '应用类型: mini_program/app/web',
  bind_type VARCHAR(20) NOT NULL DEFAULT 'auto' COMMENT '绑定类型: auto/manual',
  bind_time DATETIME NOT NULL COMMENT '绑定时间',
  last_auth_time DATETIME COMMENT '最后授权时间',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: active/inactive/deleted',
  unbind_time DATETIME COMMENT '解绑时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_appid` (`user_id`, `app_id`, `deleted`),
  KEY `idx_unionid` (`wechat_unionid`),
  KEY `idx_openid` (`wechat_openid`),
  KEY `idx_user_shard` (`user_shard`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微信绑定关系表';

-- 2.2 公司资质文件表
CREATE TABLE IF NOT EXISTS company_qualifications (
  id BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  company_id BIGINT NOT NULL COMMENT '公司ID',
  qualification_type VARCHAR(50) NOT NULL COMMENT '资质类型',
  file_name VARCHAR(200) NOT NULL COMMENT '文件名称',
  file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
  file_size BIGINT COMMENT '文件大小（字节）',
  audit_status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '审核状态',
  audit_by BIGINT COMMENT '审核人ID',
  audit_time DATETIME COMMENT '审核时间',
  audit_remark VARCHAR(500) COMMENT '审核备注',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_qualification_type` (`qualification_type`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司资质文件表';

-- 2.3 服务商申请表
CREATE TABLE IF NOT EXISTS company_service_providers (
  id BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  company_id BIGINT NOT NULL COMMENT '申请公司ID',
  apply_type VARCHAR(20) NOT NULL COMMENT '申请类型',
  current_level VARCHAR(20) COMMENT '当前等级',
  target_level VARCHAR(20) NOT NULL COMMENT '目标等级',
  apply_reason VARCHAR(500) COMMENT '申请理由',
  status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '审核状态',
  audit_by BIGINT COMMENT '审核人ID',
  audit_time DATETIME COMMENT '审核时间',
  audit_remark VARCHAR(500) COMMENT '审核备注',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务商申请表';

-- 2.4 小程序模板表
CREATE TABLE IF NOT EXISTS mini_program_templates (
  id BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
  template_code VARCHAR(50) NOT NULL COMMENT '模板编码（唯一）',
  category VARCHAR(50) NOT NULL COMMENT '模板分类',
  industry VARCHAR(100) COMMENT '所属行业',
  description TEXT COMMENT '模板描述',
  screenshot_urls JSON COMMENT '模板截图URL列表（JSON数组）',
  price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '模板价格（元）',
  is_free TINYINT NOT NULL DEFAULT 0 COMMENT '是否免费: 0-否 1-是',
  developer_company_id BIGINT NOT NULL COMMENT '开发者公司ID',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态',
  version VARCHAR(20) DEFAULT '1.0.0' COMMENT '模板版本',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`),
  KEY `idx_category` (`category`),
  KEY `idx_developer_company` (`developer_company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序模板表';

-- 2.5 小程序类目管理表
CREATE TABLE IF NOT EXISTS mini_program_categories (
  id BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  first_category VARCHAR(50) NOT NULL COMMENT '一级类目',
  second_category VARCHAR(50) NOT NULL COMMENT '二级类目',
  qualification_required TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要资质',
  qualification_types JSON COMMENT '需要的资质类型列表（JSON数组）',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态',
  sort_order INT DEFAULT 0 COMMENT '排序',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_first_category` (`first_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序类目管理表';

-- 2.6 小程序代开发任务表
CREATE TABLE IF NOT EXISTS mini_program_development_tasks (
  id BIGINT NOT NULL COMMENT '任务ID（雪花算法）',
  task_no VARCHAR(50) NOT NULL COMMENT '任务编号（唯一）',
  mini_program_id BIGINT COMMENT '小程序ID（完成后关联）',
  client_company_id BIGINT NOT NULL COMMENT '客户公司ID',
  client_user_id BIGINT NOT NULL COMMENT '客户用户ID',
  developer_company_id BIGINT NOT NULL COMMENT '开发者公司ID（平台或服务商）',
  task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
  task_type VARCHAR(20) NOT NULL COMMENT '任务类型',
  task_status VARCHAR(20) NOT NULL COMMENT '任务状态',
  current_step VARCHAR(50) COMMENT '当前步骤',
  wechat_appid VARCHAR(100) COMMENT '获得的小程序AppID',
  wechat_enterprise_name VARCHAR(200) COMMENT '企业名称',
  wechat_legal_person_wechat VARCHAR(100) COMMENT '法人微信号',
  mini_program_name VARCHAR(200) COMMENT '小程序名称',
  mini_program_name_status VARCHAR(20) COMMENT '名称审核状态',
  progress_percent INT NOT NULL DEFAULT 0 COMMENT '进度百分比（0-100）',
  estimated_finish_time DATETIME COMMENT '预计完成时间',
  total_fee DECIMAL(10,2) NOT NULL COMMENT '总费用（元）',
  paid_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '已付费用（元）',
  start_time DATETIME COMMENT '开始时间',
  finish_time DATETIME COMMENT '完成时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_client_company` (`client_company_id`),
  KEY `idx_developer_company` (`developer_company_id`),
  KEY `idx_task_status` (`task_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序代开发任务表';

-- 2.7 小程序微信认证表
CREATE TABLE IF NOT EXISTS mini_program_certification (
  id BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  mini_program_id BIGINT NOT NULL COMMENT '小程序ID',
  certification_apply_id VARCHAR(100) COMMENT '认证申请ID',
  certification_order_no VARCHAR(100) COMMENT '认证订单号',
  certification_status VARCHAR(20) NOT NULL COMMENT '认证状态',
  certification_payment_status VARCHAR(20) COMMENT '认证支付状态',
  certification_time DATETIME COMMENT '认证时间',
  certification_expire_time DATETIME COMMENT '认证过期时间',
  certification_fee DECIMAL(10,2) COMMENT '认证费用（300元/年）',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mini_program_id` (`mini_program_id`, `deleted`),
  KEY `idx_certification_status` (`certification_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序微信认证表';

-- 2.8 小程序备案表
CREATE TABLE IF NOT EXISTS mini_program_filing (
  id BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  mini_program_id BIGINT NOT NULL COMMENT '小程序ID',
  filing_apply_status VARCHAR(20) NOT NULL COMMENT '备案申请状态',
  filing_task_id VARCHAR(100) COMMENT '备案任务ID',
  filing_face_id_task_id VARCHAR(100) COMMENT '人脸核身任务ID',
  filing_status VARCHAR(20) COMMENT '备案状态',
  filing_no VARCHAR(100) COMMENT '备案号',
  filing_time DATETIME COMMENT '备案时间',
  filing_status_detail JSON COMMENT '备案状态详情（JSON格式）',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mini_program_id` (`mini_program_id`, `deleted`),
  KEY `idx_filing_status` (`filing_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序备案表';

-- 2.9 用户登录凭证表
CREATE TABLE IF NOT EXISTS user_auth_tokens (
  id BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  user_shard TINYINT NOT NULL COMMENT '用户分片ID',
  access_token VARCHAR(500) NOT NULL COMMENT '访问令牌',
  refresh_token VARCHAR(500) COMMENT '刷新令牌',
  token_expire_time DATETIME NOT NULL COMMENT 'access_token过期时间',
  login_ip VARCHAR(50) COMMENT '登录IP',
  login_device VARCHAR(100) COMMENT '登录设备',
  login_time DATETIME NOT NULL COMMENT '登录时间',
  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_access_token` (`access_token`(100)),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录凭证表';

-- ========================================
-- 第三部分：创建duda_trade数据库并新建表
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS duda_trade
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci
  COMMENT '交易订单数据库';

USE duda_trade;

-- 3.1 支付订单表（2026年03月）
CREATE TABLE IF NOT EXISTS payment_orders_202603 (
  id BIGINT NOT NULL COMMENT '订单ID（雪花算法）',
  order_no VARCHAR(50) NOT NULL COMMENT '订单编号（唯一）',
  transaction_id VARCHAR(50) COMMENT '微信支付交易号',

  mini_program_id BIGINT NOT NULL COMMENT '小程序ID',
  merchant_id BIGINT NOT NULL COMMENT '商户ID',
  merchant_user_id BIGINT NOT NULL COMMENT '商户用户ID',
  platform_user_id BIGINT NOT NULL COMMENT '平台用户ID',
  platform_user_shard TINYINT NOT NULL COMMENT '平台用户分片ID（0-99）',

  order_type VARCHAR(20) NOT NULL COMMENT '订单类型',
  order_title VARCHAR(200) COMMENT '订单标题',
  order_amount DECIMAL(10,2) NOT NULL COMMENT '订单金额（元）',
  actual_payment_amount DECIMAL(10,2) NOT NULL COMMENT '实际支付金额（元）',

  payment_method VARCHAR(20) NOT NULL COMMENT '支付方式: wechat_pay/alipay/balance',
  payment_status VARCHAR(20) NOT NULL COMMENT '支付状态: unpaid/paying/paid/refunded/cancelled',

  profit_sharing_enabled TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要分账: 0-否 1-是',
  profit_sharing_status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '分账状态: pending/processing/completed/failed',
  profit_sharing_rules JSON COMMENT '分账规则（JSON格式）',

  order_time DATETIME NOT NULL COMMENT '下单时间',
  payment_time DATETIME COMMENT '支付时间',
  profit_sharing_time DATETIME COMMENT '分账时间',

  client_ip VARCHAR(50) COMMENT '客户端IP',
  client_device VARCHAR(100) COMMENT '客户端设备',

  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_mini_program` (`mini_program_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_merchant_user` (`merchant_user_id`),
  KEY `idx_platform_user` (`platform_user_id`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_profit_sharing_status` (`profit_sharing_status`),
  KEY `idx_order_time` (`order_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单表-2026年03月';

-- 3.2 分账规则表
CREATE TABLE IF NOT EXISTS profit_sharing_rules (
  id BIGINT NOT NULL COMMENT '主键ID',
  merchant_id BIGINT NOT NULL COMMENT '商户ID',
  rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
  rule_type VARCHAR(20) NOT NULL DEFAULT 'percentage' COMMENT '规则类型: percentage/fixed',

  platform_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '平台分账比例（0.10 = 10%）',
  merchant_rate DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT '商户分账比例（0.90 = 90%）',

  settlement_cycle VARCHAR(20) NOT NULL DEFAULT 'T+1' COMMENT '结算周期: T+0/T+1/T+7/M+1',

  status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态: inactive/active/suspended',
  effective_time DATETIME NOT NULL COMMENT '生效时间',
  expire_time DATETIME COMMENT '失效时间（NULL表示永久有效）',

  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_by VARCHAR(50) COMMENT '创建人',
  update_by VARCHAR(50) COMMENT '更新人',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_rule` (`merchant_id`, `rule_name`, `deleted`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分账规则表';

-- 3.3 分账记录表
CREATE TABLE IF NOT EXISTS profit_sharing_records (
  id BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  order_no VARCHAR(50) NOT NULL COMMENT '订单编号',

  receiver_type VARCHAR(50) NOT NULL COMMENT '接收方类型: MERCHANT_ID/PERSONAL_WECHATID',
  receiver_account VARCHAR(200) NOT NULL COMMENT '接收方账号',
  receiver_name VARCHAR(200) NOT NULL COMMENT '接收方名称',

  amount DECIMAL(10,2) NOT NULL COMMENT '分账金额（元）',
  description VARCHAR(500) COMMENT '分账描述',

  wechat_transaction_id VARCHAR(100) COMMENT '微信分账交易单号',

  status VARCHAR(20) NOT NULL COMMENT '状态: success/failed/processing',
  error_code VARCHAR(50) COMMENT '错误码',
  error_message VARCHAR(500) COMMENT '错误信息',

  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  finish_time DATETIME COMMENT '完成时间',

  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_receiver_account` (`receiver_account`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分账记录表';

-- 3.4 结算单据表
CREATE TABLE IF NOT EXISTS settlement_statements (
  id BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
  merchant_id BIGINT NOT NULL COMMENT '商户ID',
  settlement_no VARCHAR(50) NOT NULL COMMENT '结算单号（唯一）',

  settlement_cycle VARCHAR(20) NOT NULL COMMENT '结算周期: T+0/T+1/T+7/M+1',
  settlement_start_date DATE NOT NULL COMMENT '结算开始日期',
  settlement_end_date DATE NOT NULL COMMENT '结算结束日期',

  total_orders INT NOT NULL DEFAULT 0 COMMENT '总订单数',
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总金额（元）',

  platform_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '平台分账金额（元）',
  merchant_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '商户分账金额（元）',

  status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态: pending/paid/refunded',
  payment_time DATETIME COMMENT '支付时间',

  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_settlement_cycle` (`settlement_cycle`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='结算单据表';

-- ========================================
-- 执行完成提示
-- ========================================
SELECT '✅ 数据库升级完成！' AS message;
SELECT '✅ user_nexus数据库：字段增强已完成' AS message;
SELECT '✅ user_nexus数据库：新增9张表' AS message;
SELECT '✅ duda_trade数据库：已创建，新增4张表' AS message;
