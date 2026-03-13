# Nacos配置指南 - API密钥加密

> **适用版本**: duda-file v1.1+
> **更新时间**: 2025-03-13
> **安全等级**: 高

## 一、概述

本文档详细说明如何在Nacos配置中心配置duda-file模块的API密钥加密密钥。

**设计原则**:
- ✅ 密钥统一在Nacos配置中心管理
- ✅ 不同环境使用不同的密钥（开发、测试、生产）
- ✅ 支持密钥动态更新（无需重启服务）
- ✅ 密钥不在代码或服务器硬编码

---

## 二、Nacos配置步骤

### 2.1 登录Nacos控制台

```
URL: http://your-nacos-host:8848/nacos
用户名: nacos
密码: nacos
```

### 2.2 创建配置

点击 **配置管理** → **配置列表** → **+** (创建配置)

---

## 三、配置内容

### 3.1 开发环境配置

**Data ID**: `duda-file-provider.yaml`
**Group**: `DUDA_FILE_GROUP`
**配置格式**: `YAML`

```yaml
# duda-file模块开发环境配置

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://dev-db-host:3306/duda_file?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: dev_user
    password: dev_password

# API密钥加密配置
duda:
  file:
    encryption:
      # 开发环境加密密钥（32位字符串）
      # ⚠️ 生产环境请务必更换此密钥！
      key: "duda-file-dev-encryption-key-2025"

    storage:
      default-type: ALIYUN_OSS
      default-region: cn-hangzhou

# Dubbo配置
dubbo:
  application:
    name: duda-file-provider
  protocol:
    name: dubbo
    port: 20880
  registry:
    address: nacos://your-nacos-host:8848
    group: DUDA_FILE_GROUP
  scan:
    base-packages: com.duda.file.provider

# 日志配置
logging:
  level:
    com.duda.file: INFO
    com.duda.file.mapper: DEBUG
```

---

### 3.2 测试环境配置

**Data ID**: `duda-file-provider-test.yaml`
**Group**: `DUDA_FILE_GROUP`
**配置格式**: `YAML`

```yaml
# duda-file模块测试环境配置

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://test-db-host:3306/duda_file?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: test_user
    password: test_password

# API密钥加密配置
duda:
  file:
    encryption:
      # 测试环境加密密钥（与开发环境不同）
      key: "duda-file-test-encryption-key-2025"

    storage:
      default-type: ALIYUN_OSS
      default-region: cn-hangzhou

# Dubbo配置
dubbo:
  application:
    name: duda-file-provider-test
  protocol:
    name: dubbo
    port: 20881
  registry:
    address: nacos://test-nacos-host:8848
    group: DUDA_FILE_GROUP
  scan:
    base-packages: com.duda.file.provider

# 日志配置
logging:
  level:
    com.duda.file: INFO
    com.duda.file.mapper: INFO
```

---

### 3.3 生产环境配置

**Data ID**: `duda-file-provider-prod.yaml`
**Group**: `DUDA_FILE_GROUP`
**配置格式**: `YAML`

```yaml
# duda-file模块生产环境配置

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://prod-db-host:3306/duda_file?useUnicode=true&characterEncoding=utf8&useSSL=true&serverTimezone=Asia/Shanghai
    username: prod_user
    password: prod_password

# API密钥加密配置
duda:
  file:
    encryption:
      # ⚠️ 生产环境加密密钥
      # 要求:
      # 1. 密钥长度至少32字符
      # 2. 包含大小写字母、数字、特殊字符
      # 3. 定期轮换（建议每季度一次）
      # 4. 仅限授权人员可见
      key: "YOUR-PRODUCTION-ENCRYPTION-KEY-CHANGE-IT"

    storage:
      default-type: ALIYUN_OSS
      default-region: cn-hangzhou

# Dubbo配置
dubbo:
  application:
    name: duda-file-provider-prod
  protocol:
    name: dubbo
    port: 20882
  registry:
    address: nacos://prod-nacos-host:8848
    group: DUDA_FILE_GROUP
  scan:
    base-packages: com.duda.file.provider

# 日志配置
logging:
  level:
    com.duda.file: WARN
    com.duda.file.mapper: WARN
```

