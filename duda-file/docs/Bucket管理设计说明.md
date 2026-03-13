# Bucket管理设计说明

## 概述

基于阿里云OSS的Bucket管理文档，我们已经完成了Bucket管理模块的核心设计。本文档总结了已完成的设计内容，并为后续对象文件管理提供基础。

## 一、已完成的设计内容

### 1. 数据库设计

**文件位置**: `/Volumes/DudaDate/DudaNexus/sql/bucket_config.sql`

**核心表结构**: `bucket_config`

**关键字段**:
- **基础信息**: bucket_name, storage_type, region
- **多租户支持**: user_id, user_type, tenant_id
- **存储配置**: storage_class, data_redundancy_type, acl_type
- **配额管理**: max_file_size, max_file_count, allowed_file_types
- **安全加密**: access_key_id, access_key_secret (AES-256-GCM加密)
- **状态管理**: status, is_deleted

**设计亮点**:
1. 支持多云存储类型（阿里云OSS、腾讯云COS、七牛云Kodo等）
2. API密钥加密存储，保证安全性
3. 灵活的配额管理
4. 完整的审计字段（创建时间、更新时间等）

### 2. 枚举类型设计

#### 2.1 StorageType (存储类型)
**文件位置**: `duda-file-interface/src/main/java/com/duda/file/enums/StorageType.java`

支持的存储类型:
- ALIYUN_OSS - 阿里云OSS
- TENCENT_COS - 腾讯云COS
- QINIU_KODO - 七牛云Kodo
- AWS_S3 - 亚马逊S3
- MINIO - MinIO

#### 2.2 StorageClass (存储类型/存储级别)
**文件位置**: `duda-file-interface/src/main/java/com/duda/file/enums/StorageClass.java`

支持的存储级别:
- STANDARD - 标准存储（高频访问）
- IA - 低频访问存储（适合不常访问的数据）
- ARCHIVE - 归档存储（30天最低存储）
- COLD_ARCHIVE - 冷归档存储（180天最低存储）

每个级别都包含元数据:
- 最小存储天数
- 最小计费单元大小
- 数据取回时间
- 适用场景

#### 2.3 DataRedundancyType (数据冗余类型)
**文件位置**: `duda-file-interface/src/main/java/com/duda/file/enums/DataRedundancyType.java`

- LRS - 本地冗余存储（成本低，单可用区）
- ZRS - 同城冗余存储（生产环境推荐，多可用区，99.9999999999%持久性）

#### 2.4 AclType (访问控制类型)
**文件位置**: `duda-file-interface/src/main/java/com/duda/file/enums/AclType.java`

- PRIVATE - 私有读写（默认）
- PUBLIC_READ - 公共读
- PUBLIC_READ_WRITE - 公共读写

### 3. DTO设计

#### 3.1 BucketDTO
**文件位置**: `duda-file-interface/src/main/java/com/duda/file/dto/BucketDTO.java`

**主要字段**:
- 基础信息: bucketName, displayName, storageType, region
- 配置信息: storageClass, dataRedundancyType, acl
- 访问端点: extranetEndpoint, intranetEndpoint
- 使用情况: fileCount, storageSize, storageQuota
- 其他: tags, status, versioningEnabled, userId, extra

#### 3.2 CreateBucketReqDTO
**文件位置**: `duda-file-interface/src/main/java/com/duda/file/dto/CreateBucketReqDTO.java`

**主要字段**:
- bucketName（可选，不填则自动生成）
- displayName（用户自定义显示名称）
- storageType, region
- storageClass, dataRedundancyType, aclType
- versioningEnabled（版本控制）
- tags（标签）
- userId, userType

#### 3.3 BucketStatisticsDTO
**文件位置**: `duda-file-interface/src/main/java/com/duda/file/dto/BucketStatisticsDTO.java`

**统计维度**:
- 存储使用情况: fileCount, storageUsed, storageQuota, usagePercentage
- 今日流量: todayUploadCount, todayUploadTraffic, todayDownloadTraffic
- 请求统计: monthRequestCount
- 更新时间: lastUpdateTime

### 4. 服务接口设计

#### 4.1 BucketManagementService
**文件位置**: `duda-file-interface/src/main/java/com/duda/file/service/BucketManagementService.java`

**核心方法**:

