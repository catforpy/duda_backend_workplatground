# STS临时凭证服务实现指南

> **适用场景**: 客户端直传文件到云存储
> **支持平台**: Flutter Web、iOS、Android、Web前端
> **更新时间**: 2025-03-13

## 一、什么是STS？

**STS (Security Token Service)** 是阿里云、腾讯云等云服务商提供的临时访问凭证服务。

### 1.1 为什么使用STS？

| 对比项 | 使用主账号密钥 | 使用STS临时凭证 |
|--------|--------------|----------------|
| **安全性** | ❌ 密钥暴露风险 | ✅ 临时密钥，自动过期 |
| **权限控制** | ❌ 难以限制权限 | ✅ 可精确控制访问范围 |
| **客户端使用** | ❌ 不推荐 | ✅ 专为客户端设计 |
| **有效期** | ❌ 永久有效 | ✅ 可设置（15分钟-12小时） |

### 1.2 典型应用场景

**Flutter Web文件上传流程**:

```
Flutter Web应用
    ↓ 1. 请求STS临时凭证
duda-file服务 (调用云存储API)
    ↓ 2. 返回STS凭证
云存储API (AssumeRole)
    ↓ 3. 返回临时AK/SK/Token
Flutter Web应用
    ↓ 4. 使用临时凭证直接上传
云存储 (OSS/COS)
    ↓ 5. 上传完成
    ✅ 文件已存储，无需经过后端服务器
```

---

## 二、STS服务实现

### 2.1 添加阿里云STS依赖

在 `duda-file-provider/pom.xml` 中添加：

```xml
<!-- 阿里云STS SDK -->
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>aliyun-java-sdk-sts</artifactId>
    <version>3.1.1</version>
</dependency>
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>aliyun-java-sdk-core</artifactId>
    <version>4.6.4</version>
</dependency>
```

### 2.2 创建STS服务类

**文件**: `duda-file-provider/src/main/java/com/duda/file/provider/service/StsService.java`