---

## 四、密钥生成规范

### 4.1 密钥要求

| 项目 | 要求 | 说明 |
|------|------|------|
| **长度** | 至少32字符 | AES-128需要16字节，建议32字节以上 |
| **字符集** | 大小写字母+数字+特殊字符 | 增强安全性 |
| **唯一性** | 每个环境不同 | 开发、测试、生产环境密钥不同 |
| **轮换周期** | 建议每季度一次 | 定期更换提高安全性 |

### 4.2 密钥生成方法

#### 方法1: 在线工具生成

访问在线密码生成器：
- https://www.random.org/passwords/
- https://lastpass.com/generatepassword/

参数设置：
- 长度: 32字符
- 包含: 大写字母、小写字母、数字、符号
- 排除: 容易混淆的字符（如0OIl1）

#### 方法2: 使用OpenSSL命令

```bash
# 生成32字节随机密钥并Base64编码
openssl rand -base64 32

# 输出示例:
# kK7x9P2qR8mN4vT6wY5zA1bC3dE7fG9hJ0lM2nO4pQ=
```

#### 方法3: 使用Java代码

```java
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionKeyGenerator {

    public static String generateKey() {
        // 生成32字节随机密钥
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);

        // Base64编码
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    public static void main(String[] args) {
        String key = generateKey();
        System.out.println("生成的加密密钥: " + key);
        System.out.println("密钥长度: " + key.length() + " 字符");
    }
}
```

#### 方法4: 使用Python

```python
import secrets
import base64

# 生成32字节随机密钥
key_bytes = secrets.token_bytes(32)
key = base64.b64encode(key_bytes).decode('utf-8')

print(f"生成的加密密钥: {key}")
print(f"密钥长度: {len(key)} 字符")
```

---

## 五、配置管理最佳实践

### 5.1 环境隔离

```
开发环境 (dev)
  ├─ Data ID: duda-file-provider-dev.yaml
  ├─ Group: DUDA_FILE_GROUP
  ├─ 密钥: duda-file-dev-encryption-key-2025
  └─ 数据库: dev-db

测试环境 (test)
  ├─ Data ID: duda-file-provider-test.yaml
  ├─ Group: DUDA_FILE_GROUP
  ├─ 密钥: duda-file-test-encryption-key-2025
  └─ 数据库: test-db

生产环境 (prod)
  ├─ Data ID: duda-file-provider-prod.yaml
  ├─ Group: DUDA_FILE_GROUP
  ├─ 密钥: YOUR-PRODUCTION-ENCRYPTION-KEY-CHANGE-IT
  └─ 数据库: prod-db
```

### 5.2 权限控制

**Nacos权限设置**:

1. **开发环境**:
   - 读取权限: 开发团队
   - 编辑权限: 技术负责人、DevOps
   - 删除权限: 技术负责人

2. **测试环境**:
   - 读取权限: 测试团队、DevOps
   - 编辑权限: DevOps负责人
   - 删除权限: 技术经理

3. **生产环境**:
   - 读取权限: DevOps负责人、系统管理员
   - 编辑权限: 技术总监
   - 删除权限: CTO

### 5.3 配置加密（Nacos原生功能）

Nacos支持配置内容加密，进一步增强安全性：

1. 在Nacos控制台启用配置加密
2. 加密方式: AES-128
3. 加密后的配置在数据库中以密文存储
4. 只有授权的Nacos客户端可以解密

**配置示例**:
```yaml
# 加密前
duda:
  file:
    encryption:
      key: "YOUR-PRODUCTION-ENCRYPTION-KEY"

# 加密后（在Nacos数据库中）
cipher-aes-xxx: "encrypted_content_here"
```

---

## 六、密钥轮换流程

### 6.1 轮换时机

- ✅ 定期轮换（建议每季度）
- ✅ 发生安全事件时
- ✅ 密钥泄露风险时
- ✅ 人员变动（核心开发离职）

### 6.2 轮换步骤

#### 步骤1: 准备新密钥

```bash
# 生成新的加密密钥
openssl rand -base64 32

# 输出:
# NEW_KEY_HERE_32_CHARACTERS_LONG
```

#### 步骤2: 更新Nacos配置

