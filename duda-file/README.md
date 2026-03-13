# DudaNexus 文件管理服务

## 📋 项目简介

`duda-file` 是 DudaNexus 的文件管理服务模块，提供统一的云存储接口，支持多云存储（阿里云OSS、腾讯云COS、千牛云Kodo等）。

## 🎯 核心特性

- ✅ **统一抽象接口**：屏蔽不同云厂商API差异
- ✅ **多云存储支持**：阿里云OSS、腾讯云COS、千牛云Kodo、MinIO
- ✅ **用户端直传**：支持STS临时凭证和预签名URL
- ✅ **适配器模式**：配置化切换，无需修改代码
- ✅ **异步文件处理**：支持图片、视频、音频异步处理

## 🏗️ 模块架构

```
duda-file/
├── duda-file-api/          # 文件API网关服务（HTTP REST）
├── duda-file-provider/     # 文件核心服务（Dubbo RPC）
└── duda-file-interface/    # 文件服务接口定义
```

## 🔧 技术栈

- **Spring Boot**: 3.2.0
- **Dubbo**: 3.3.2
- **Nacos**: 服务发现与配置中心
- **RocketMQ**: 消息队列（异步处理）
- **Redis**: 缓存
- **MySQL**: 元数据存储

## 📦 依赖的云存储SDK

- 阿里云 OSS SDK: 3.17.4
- 腾讯云 COS SDK: 5.6.155
- 千牛云 SDK: 7.13.1
- MinIO SDK: 8.5.7

## 🚀 快速开始

### 前置条件

1. JDK 17+
2. Maven 3.6+
3. Nacos 2.x
4. MySQL 8.0+
5. Redis 6.0+

### 编译项目

```bash
cd duda-file
mvn clean install
```

### 配置说明

#### 1. 应用配置

修改 `duda-file-api/src/main/resources/bootstrap.yml`:

```yaml
spring:
  profiles:
    active: dev  # 环境配置

  cloud:
    nacos:
      server-addr: your-nacos-host:8848
      username: nacos
      password: nacos
```

#### 2. 存储配置

在 Nacos 配置中心添加 `duda-file-provider-dev.yml`:

```yaml
storage:
  type: aliyun-oss  # 选择存储类型

  aliyun:
    oss:
      enabled: true
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key-id: your-access-key-id
      access-key-secret: your-access-key-secret
      region: cn-hangzhou
```

### 启动服务

```bash
# 启动 Provider 服务
cd duda-file-provider
java -jar duda-file-provider-1.0.0-SNAPSHOT.jar

# 启动 API 服务
cd duda-file-api
java -jar duda-file-api-1.0.0-SNAPSHOT.jar
```

## 📝 核心功能

### 1. 统一存储服务接口

所有云存储适配器实现 `StorageService` 接口：

```java
public interface StorageService {
    UploadResult uploadFile(UploadRequest request);
    InputStream downloadFile(String bucketName, String objectKey);
    void deleteFile(String bucketName, String objectKey);
    FileInfo getFileInfo(String bucketName, String objectKey);
    PresignedUrlResult generatePresignedUrl(...);
}
```

### 2. 用户端直传

前端通过后端获取的上传凭证直接上传到云存储：

```java
// 1. 后端生成上传凭证
UploadCredentialDTO credential = fileUploadService.generateUploadCredential(reqDTO, userId);

// 2. 前端使用凭证直传
fetch(credential.getUploadUrl(), {
    method: 'PUT',
    body: file,
    headers: {
        'Authorization': 'Bearer ' + credential.getSecurityToken()
    }
});
```

### 3. 多云存储切换

只需修改配置即可切换云存储：

```yaml
# 切换到腾讯云
storage:
  type: tencent-cos

# 切换到千牛云
storage:
  type: qiniu-kodo
```

## 📚 文档

- [设计手册-多云存储文件管理服务.md](./docs/设计手册-多云存储文件管理服务.md)
- [API文档](./docs/API文档.md) (待补充)

## 🔐 安全说明

⚠️ **重要**：
- 生产环境必须修改默认密钥
- AccessKey 和 SecretKey 建议从环境变量读取
- 启用STS临时凭证，限制权限和有效期

## 📊 状态

**当前版本**: v1.0.0-SNAPSHOT
**开发状态**: 🚧 设计阶段

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**作者**: DudaNexus Team
**创建时间**: 2026-03-13