```java
package com.duda.file.provider.service;

import com.aliyun.sts20150401.Client;
import com.aliyun.sts20150401.models.AssumeRoleRequest;
import com.aliyun.sts20150401.models.AssumeRoleResponse;
import com.duda.file.dto.upload.STSCredentialsDTO;
import com.duda.file.dto.upload.GetSTSReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * STS临时凭证服务
 *
 * @author duda
 * @date 2025-03-13
 */
@Slf4j
@Service
public class StsService {

    /**
     * 阿里云RAM角色ARN
     * 从Nacos配置中心读取
     */
    @Value("${aliyun.sts.role-arn}")
    private String roleArn;

    /**
     * STS接入点
     */
    @Value("${aliyun.sts.endpoint:sts.cn-hangzhou.aliyuncs.com}")
    private String endpoint;

    /**
     * 默认过期时间(秒)
     */
    @Value("${aliyun.sts.default-duration:3600}")
    private Long defaultDuration;

    /**
     * 获取STS临时凭证
     *
     * @param request STS请求参数
     * @return STS凭证
     */
    public STSCredentialsDTO getSTSForClientUpload(GetSTSReqDTO request) {
        log.info("获取STS临时凭证: bucket={}, prefix={}", request.getBucketName(), request.getObjectPrefix());

        try {
            // 1. 创建STS客户端
            Client stsClient = createStsClient();

            // 2. 构建AssumeRole请求
            AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest();
            assumeRoleRequest.setRoleArn(roleArn);
            assumeRoleRequest.setRoleSessionName(generateSessionName(request.getUserId()));
            assumeRoleRequest.setDurationSeconds(request.getDurationSeconds() != null ?
                request.getDurationSeconds() : defaultDuration);

            // 3. 设置权限策略（限制只能访问指定Bucket和前缀）
            String policy = generatePolicy(request.getBucketName(), request.getObjectPrefix(), request.getPermissionType());
            assumeRoleRequest.setPolicy(policy);

            // 4. 调用STS API
            AssumeRoleResponse response = stsClient.assumeRole(assumeRoleRequest);

            // 5. 构建返回结果
            return STSCredentialsDTO.builder()
                .accessKeyId(response.getCredentials().getAccessKeyId())
                .accessKeySecret(response.getCredentials().getAccessKeySecret())
                .securityToken(response.getCredentials().getSecurityToken())
                .expiration(java.time.LocalDateTime.ofInstant(
                    response.getCredentials().getExpiration().toInstant(),
                    java.time.ZoneId.systemDefault()
                ))
                .durationSeconds(response.getCredentials().getDurationSeconds())
                .permissionType(request.getPermissionType())
                .allowedBuckets(java.util.Arrays.asList(request.getBucketName()))
                .allowedPrefixes(request.getObjectPrefix() != null ?
                    java.util.Arrays.asList(request.getObjectPrefix()) : null)
                .httpsOnly(true)
                .build();

        } catch (Exception e) {
            log.error("获取STS临时凭证失败", e);
            throw new com.duda.file.common.exception.StorageException("STS_FAILED",
                "Failed to get STS credentials: " + e.getMessage());
        }
    }

    /**
     * 创建STS客户端
     */
    private Client createStsClient() {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.accessKeyId = getAccessKeyId();
        config.accessKeySecret = getAccessKeySecret();
        config.endpoint = endpoint;
        return new Client(config);
    }

    /**
     * 生成会话名称
     */
    private String generateSessionName(Long userId) {
        return "duda-file-session-" + userId + "-" + System.currentTimeMillis();
    }

    /**
     * 生成权限策略
     */
    private String generatePolicy(String bucketName, String objectPrefix, String permissionType) {
        StringBuilder policy = new StringBuilder();
        policy.append("{\n");
        policy.append("  \"Version\": \"1\",\n");
        policy.append("  \"Statement\": [\n");

        // 限制只能访问指定Bucket和前缀
        String resource = "acs:oss:*:*:" + bucketName + "/" +
            (objectPrefix != null ? objectPrefix : "*");

        if ("READ".equals(permissionType) || "READ_WRITE".equals(permissionType)) {
            policy.append("    {\n");
            policy.append("      \"Effect\": \"Allow\",\n");
            policy.append("      \"Action\": [\n");
            policy.append("        \"oss:GetObject\"\n");
            policy.append("      ],\n");
            policy.append("      \"Resource\": \"").append(resource).append("\"\n");
            policy.append("    },\n");
        }

        if ("WRITE".equals(permissionType) || "READ_WRITE".equals(permissionType)) {
            policy.append("    {\n");
            policy.append("      \"Effect\": \"Allow\",\n");
            policy.append("      \"Action\": [\n");
            policy.append("        \"oss:PutObject\"\n");
            policy.append("      ],\n");
            policy.append("      \"Resource\": \"").append(resource).append("\"\n");
            policy.append("    }\n");
        }

        policy.append("  ]\n");
        policy.append("}");

        log.debug("生成的STS策略: {}", policy);
        return policy.toString();
    }

    /**
     * 获取AccessKeyId（从环境变量或Nacos）
     */
    private String getAccessKeyId() {
        // 从环境变量或配置中心读取
        return System.getenv("ALIYUN_ACCESS_KEY_ID");
    }

    /**
     * 获取AccessKeySecret（从环境变量或Nacos）
     */
    private String getAccessKeySecret() {
        // 从环境变量或配置中心读取
        return System.getenv("ALIYUN_ACCESS_KEY_SECRET");
    }
}
```

### 2.3 更新UploadServiceImpl

在 `UploadServiceImpl.java` 中注入 `StsService`：

```java
@Autowired
private StsService stsService;

@Override
public STSCredentialsDTO getSTSForClientUpload(GetSTSReqDTO request) {
    log.info("Dubbo: Getting STS for client upload: {}", request.getBucketName());
    return stsService.getSTSForClientUpload(request);
}
```

