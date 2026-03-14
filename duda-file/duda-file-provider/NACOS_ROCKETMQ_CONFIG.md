# Nacos + RocketMQ 配置指南

## 📋 概述

本文档说明如何在 Nacos 中配置 RocketMQ，并测试 Bucket 配置变更的消息发送和消费功能。

---

## 1️⃣ Nacos 配置（RocketMQ）

### **配置名称**
`duda-file-provider-dev.yml` (或其他环境对应的配置文件)

### **配置内容**

```yaml
# RocketMQ 配置
rocketmq:
  name-server: 120.26.170.213:9876  # RocketMQ Nameserver地址
  producer:
    group: BUCKET_CONFIG_PRODUCER_GROUP
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
  consumer:
    group: BUCKET_CONFIG_CONSUMER_GROUP
    consume-thread-min: 5
    consume-thread-max: 10
```

### **配置步骤**

1. **登录 Nacos 控制台**
   - 地址: http://120.26.170.213:8848/nacos
   - 用户名/密码: nacos/nacos

2. **进入配置管理**
   - 点击「配置管理」→「配置列表」

3. **创建/编辑配置**
   - 命名空间: `duda-dev`
   - Group: `DUDA_FILE_GROUP`
   - Data ID: `duda-file-provider-dev.yml`
   - 配置格式: YAML
   - 配置内容: 见上面的配置

4. **发布配置**

---

## 2️⃣ RocketMQ Topic 创建

### **Topic 信息**
- **Topic 名称**: `BUCKET_CONFIG_CHANGE_TOPIC`
- **类型**: 普通消息（或广播消息）

### **创建方式**

#### 方式1: RocketMQ 控制台创建
1. 登录 RocketMQ Console
2. 进入 Topic 管理
3. 创建 Topic: `BUCKET_CONFIG_CHANGE_TOPIC`

#### 方式2: 自动创建（推荐）
- RocketMQ 默认会自动创建 Topic

---

## 3️⃣ 消息格式说明

### **消息结构（JSON）**

```json
{
  "bucketName": "duda-java-backend-test",
  "configType": "CORS|REFERER|VERSIONING|WEBSITE|ACCELERATION|ALL",
  "action": "SET|DELETE|SYNC",
  "timestamp": 1678234567
}
```

### **字段说明**

| 字段 | 说明 | 可选值 |
|------|------|--------|
| bucketName | Bucket名称 | 任意有效的Bucket名称 |
| configType | 配置类型 | CORS, REFERER, VERSIONING, WEBSITE, ACCELERATION, ALL |
| action | 操作类型 | SET（设置）, DELETE（删除）, SYNC（同步） |
| timestamp | 时间戳 | Unix时间戳（秒） |

### **消息示例**

#### 示例1: CORS配置变更
```json
{
  "bucketName": "duda-java-backend-test",
  "configType": "CORS",
  "action": "SET",
  "timestamp": 1678234567
}
```

#### 示例2: 所有配置同步
```json
{
  "bucketName": "duda-java-backend-test",
  "configType": "ALL",
  "action": "SYNC",
  "timestamp": 1678234567
}
```

---

## 4️⃣ 测试步骤

### **步骤1: 启动应用**
```bash
cd /Volumes/DudaDate/DudaNexus/duda-file/duda-file-provider
mvn spring-boot:run
```

### **步骤2: 运行MQ测试**
```bash
mvn exec:java -Dexec.mainClass="com.duda.file.test.MQMessageTest" -Dexec.classpathScope=test
```

### **步骤3: 观察日志**
- 发送日志:
  ```
  → 发送配置变更消息:
    Topic: BUCKET_CONFIG_CHANGE_TOPIC
    Bucket: duda-java-backend-test
    配置类型: CORS
    操作: SET
  ✓ 消息发送成功
  ```

- 消费日志:
  ```
  ╔════════════════════════════════════════╗
  ║   收到Bucket配置变更消息                 ║
  ╚════════════════════════════════════════╝
  消息内容: {...}
  → Bucket名称: duda-java-backend-test
  → 配置类型: CORS
  → 操作类型: SET
  ✓ 消息处理完成
  ```

---

## 5️⃣ 验证服务注册

### **验证方式1: Nacos 控制台**
1. 登录 Nacos 控制台
2. 进入「服务管理」→「服务列表」
3. 查看命名空间 `duda-dev` 中的服务
4. 应该能看到: `duda-file-provider`

### **验证方式2: 命令行**
```bash
curl -X GET 'http://120.26.170.213:8848/nacos/v1/ns/instance/list?serviceName=duda-file-provider&groupName=DUDA_FILE_GROUP&namespaceId=duda-dev'
```

---

## 6️⃣ 集成到服务中

### **在 BucketAuthorizationService 中发送消息**

修改 `BucketAuthorizationService.java`，在每个设置方法中添加消息发送：

```java
@Autowired
private BucketConfigChangeProducer mqProducer;

public SetBucketCORSResultDTO setCORSAndSync(String bucketName, SetBucketCORSReqDTO config) {
    // 1. 设置到OSS
    SetBucketCORSResultDTO result = ossAdapter.setBucketCORS(bucketName, config);

    // 2. 同步到数据库
    syncCORSToDatabase(bucketName, config);

    // 3. 发送MQ消息（新增）
    mqProducer.sendConfigChangeMessage(bucketName, "CORS", "SET");

    log.info("✓ CORS配置已同步到数据库并发送MQ通知");
    return result;
}
```

---

## 7️⃣ 常见问题

### **Q1: RocketMQ 连接失败**
**检查:**
- RocketMQ Nameserver 是否启动
- 端口是否正确（默认9876）
- 防火墙是否开放

### **Q2: 消息发送成功但未被消费**
**检查:**
- Consumer Group 是否正确
- Topic 是否一致
- 查看消费者日志

### **Q3: 服务未注册到 Nacos**
**检查:**
- `bootstrap.yml` 配置是否正确
- Nacos 服务器是否可访问
- 命名空间和分组是否正确

---

## 8️⃣ 监控和日志

### **RocketMQ Console**
- 查看消息堆积
- 查看消费速度
- 查看消息轨迹

### **应用日志**
```log
# 发送消息
→ 发送配置变更消息: ...
✓ 消息发送成功

# 接收消息
╔════════════════════════════════════════╗
║   收到Bucket配置变更消息                 ║
╚════════════════════════════════════════╝
✓ 消息处理完成
```

---

## 9️⃣ 总结

✅ **已完成:**
- Nacos 服务发现配置（已完成）
- RocketMQ 消费者组件
- RocketMQ 生产者组件
- MQ 消息测试类

⏳ **待完成:**
- 在 Nacos 中配置 RocketMQ
- 测试消息发送和消费
- 集成到 BucketAuthorizationService

📞 **需要帮助？**
- RocketMQ 配置问题
- Nacos 配置问题
- 消息测试问题