1. 登录Nacos控制台
2. 找到对应环境的配置
3. 修改 `duda.file.encryption.key` 为新密钥
4. 点击 **发布**

```yaml
# 旧密钥
duda:
  file:
    encryption:
      key: "old-encryption-key-should-be-changed"

# 新密钥
duda:
  file:
    encryption:
      key: "new-encryption-key-generated-20250313"
```

#### 步骤3: 重启服务（如需要）

```bash
# 如果使用了@RefreshScope支持动态刷新，无需重启
# 如果没有，需要重启duda-file-provider服务

kubectl rollout restart deployment duda-file-provider
```

#### 步骤4: 验证

1. 查看应用日志，确认加载了新密钥
2. 测试文件上传/下载功能
3. 确认API密钥解密正常

```bash
# 查看日志
tail -f logs/duda-file-provider.log | grep "encryption"
```

#### 步骤5: 重新加密已有数据

⚠️ **重要**: 密钥轮换后，数据库中已加密的API密钥需要重新加密！

**使用新密钥重新加密**:
```java
@Component
public class KeyRotationService {

    @Autowired
    private BucketConfigMapper bucketConfigMapper;

    @Autowired
    private ApiKeyEncryptionHelper encryptionHelper;

    /**
     * 密钥轮换：使用新密钥重新加密所有API密钥
     */
    public void rotateApiKeys() {
        List<BucketConfig> configs = bucketConfigMapper.selectAll();

        for (BucketConfig config : configs) {
            // 1. 假设旧密钥仍然可用，先解密
            String plainKeyId = decryptWithOldKey(config.getAccessKeyId());
            String plainKeySecret = decryptWithOldKey(config.getAccessKeySecret());

            // 2. 使用新密钥重新加密
            String[] encrypted = encryptionHelper.encryptApiKeyPair(plainKeyId, plainKeySecret);

            // 3. 更新数据库
            config.setAccessKeyId(encrypted[0]);
            config.setAccessKeySecret(encrypted[1]);
            bucketConfigMapper.update(config);
        }
    }
}
```

---

## 七、应用配置

### 7.1 application.yml配置

在 `duda-file-provider` 的 `application.yml` 中指定Nacos配置：

```yaml
spring:
  application:
    name: duda-file-provider
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:}
        group: DUDA_FILE_GROUP
        file-extension: yaml
        # 根据环境加载不同的配置
        name: duda-file-provider-${ENV:dev}
        # 支持配置自动刷新
        refresh-enabled: true
```

### 7.2 启动参数

```bash
# 开发环境
java -jar duda-file-provider.jar --spring.profiles.active=dev

# 测试环境
java -jar duda-file-provider.jar --spring.profiles.active=test

# 生产环境
java -jar duda-file-provider.jar --spring.profiles.active=prod
```

### 7.3 Docker部署

```dockerfile
FROM openjdk:11-jre-slim

# 设置环境变量
ENV ENV=prod
ENV NACOS_ADDR=nacos-prod:8848

COPY duda-file-provider.jar /app/
WORKDIR /app

CMD ["java", "-jar", "duda-file-provider.jar", "--spring.profiles.active=${ENV}"]
```

### 7.4 Kubernetes部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: duda-file-provider
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: duda-file-provider
        image: duda-file-provider:latest
        env:
        - name: ENV
          value: "prod"
        - name: NACOS_ADDR
          value: "nacos-prod:8848"
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
```

---

## 八、配置验证

### 8.1 验证配置加载成功

查看启动日志：

```log
2025-03-13 10:00:00.123 INFO  o.s.c.c.c.ConfigServiceBeanLocator - Located property source: [BootstrapPropertySource {name='bootstrapProperties-duda-file-provider-prod.yaml'}]
2025-03-13 10:00:00.456 INFO  c.d.f.p.impl.BucketServiceImpl - API密钥加密密钥已加载
2025-03-13 10:00:00.789 INFO  c.d.f.p.BucketServiceImpl - Dubbo服务启动成功
```

### 8.2 验证加密/解密功能

```java
@Test
public void testEncryptionWithNacosConfig() {
    // 从Nacos读取的配置
    String encryptionKey = encryptionHelper.getEncryptionKey();

    assertNotNull(encryptionKey);
    assertFalse(encryptionKey.contains("dev"));
    assertFalse(encryptionKey.contains("test"));

    // 测试加密解密
    String originalKey = "LTAI5tXXXXXXXXXXXXXX";
    String encrypted = AesUtil.encrypt(originalKey, encryptionKey);
    String decrypted = AesUtil.decrypt(encrypted, encryptionKey);

    assertEquals(originalKey, decrypted);
}
```

---

## 九、故障排查

### 9.1 常见问题

#### 问题1: 配置无法加载

**症状**: 启动报错 `Could not resolve placeholder 'duda.file.encryption.key'`

**原因**:
- Nacos连接失败
- Data ID或Group配置错误
- 配置不存在

**解决方案**:
```bash
# 1. 检查Nacos连接
curl http://nacos-host:8848/nacos/v1/cs/configs?dataId=duda-file-provider-prod.yaml&group=DUDA_FILE_GROUP