### 2.4 Nacos配置

在 `duda-file-provider.yaml` 中添加：

```yaml
# 阿里云STS配置
aliyun:
  sts:
    # RAM角色ARN (需要在阿里云RAM控制台创建)
    role-arn: acs:ram::1234567890123456:role/duda-file-sts-role
    # STS接入点
    endpoint: sts.cn-hangzhou.aliyuncs.com
    # 默认过期时间(秒) 15分钟-12小时
    default-duration: 3600
```

---

## 三、阿里云RAM角色配置

### 3.1 创建RAM角色

1. 登录阿里云RAM控制台
2. 点击 **权限管理** → **角色** → **创建角色**
3. 选择 **可信实体类型**: 阿里云服务
4. 选择 **受信服务**: 云存储 (OSS)
5. 角色名称: `duda-file-sts-role`

### 3.2 配置角色权限策略

创建自定义权限策略:

```json
{
  "Version": "1",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "oss:PutObject",
        "oss:GetObject"
      ],
      "Resource": [
        "acs:oss:*:*:duda-file-bucket/*"
      ]
    }
  ]
}
```

将策略附加到RAM角色。

### 3.3 修改信任策略

编辑角色的信任策略:

```json
{
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Effect": "Allow",
      "Principal": {
        "Service": [
          "your-account-id@aliyun-oss.service.aliyuncs.com"
        ]
      }
    }
  ],
  "Version": "1"
}
```

---

## 四、Flutter Web集成

### 4.1 添加依赖

在 `pubspec.yaml` 中添加：

```yaml
dependencies:
  flutter:
    sdk: flutter
  # HTTP请求库
  http: ^1.1.0
  # 阿里云OSS SDK (支持Flutter Web)
  aliyun_oss_dio_sdk: ^2.0.0
  # 或者使用通用HTTP方式调用OSS API
```

### 4.2 创建STS服务类

**文件**: `lib/services/sts_service.dart`

```dart
import 'dart:convert';
import 'package:http/http.dart' as http;

class STSCredentials {
  final String accessKeyId;
  final String accessKeySecret;
  final String securityToken;
  final DateTime expiration;
  final int durationSeconds;

  STSCredentials({
    required this.accessKeyId,
    required this.accessKeySecret,
    required this.securityToken,
    required this.expiration,
    required this.durationSeconds,
  });

  factory STSCredentials.fromJson(Map<String, dynamic> json) {
    return STSCredentials(
      accessKeyId: json['accessKeyId'],
      accessKeySecret: json['accessKeySecret'],
      securityToken: json['securityToken'],
      expiration: DateTime.parse(json['expiration']),
      durationSeconds: json['durationSeconds'],
    );
  }

  bool get isExpired {
    return DateTime.now().isAfter(expiration);
  }
}

class STSService {
  final String baseUrl;
  final String accessToken;

  STSService({
    required this.baseUrl,
    required this.accessToken,
  });

  /// 获取STS临时凭证
  Future<STSCredentials> getSTSCredentials({
    required String bucketName,
    String? objectPrefix,
    String permissionType = 'READ_WRITE',
    int durationSeconds = 3600,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/api/file/upload/sts'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $accessToken',
        },
        body: jsonEncode({
          'bucketName': bucketName,
          'objectPrefix': objectPrefix ?? 'uploads/',
          'permissionType': permissionType,
          'durationSeconds': durationSeconds,
        }),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        if (data['success'] == true) {
          return STSCredentials.fromJson(data['data']);
        } else {
          throw Exception('获取STS凭证失败: ${data['message']}');
        }
      } else {
        throw Exception('请求失败: ${response.statusCode}');
      }
    } catch (e) {
      throw Exception('获取STS凭证异常: $e');
    }
  }
}
```

### 4.3 创建OSS上传服务

**文件**: `lib/services/oss_upload_service.dart`

