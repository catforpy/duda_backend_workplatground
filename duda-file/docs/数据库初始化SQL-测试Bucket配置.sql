-- duda-file-provider 数据库初始化SQL
-- 用途：创建测试Bucket配置
-- 执行前请先替换成你自己的阿里云Access Key

-- ========================================
-- 步骤1：插入测试Bucket配置
-- ========================================

USE duda_file;

INSERT INTO bucket_config (
    bucket_name,
    bucket_display_name,
    storage_type,
    access_key_id,
    access_key_secret,
    endpoint,
    region,
    acl_type,
    user_id,
    user_type,
    category,
    data_redundancy_type,
    max_storage_size,
    allowed_file_types,
    blocked_file_types,
    domain_name,
    cdn_enabled,
    versioning_enabled,
    cors_enabled,
    watermark_enabled,
    encryption_enabled,
    lifecycle_enabled,
    current_file_count,
    current_storage_size,
    storage_used_quota,
    is_deleted,
    created_time,
    updated_time
) VALUES (
    -- Bucket基本信息
    'test-bucket',                                    -- Bucket名称
    '测试存储桶',                                      -- 显示名称
    'ALIYUN_OSS',                                     -- 存储类型

    -- ⭐⭐⭐ 阿里云Access Key（请替换成你自己的）⭐⭐⭐
    'LTAI5t你的AccessKeyId',                          -- Access Key ID
    '你的AccessKeySecret',                            -- Access Key Secret

    -- OSS配置
    'oss-cn-hangzhou.aliyuncs.com',                   -- Endpoint
    'cn-hangzhou',                                    -- Region

    -- 权限配置
    'PRIVATE',                                        -- ACL类型
    1,                                                -- 用户ID
    'individual',                                     -- 用户类型

    -- 其他配置
    'test',                                           -- 分类
    'LRS',                                            -- 数据冗余类型
    10737418240,                                      -- 最大存储(10GB)
    NULL,                                             -- 允许的文件类型
    NULL,                                             -- 禁止的文件类型
    NULL,                                             -- 自定义域名
    0,                                                -- CDN启用
    0,                                                -- 版本控制启用
    0,                                                -- CORS启用
    0,                                                -- 水印启用
    0,                                                -- 加密启用
    0,                                                -- 生命周期启用
    0,                                                -- 当前文件数
    0,                                                -- 当前存储大小
    0,                                                -- 已用存储配额
    0,                                                -- 是否删除
    NOW(),                                            -- 创建时间
    NOW()                                             -- 更新时间
);

-- ========================================
-- 步骤2：验证插入结果
-- ========================================

SELECT
    id,
    bucket_name,
    access_key_id,
    endpoint,
    region,
    created_time
FROM bucket_config
WHERE bucket_name = 'test-bucket';

-- 预期结果：应该看到刚才插入的记录