# 2. 检查application.yml配置
spring:
  cloud:
    nacos:
      config:
        server-addr: correct-nacos-host:8848
        group: DUDA_FILE_GROUP
        name: duda-file-provider-prod

# 3. 确认配置已在Nacos创建
登录Nacos控制台检查配置列表
```

#### 问题2: API密钥解密失败

**症状**: `DECRYPTION_FAILED` 异常

**原因**:
- 加密密钥与加密时使用的密钥不一致
- 密钥在Nacos中被修改

**解决方案**:
```bash
# 1. 检查Nacos配置中的密钥
登录Nacos控制台 → 查看duda.file.encryption.key

# 2. 确认数据库中的API密钥是使用哪个密钥加密的

# 3. 如果密钥已更改，需要重新加密数据库中的API密钥
使用KeyRotationService重新加密
```

#### 问题3: 密钥长度不足

**症状**: `InvalidKeyException: Illegal key size`

**原因**: 加密密钥长度不足16字节

**解决方案**:
```yaml
# ❌ 错误: 密钥太短
duda:
  file:
    encryption:
      key: "short"  # 只有5个字符

# ✅ 正确: 密钥至少32字符
duda:
  file:
    encryption:
      key: "duda-file-production-encryption-key-32-characters-long"
```

---

## 十、安全建议

### 10.1 密钥管理

1. ✅ **密钥存储**:
   - 仅存储在Nacos配置中心
   - 不在代码中硬编码
   - 不在服务器环境变量中明文存储

2. ✅ **密钥访问**:
   - 生产环境密钥仅授权人员可见
   - 定期审计密钥访问日志
   - 离职人员立即撤销权限

3. ✅ **密钥轮换**:
   - 每季度定期更换
   - 发生安全事件立即更换
   - 轮换后重新加密已有数据

### 10.2 审计日志

启用Nacos操作审计日志：

```yaml
# Nacos配置
nacos:
  audit:
    enabled: true
    log:
      path: /var/log/nacos/audit.log
```

监控配置变更：
- 谁修改了加密密钥
- 修改时间
- 修改前后内容对比

### 10.3 备份策略

1. **配置备份**:
   - 每日自动备份Nacos配置
   - 备份文件加密存储
   - 异地备份

2. **密钥备份**:
   - 生产密钥密封存储在密码保险箱
   - 仅限紧急情况下使用
   - 使用记录需审批

---

## 十一、快速检查清单

部署前检查：

- [ ] Nacos配置已创建（dev/test/prod）
- [ ] 加密密钥已生成并配置
- [ ] 不同环境使用不同密钥
- [ ] 密钥长度至少32字符
- [ ] Nacos权限已正确配置
- [ ] 应用配置正确指向Nacos
- [ ] 配置刷新已启用
- [ ] 日志中有密钥加载成功的记录
- [ ] 加密/解密功能测试通过
- [ ] 密钥轮换流程已文档化

---

## 十二、联系支持

如有问题，请联系：

- **技术支持**: tech-support@duda.com
- **DevOps团队**: devops@duda.com
- **安全问题**: security@duda.com

---

**文档更新**: 2025-03-13
**作者**: Claude Code
**版本**: v1.0