```dart
import 'dart:io';
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'sts_service.dart';

class OSSUploadService {
  final String bucketName;
  final String region;
  final STSCredentials credentials;

  OSSUploadService({
    required this.bucketName,
    required this.region,
    required this.credentials,
  });

  /// 上传文件到OSS
  Future<String> uploadFile({
    required String objectKey,
    required File file,
    required ProgressCallback onProgress,
  }) async {
    // 检查凭证是否过期
    if (credentials.isExpired) {
      throw Exception('STS凭证已过期，请重新获取');
    }

    try {
      // 读取文件内容
      final bytes = await file.readAsBytes();

      // 构建OSS上传URL
      final uploadUrl = 'https://$bucketName.$region.aliyuncs.com/$objectKey';

      // 使用STS凭证签名
      final timestamp = DateTime.now().toUtc().toIso8601String();
      final signature = _generateSignature(
        'PUT',
        objectKey,
        timestamp,
        bytes.length,
      );

      // 发送上传请求
      final response = await http.put(
        Uri.parse(uploadUrl),
        headers: {
          'Authorization': 'OSS ${credentials.accessKeyId}:$signature',
          'x-oss-security-token': credentials.securityToken,
          'Date': timestamp,
          'Content-Type': 'application/octet-stream',
          'Content-Length': bytes.length.toString(),
        },
        body: bytes,
      );

      if (response.statusCode == 200) {
        // 返回文件URL
        return 'https://$bucketName.$region.aliyuncs.com/$objectKey';
      } else {
        throw Exception('上传失败: ${response.statusCode} - ${response.body}');
      }
    } catch (e) {
      throw Exception('上传异常: $e');
    }
  }

  /// 生成OSS签名
  String _generateSignature(
    String method,
    String objectKey,
    String timestamp,
    int contentLength,
  ) {
    // 构建待签名字符串
    final stringToSign = '$method\n\napplication/octet-stream\n\n$timestamp\n/$bucketName/$objectKey';

    // 使用HMAC-SHA1签名
    final key = utf8.encode(credentials.accessKeySecret);
    final bytes = utf8.encode(stringToSign);

    final hmac = Hmac(sha1, key);
    final digest = hmac.convert(bytes);

    return base64.encode(digest.bytes);
  }
}

// 导入所需的加密库
import 'package:crypto/crypto.dart';
import 'dart:typed_data';
import 'package:pointycastle/export.dart';

Hmac hmac(Hash sha1, List<int> key) {
  return Hmac(sha1, key);
}
```

### 4.4 完整的上传流程示例

**文件**: `lib/pages/upload_page.dart`

