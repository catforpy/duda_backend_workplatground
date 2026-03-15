# OSS POST签名直传使用指南

## 概述

本实现完全遵循阿里云官方文档的POST签名方案，支持Web端表单直传OSS，无需前端SDK。

## 架构说明

```
┌─────────┐        获取签名         ┌──────────────┐
│  浏览器  │ ───────────────────►  │  应用服务器  │
│ (Web端) │                        │ (file-api)  │
└─────────┘                        └──────────────┘
     │                                    │
     │                                    │
     │ 1. 获取STS临时凭证（可选）            │
     │◄───────────────────────────────────│
     │                                    │
     │ 2. 返回POST签名参数                  │
     │◄───────────────────────────────────│
     │                                    │
     │ 3. 表单直传OSS                       │
     └────────────────────────────────────►│
                                      阿里云OSS
```

## 核心优势

### ✅ 与官方示例完全一致
- 使用相同的STS SDK版本（sts20150401）
- 使用相同的签名算法（OSS4-HMAC-SHA256）
- 使用相同的Policy格式
- 支持POST签名表单直传

### ✅ 安全性
- 支持STS临时凭证（推荐生产环境）
- 签名在服务端生成，不暴露密钥
- 支持临时凭证过期时间控制
- 支持文件大小限制和类型限制

### ✅ 易用性
- 前端无需集成OSS SDK
- 纯HTML表单即可上传
- 支持进度显示
- 支持自定义上传目录

## 快速开始

### 1. 配置环境变量

创建 `.env` 文件（参考 `.env.example`）：

```bash
cp .env.example .env
```

编辑 `.env` 文件，填写真实的阿里云配置：

```bash
# 阿里云访问密钥（或使用STS）
OSS_ACCESS_KEY_ID=your-access-key-id
OSS_ACCESS_KEY_SECRET=your-access-key-secret

# STS Role ARN（可选，如果使用STS临时凭证）
# 格式: acs:ram::你的账号ID:role/角色名
OSS_STS_ROLE_ARN=acs:ram::1234567890123456:role/oss-sts-role

# OSS区域（如：cn-hangzhou, cn-beijing）
OSS_REGION=cn-hangzhou

# OSS Bucket名称
OSS_BUCKET=duda-java-backend-test
```

### 2. 启动服务

使用Docker Compose启动file-api服务：

```bash
docker-compose up -d duda-file-api
```

### 3. 测试上传

访问Swagger文档测试API：

```
http://localhost:8085/swagger-ui.html
```

或直接访问上传页面测试：

```
http://localhost:8085/upload.html
```

## API端点

### 获取POST签名

**接口：** `GET /api/oss/upload/post-signature`

**响应示例：**

```json
{
  "version": "OSS4-HMAC-SHA256",
  "policy": "eyJleHBpcmF0aW9uIjogIjIwMjYtMDMtMTVUMTE6NTE6NDVaIiwgImNvbmRpdGlvbnMiOiBb...",
  "x_oss_credential": "LTAI5tKx.../20250315/cn-hangzhou/oss/aliyun_v4_request",
  "x_oss_date": "20250315T115124Z",
  "signature": "1a2b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t1u2v3w",
  "dir": "uploads/",
  "host": "https://duda-java-backend-test.oss-cn-hangzhou.aliyuncs.com",
  "security_token": "CAIS...（如果使用STS）"
}
```

**前端使用示例：**

```javascript
// 1. 获取签名
const response = await fetch('/api/oss/upload/post-signature');
const signatureData = await response.json();

// 2. 构造FormData
const formData = new FormData();
formData.append('success_action_status', '200');
formData.append('policy', signatureData.policy);
formData.append('x-oss-signature', signatureData.signature);
formData.append('x-oss-signature-version', 'OSS4-HMAC-SHA256');
formData.append('x-oss-credential', signatureData.x_oss_credential);
formData.append('x-oss-date', signatureData.x_oss_date);

// 如果使用STS
if (signatureData.security_token) {
    formData.append('x-oss-security-token', signatureData.security_token);
}

// 文件名
formData.append('key', signatureData.dir + 'example.jpg');

// 文件本身（必须是最后一个字段）
formData.append('file', fileInput.files[0]);

// 3. 上传到OSS
await fetch(signatureData.host, {
    method: 'POST',
    body: formData
});
```

## 安全建议

### 开发环境
- 可以直接使用AccessKey/Secret
- 设置较短的过期时间（如3600秒）

### 生产环境
- **强烈建议使用STS临时凭证**
- 配置STS Role，限制权限范围
- 设置合理的过期时间（900-3600秒）
- 定期轮换密钥

### STS Role配置示例

```json
{
  "Version": "1",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "oss:PutObject"
      ],
      "Resource": [
        "acs:oss:*:*:duda-java-backend-test/uploads/*"
      ],
      "Condition": {
        "StringLike": {
          "oss:x-oss-object-name": [
            "uploads/*"
          ]
        }
      }
    }
  ]
}
```

## 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 | 必填 |
|--------|------|--------|------|
| OSS_ACCESS_KEY_ID | 阿里云AccessKey ID | - | 是 |
| OSS_ACCESS_KEY_SECRET | 阿里云AccessKey Secret | - | 是 |
| OSS_STS_ROLE_ARN | STS Role ARN | - | 否 |
| OSS_REGION | OSS区域 | cn-hangzhou | 否 |
| OSS_BUCKET | Bucket名称 | duda-java-backend-test | 否 |
| OSS_UPLOAD_DIR | 上传目录前缀 | uploads/ | 否 |
| OSS_EXPIRE_TIME | 签名过期时间（秒） | 3600 | 否 |

### Policy限制

默认生成的Policy包含以下限制：

1. **Bucket限制**：只能上传到指定Bucket
2. **文件大小限制**：1B - 10MB
3. **前缀限制**：文件必须以指定前缀开头（默认：`uploads/`）
4. **成功状态**：上传成功返回200状态码
5. **签名版本**：必须使用OSS4-HMAC-SHA256

## 故障排查

### 1. 获取签名失败

**错误信息：** `获取STS临时凭证失败`

**解决方案：**
- 检查环境变量是否正确配置
- 确认STS Role ARN格式正确
- 检查RAM角色权限配置

### 2. 上传失败

**错误信息：** `AccessDenied`

**解决方案：**
- 检查Bucket权限配置
- 确认STS Role有`oss:PutObject`权限
- 检查文件大小是否超过限制

### 3. 签名不匹配

**错误信息：** `SignatureDoesNotMatch`

**解决方案：**
- 检查系统时间是否准确
- 确认region配置正确
- 检查x-oss-date格式

## 相关文档

- [阿里云OSS POST签名官方文档](https://help.aliyun.com/zh/oss/developer-reference/perform-post-object-signature-calculation-in-the-server)
- [STS临时凭证授权](https://help.aliyun.com/zh/ram/developer-guide/role-sts)
- [OSS Browser.js SDK](https://help.aliyun.com/zh/oss/developer-reference/oss-browserjs-3)

## 版本历史

- **v1.0.0** (2025-03-15)
  - 初始版本
  - 支持POST签名直传
  - 支持STS临时凭证
  - 提供Web端上传示例

## 作者

DudaNexus Team

## 许可证

Copyright © 2025 DudaNexus
