# STS Token API 修复指南

## 问题诊断

调用 `/api/oss/sts/token` 返回 500 错误，根本原因有两个：

1. **数据库中的 API 密钥未加密**，但代码会尝试解密
2. **Dubbo 序列化安全限制**，不允许序列化 Spring 的异常类

## 解决方案

### 步骤 1：更新数据库中的 API 密钥

执行以下 SQL 更新 `test-bucket` 的加密密钥：

```sql
USE duda_file;

UPDATE bucket_config SET
    access_key_id = 'iQxMxGZLymfv8j/dPoo2FZH3SEjI2CWuU40qbctrYMg=',
    access_key_secret = 'e+kq1z6Th1oKolrYRqjekfeldH+dVGj1yqZUkhXsf0I='
WHERE bucket_name = 'test-bucket';

-- 验证更新
SELECT bucket_name, access_key_id, access_key_secret, endpoint, region
FROM bucket_config
WHERE bucket_name = 'test-bucket';
```

### 步骤 2：重启 Dubbo 服务

修改代码后需要重启 `duda-file-provider` 服务：

```bash
# 方式1：使用 Docker
docker-compose restart duda-file-provider

# 方式2：使用 IDE
# 停止并重新运行 DudaFileProviderApplication
```

### 步骤 3：测试 API

```bash
curl -X 'POST' \
  'http://localhost:8085/api/oss/sts/token' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "bucketName": "test-bucket",
  "userId": 1,
  "durationSeconds": 3600,
  "permissionType": "ReadWrite",
  "httpsOnly": true
}'
```

预期返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "credentials": {
      "accessKeyId": "...",
      "accessKeySecret": "...",
      "securityToken": "...",
      "expiration": "2026-03-16T16:47:48Z"
    },
    "ossConfig": {
      "bucketName": "test-bucket",
      "region": "cn-hangzhou"
    }
  }
}
```

## 代码修改说明

已修改 `UploadServiceImpl.java`，移除所有异常传递：

**修改前：**
```java
throw new StorageException("STS_FAILED", "Failed to get STS credentials: " + e.getMessage(), e);
```

**修改后：**
```java
throw new StorageException("STS_FAILED", "Failed to get STS credentials: " + e.getMessage());
```

这样做的好处：
- 避免 Dubbo 序列化 Spring 异常类
- 保持异常信息完整
- 日志中仍然记录完整的异常堆栈

## 其他需要加密 API 密钥的 Bucket

如果还有其他 Bucket，使用 `EncryptApiKey.java` 工具生成加密密钥：

```bash
cd /Volumes/DudaDate/DudaNexus
javac EncryptApiKey.java
java -cp . EncryptApiKey
```

然后手动修改 `EncryptApiKey.java` 中的 `accessKeyId` 和 `accessKeySecret`，重新运行。

## Nacos 配置中的加密密钥

当前配置的加密密钥（Base64 编码）：
```yaml
duda:
  file:
    encryption:
      key: "IllvgnDNuazK972Ly+WG7ftWJj9r2AchDY5bjRhhTek="
```

这个密钥用于加密/解密数据库中的 API 密钥。

## 常见问题

### Q: 为什么需要加密 API 密钥？
A: 为了安全起见，防止数据库泄露时直接暴露阿里云 Access Key。

### Q: 解密失败怎么办？
A: 检查 Nacos 配置中的 `duda.file.encryption.key` 是否与加密时使用的密钥一致。

### Q: 如何更新加密密钥？
A:
1. 修改 Nacos 配置中的 `duda.file.encryption.key`
2. 重新加密所有 API 密钥
3. 更新数据库中的 `access_key_id` 和 `access_key_secret`

### Q: STS Token 是什么？
A: STS (Security Token Service) 临时凭证，允许前端直接上传文件到 OSS，避免文件经过后端服务器。