```dart
import 'package:flutter/material.dart';
import 'package:file_picker/file_picker.dart';
import 'dart:io';
import 'services/sts_service.dart';
import 'services/oss_upload_service.dart';

class FileUploadPage extends StatefulWidget {
  @override
  _FileUploadPageState createState() => _FileUploadPageState();
}

class _FileUploadPageState extends State<FileUploadPage> {
  File? _selectedFile;
  bool _isUploading = false;
  double _uploadProgress = 0.0;
  String? _uploadedFileUrl;

  // 初始化服务
  final stsService = STSService(
    baseUrl: 'http://localhost:8080', // duda-file服务地址
    accessToken: 'your-user-token', // 用户登录token
  );

  Future<void> _selectFile() async {
    final result = await FilePicker.platform.pickFiles();
    if (result != null && result.files.single.path != null) {
      setState(() {
        _selectedFile = File(result.files.single.path!);
      });
    }
  }

  Future<void> _uploadFile() async {
    if (_selectedFile == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('请先选择文件')),
      );
      return;
    }

    setState(() {
      _isUploading = true;
      _uploadProgress = 0.0;
    });

    try {
      // 1. 获取STS临时凭证
      final credentials = await stsService.getSTSCredentials(
        bucketName: 'duda-file-bucket',
        objectPrefix: 'flutter-uploads/',
        permissionType: 'WRITE',
        durationSeconds: 3600,
      );

      // 2. 创建OSS上传服务
      final uploadService = OSSUploadService(
        bucketName: 'duda-file-bucket',
        region: 'cn-hangzhou',
        credentials: credentials,
      );

      // 3. 生成唯一的object key
      final objectKey = 'flutter-uploads/${DateTime.now().millisecondsSinceEpoch}_${_selectedFile!.path.split('/').last}';

      // 4. 上传文件
      final fileUrl = await uploadService.uploadFile(
        objectKey: objectKey,
        file: _selectedFile!,
        onProgress: (sent, total) {
          setState(() {
            _uploadProgress = sent / total;
          });
        },
      );

      setState(() {
        _uploadedFileUrl = fileUrl;
        _isUploading = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('文件上传成功！')),
      );

    } catch (e) {
      setState(() {
        _isUploading = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('上传失败: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Flutter Web文件上传')),
      body: Padding(
        padding: EdgeInsets.all(16.0),
        child: Column(
          children: [
            // 文件选择按钮
            ElevatedButton(
              onPressed: _selectFile,
              child: Text('选择文件'),
            ),

            // 显示选中的文件
            if (_selectedFile != null)
              Text('已选择: ${_selectedFile!.path.split('/').last}'),

            SizedBox(height: 20),

            // 上传按钮
            ElevatedButton(
              onPressed: _isUploading ? null : _uploadFile,
              child: Text(_isUploading ? '上传中...' : '开始上传'),
            ),

            // 上传进度
            if (_isUploading)
              Column(
                children: [
                  LinearProgressIndicator(value: _uploadProgress),
                  Text('${(_uploadProgress * 100).toStringAsFixed(1)}%'),
                ],
              ),

            // 上传结果
            if (_uploadedFileUrl != null)
              Text('文件URL: $_uploadedFileUrl'),
          ],
        ),
      ),
    );
  }
}
```

---

## 五、最佳实践

### 5.1 STS凭证缓存

```dart
class CachedSTSService {
  STSCredentials? _cachedCredentials;
  DateTime? _cacheTime;

  Future<STSCredentials> getCredentials() async {
    // 如果有缓存且未过期，直接返回
    if (_cachedCredentials != null && !_cachedCredentials!.isExpired) {
      return _cachedCredentials!;
    }

    // 否则重新获取
    _cachedCredentials = await stsService.getSTSCredentials(...);
    _cacheTime = DateTime.now();
    return _cachedCredentials!;
  }
}
```

### 5.2 错误处理

```dart
try {
  // 上传文件
  await uploadService.uploadFile(...);
} on STSExpiredException {
  // STS凭证过期，重新获取
  credentials = await stsService.getSTSCredentials(...);
  // 重试上传
  await uploadService.uploadFile(...);
} on UploadException catch (e) {
  // 上传失败，提示用户
  showError('上传失败: ${e.message}');
}
```

### 5.3 安全建议

1. ✅ **HTTPS传输**: 所有API请求必须使用HTTPS
2. ✅ **临时凭证**: STS凭证有效期不要太长（建议1小时）
3. ✅ **权限最小化**: 只授予必要的权限
4. ✅ **用户隔离**: 不同用户使用不同的前缀路径
5. ✅ **文件大小限制**: 在后端限制单个文件大小
6. ✅ **文件类型限制**: 校验上传文件的MIME类型

---

## 六、总结

### 6.1 STS集成流程

1. ✅ 在duda-file服务中实现STS API
2. ✅ 在阿里云RAM控制台创建角色和权限策略
3. ✅ Flutter Web应用调用STS API获取临时凭证
4. ✅ 使用临时凭证直接上传到OSS
5. ✅ 上传完成，文件已存储在云存储

### 6.2 优势

- ✅ **安全性高**: 临时密钥，自动过期
- ✅ **性能好**: 直接上传到云存储，不经过后端服务器
- ✅ **成本低**: 减少服务器带宽消耗
- ✅ **可扩展**: 支持大量并发上传

---

**文档更新**: 2025-03-13
**作者**: Claude Code
**版本**: v1.0