**Bucket基础操作**:
- `createBucket(CreateBucketReqDTO)` - 创建存储空间
- `deleteBucket(bucketName, userId)` - 删除存储空间
- `listBuckets(userId)` - 列举用户的存储空间
- `getBucketInfo(bucketName)` - 获取Bucket详细信息
- `doesBucketExist(bucketName)` - 检查Bucket是否存在

**权限管理**:
- `setBucketAcl(bucketName, aclType)` - 设置访问权限
- `getBucketAcl(bucketName)` - 获取访问权限

**地域和标签管理**:
- `getBucketLocation(bucketName)` - 获取Bucket地域
- `setBucketTags(bucketName, tags)` - 设置标签
- `getBucketTags(bucketName)` - 获取标签

**配额和统计**:
- `getBucketStatistics(bucketName)` - 获取用量统计
- `updateBucketQuota(bucketName, maxSize, maxCount)` - 更新配额

**辅助功能**:
- `generateBucketName(userId, userType, category)` - 自动生成Bucket名称
- `validateBucketName(bucketName)` - 验证Bucket名称合法性

### 5. 工具类设计

#### 5.1 BucketNameGenerator
**文件位置**: `duda-file-interface/src/main/java/com/duda/file/util/BucketNameGenerator.java`

**功能**:
- 生成符合阿里云OSS规范的Bucket名称
- 格式: `{prefix}-{userType}-{userId}-{category}-{timestamp}`
- 完整的名称验证逻辑

**特性**:
- 全局唯一性
- 符合命名规范（3-63字符，小写字母/数字/短横线）
- 自动处理超长名称（使用UUID缩短）

#### 5.2 ApiKeyEncryptionUtil
**文件位置**: `duda-file-interface/src/main/java/com/duda/file/util/ApiKeyEncryptionUtil.java`

**功能**:
- 使用AES-256-GCM加密API密钥
- 每次加密生成新的IV，防止相同明文产生相同密文
- 提供完整性校验，防止密文被篡改

**安全设计**:
- 加密密钥应从Nacos配置中心获取
- 密文格式: IV(12字节) + 密文 + 认证标签(16字节)
- 支持密钥生成和验证

**使用方式**:
```java
// 初始化加密密钥（从Nacos读取）
ApiKeyEncryptionUtil.setEncryptionKey(nacosConfig.getEncryptionKey());

// 加密用户提供的API密钥
String encrypted = ApiKeyEncryptionUtil.encrypt(userAccessKeySecret);

// 存储到数据库
bucketConfig.setAccessKeySecret(encrypted);

// 使用时解密
String decrypted = ApiKeyEncryptionUtil.decrypt(encrypted);
```

#### 5.3 BucketValidationUtil
**文件位置**: `duda-file-interface/src/main/java/com/duda/file/util/BucketValidationUtil.java`

**验证功能**:
- Bucket名称验证（长度、格式、IP地址格式检查）
- 显示名称验证
- 地域代码验证（支持主流云厂商地域）
- 存储类型验证
- 标签验证（键长度、值长度、数量限制）
- 配额验证
- 用户信息验证

**使用方式**:
```java
ValidationResult result = BucketValidationUtil.validateBucketName(bucketName);
if (!result.isValid()) {
    throw new IllegalArgumentException(result.getMessage());
}
```

## 二、Bucket管理业务流程

### 1. 创建Bucket流程

```
用户请求创建Bucket
    ↓
验证用户权限和配额
    ↓
验证参数（工具类验证）
    ↓
生成或验证Bucket名称
    ↓
判断API密钥来源:
  - 平台API密钥: 从Nacos读取
  - 用户API密钥: 从数据库解密
    ↓
调用对应云厂商SDK创建Bucket
    ↓
保存Bucket配置到数据库
    ↓
返回Bucket信息给用户
```

### 2. API密钥管理策略

#### 平台级API密钥
- **存储位置**: Nacos配置中心
- **配置示例**:
```yaml
duda:
  file:
    storage:
      aliyun:
        access-key-id: ${ALIYUN_ACCESS_KEY_ID}
        access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
        endpoint: oss-cn-hangzhou.aliyuncs.com
```
- **使用场景**: 平台统一管理的Bucket

#### 用户级API密钥
- **存储位置**: 数据库（AES-256-GCM加密）
- **表字段**:
  - access_key_id VARCHAR(500)
  - access_key_secret VARCHAR(500)
- **使用场景**: 用户使用自己的云存储账号

### 3. 多云适配策略

通过统一的BucketManagementService接口，适配不同的云厂商:

```java
// 根据storageType选择对应的适配器
switch (bucketDTO.getStorageType()) {
    case "aliyun-oss":
        return aliyunOssAdapter.createBucket(request);
    case "tencent-cos":
        return tencentCosAdapter.createBucket(request);
    case "qiniu-kodo":
        return qiniuKodoAdapter.createBucket(request);
    default:
        throw new UnsupportedStorageTypeException();
}
```

## 三、下一步工作（待对象文件文档）

### 待设计的对象文件管理功能:

1. **文件上传**
   - 用户端直传（STS临时凭证）
   - 服务端上传
   - 分片上传（大文件）
   - 批量上传

2. **文件下载**
   - 直接下载
   - 生成预签名URL
   - Range下载（断点续传）

3. **文件管理**
   - 文件列表（分页、前缀查询、递归查询）
   - 文件搜索（按名称、类型、时间）
   - 文件详情
   - 文件删除（单个、批量）
   - 文件复制/移动

4. **目录管理**
   - 创建目录
   - 删除目录
   - 重命名目录
   - 目录结构树

5. **文件元数据**
   - 基础元数据（文件名、大小、类型、ETag）
   - 自定义元数据
   - 文件标签
   - 权限管理

6. **文件操作审计**
   - 上传记录
   - 下载记录
   - 删除记录
   - 访问日志

### 待设计的数据库表:

1. **file_meta** - 文件元数据表
   - object_id, bucket_name, object_key
   - 文件大小、类型、ETag
   - 存储类型、访问权限
   - 创建时间、更新时间

2. **upload_task** - 上传任务表
   - 任务ID、用户ID、Bucket名称
   - 上传状态、进度
   - 分片信息（分片上传时）

3. **file_access_log** - 文件访问日志表
   - 操作类型（上传、下载、删除）
   - 用户ID、文件ID
   - 操作时间、IP地址

## 四、关键设计原则

### 1. 统一抽象
- 所有云厂商实现相同的接口
- 业务代码不感知底层云厂商差异
- 统一的DTO和异常处理

### 2. 安全第一
- API密钥加密存储
- 严格的权限校验
- 完整的审计日志
- STS临时凭证（最小权限原则）

### 3. 性能优化
- Redis缓存热门数据
- 异步处理大文件操作
- 批量操作支持
- 分页查询

### 4. 可扩展性
- 易于接入新的云厂商
- 支持用户自定义配置
- 灵活的存储类型选择
- 可扩展的元数据

### 5. 多租户隔离
- Bucket级别的隔离
- 用户级别的权限控制
- 配额管理
- 计费统计

## 五、配置示例

### Nacos配置示例（平台API密钥）

```yaml
duda:
  file:
    # 加密密钥（用于加密用户API密钥）
    encryption:
      key: ${ENCRYPTION_KEY}

    # 默认存储类型
    default-storage-type: aliyun-oss

    # 各云厂商配置
    aliyun:
      enabled: true
      access-key-id: ${ALIYUN_ACCESS_KEY_ID}
      access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
      default-region: cn-hangzhou
      default-storage-class: STANDARD

    tencent:
      enabled: false
      secret-id: ${TENCENT_SECRET_ID}
      secret-key: ${TENCENT_SECRET_KEY}
      default-region: ap-guangzhou

    qiniu:
      enabled: false
      access-key: ${QINIU_ACCESS_KEY}
      secret-key: ${QINIU_SECRET_KEY}
      default-region: east-cn
```

## 六、注意事项

1. **API密钥安全**
   - 平台密钥使用Nacos配置
   - 用户密钥加密存储到数据库
   - 日志中脱敏显示

2. **Bucket命名规范**
   - 全局唯一
   - 符合云厂商规范
   - 包含用户标识便于管理

3. **存储类型选择**
   - 根据访问频率选择合适的存储类型
   - 考虑存储成本和取回成本
   - 提供存储类型转换功能

4. **配额管理**
   - 防止用户滥用资源
   - 提供灵活的配额调整
   - 实时监控使用情况

5. **多云支持**
   - 统一接口适配
   - 配置化切换
   - 保持接口一致性

---

**下一步**: 等待用户提供对象文件(Object)的相关文档，继续设计文件管理功能。
