# DudaNexus 微服务 Dubbo 配置规范

## 📋 概述

本文档定义 DudaNexus 微服务平台的 Dubbo RPC 服务配置规范，包括服务分组、版本管理和跨服务调用规则。

## 🏗️ 服务分组架构

### 分组设计原则

**按服务层次分组，便于管理和权限控制**

```
INFRA_GROUP (基础设施服务)
└── duda-id-generator (ID生成器)

USER_GROUP (业务服务)
├── duda-user-provider (用户服务提供者)
├── duda-user-api (用户服务API)
└── ... 其他业务服务

AUTH_GROUP (认证服务)
└── duda-auth-provider

ORDER_GROUP (订单服务)
└── duda-order-provider

...
```

## 🔧 核心配置规则

### ⭐ 规则1: 使用多 Registry 实现跨 Group 调用（推荐）

**这是 DudaNexus 的标准配置方案，已验证可用！**

```yaml
# bootstrap.yml
dubbo:
  registries:
    # Registry 1: 基础设施服务（INFRA_GROUP）
    infraRegistry:
      address: nacos://${spring.cloud.nacos.server-addr}
      parameters:
        namespace: ${NACOS_NAMESPACE:duda-dev}
        group: INFRA_GROUP
      default: false  # 不是默认

    # Registry 2: 业务服务（USER_GROUP）
    userRegistry:
      address: nacos://${spring.cloud.nacos.server-addr}
      parameters:
        namespace: ${NACOS_NAMESPACE:duda-dev}
        group: USER_GROUP
      default: true  # 默认 registry
```

**调用不同 Group 的服务**：
```java
// 指定使用 infraRegistry
@DubboReference(
    group = "INFRA_GROUP",
    version = "1.0.0",
    registry = "infraRegistry"  // ← 关键！指定 registry
)
private IdGeneratorRpc idGeneratorRpc;

// 使用默认 registry (userRegistry)
@DubboReference(
    version = "1.0.0",
    group = "USER_GROUP"
)
private IUserRpc userRpc;
```

### 规则2: @DubboService 必须指定 group

```java
@DubboService(
    version = "1.0.0",
    group = "INFRA_GROUP"  // ← 必须！与服务的 group 一致
)
public class IdGeneratorRpcImpl implements IdGeneratorRpc {
}
```

### 规则3: 版本号必须统一

```java
// Provider
@DubboService(version = "1.0.0", group = "INFRA_GROUP")

// Consumer
@DubboReference(version = "1.0.0", group = "INFRA_GROUP", registry = "infraRegistry")
```

## 📝 完整配置示例

### 基础设施服务（INFRA_GROUP）

**duda-id-generator-provider/src/main/resources/bootstrap.yml**

```yaml
dubbo:
  application:
    name: duda-id-generator
  protocol:
    name: dubbo
    port: -1
  registry:
    address: nacos://${spring.cloud.nacos.server-addr}
    parameters:
      namespace: ${NACOS_NAMESPACE:duda-dev}
      group: INFRA_GROUP
```

**duda-id-generator-provider/src/main/java/com/duda/id/rpc/IdGeneratorRpcImpl.java**

```java
@DubboService(
    version = "1.0.0",
    group = "INFRA_GROUP",
    timeout = 5000,
    retries = 0
)
public class IdGeneratorRpcImpl implements IdGeneratorRpc {
    // ...
}
```

### 业务服务（USER_GROUP）

**duda-user-provider/src/main/resources/bootstrap.yml**

```yaml
dubbo:
  application:
    name: duda-user-provider
  protocol:
    name: dubbo
    port: -1

  # ⭐ 多 Registry 配置
  registries:
    infraRegistry:
      address: nacos://${spring.cloud.nacos.server-addr}
      parameters:
        namespace: ${NACOS_NAMESPACE:duda-dev}
        group: INFRA_GROUP
      default: false

    userRegistry:
      address: nacos://${spring.cloud.nacos.server-addr}
      parameters:
        namespace: ${NACOS_NAMESPACE:duda-dev}
        group: USER_GROUP
      default: true

  scan:
    base-packages: com.duda.user.rpc
```

**duda-user-provider/src/main/java/com/duda/user/rpc/UserRpcImpl.java**

```java
@DubboService(
    version = "1.0.0",
    group = "USER_GROUP",
    timeout = 5000
)
public class UserRpcImpl implements IUserRpc {
    // ...
}
```

**duda-user-provider/src/main/java/com/duda/user/service/impl/UserServiceImpl.java**

```java
@Service
public class UserServiceImpl {

    // ⭐ 跨 Group 调用：指定 registry
    @DubboReference(
        group = "INFRA_GROUP",
        version = "1.0.0",
        registry = "infraRegistry"  // ← 使用 infraRegistry
    )
    private IdGeneratorRpc idGeneratorRpc;

    public Long register(UserRegisterReqDTO req) {
        // 调用 RPC 生成雪花ID
        Long userId = idGeneratorRpc.generateUserId();
        // ...
    }
}
```

**duda-user-api/src/main/resources/bootstrap.yml**

```yaml
dubbo:
  application:
    name: duda-user-api
  protocol:
    name: dubbo
    port: -1
  registry:
    address: nacos://${spring.cloud.nacos.server-addr}
    parameters:
      namespace: ${NACOS_NAMESPACE:duda-dev}
      group: USER_GROUP
  consumer:
    check: false
    timeout: 5000
    retries: 2
```

**duda-user-api/src/main/java/com/duda/user/api/controller/UserController.java**

```java
@RestController
public class UserController {

    @DubboReference(
        version = "1.0.0",
        group = "USER_GROUP"  // 同 Group 调用
    )
    private IUserRpc userRpc;
}
```

## ⚠️ 常见问题及解决方案

### 问题1: Invokers Size 为 0

**现象**：
```
Urls Size : 1. Invokers Size : 0. Available Size: 0
```

**原因**：Registry 的 group 配置限制了服务发现范围

**解决方案**：
1. ✅ **使用多 Registry 配置**（推荐）
2. 或者：去掉 registry 的 group，在每个 @DubboReference 中指定

### 问题2: 跨 Group 调用失败

**现象**：
```
No provider available for service INFRA_GROUP/com.duda.id.api.IdGeneratorRpc
```

**原因**：没有指定正确的 registry

**解决方案**：
```java
@DubboReference(
    group = "INFRA_GROUP",
    version = "1.0.0",
    registry = "infraRegistry"  // ← 必须指定！
)
private IdGeneratorRpc idGeneratorRpc;
```

### 问题3: 服务重复注册在多个 Group

**现象**：Nacos 控制台看到同一个服务注册在多个 Group

**原因**：配置冲突或旧服务未停止

**解决方案**：
1. 停止所有旧服务：`ps aux | grep duda- | grep -v grep | awk '{print $2}' | xargs kill -9`
2. 清理 Nacos 上的旧注册
3. 重新编译并启动服务

## ✅ 配置检查清单

### 多 Registry 配置检查

- [ ] `registries` 配置正确（至少2个）
- [ ] 每个 registry 有明确的 group
- [ ] 指定默认 registry (`default: true`)
- [ ] 跨 Group 调用明确指定 `registry`

### Provider 配置检查

- [ ] `@DubboService` 的 group 与服务所属 group 一致
- [ ] `@DubboService` 的 version 明确指定
- [ ] 服务注册到正确的 Registry

### Consumer 配置检查

- [ ] 跨 Group 调用指定 `registry` 参数
- [ ] 同 Group 调用使用默认 registry
- [ ] `@DubboReference` 的 version 与 provider 一致

## 📊 Nacos 服务注册示例

### 服务列表应该看到的：

```
服务管理 → 服务列表 → duda-dev 命名空间

INFRA_GROUP 分组
├── duda-id-generator (HTTP: 9090)
├── providers:com.duda.id.api.IdGeneratorRpc:1.0.0:INFRA_GROUP (Dubbo: 20880)
└── com.duda.id.api.IdGeneratorRpc (mapping)

USER_GROUP 分组
├── duda-user-provider (HTTP: 8082)
├── duda-user-api (HTTP: 8083)
├── providers:com.duda.user.rpc.IUserRpc:1.0.0:USER_GROUP (Dubbo: 20881)
└── com.duda.user.rpc.IUserRpc (mapping)
```

## 🚀 启动顺序

1. **基础设施服务**（INFRA_GROUP）
   ```bash
   # duda-id-generator
   java -jar duda-id-generator-provider.jar
   ```

2. **业务服务提供者**（USER_GROUP）
   ```bash
   # duda-user-provider
   java -jar duda-user-provider.jar
   ```

3. **业务服务 API**（USER_GROUP）
   ```bash
   # duda-user-api
   java -jar duda-user-api.jar
   ```

## 🎯 验证测试

### 测试1: 基础设施服务

```bash
curl http://localhost:9090/actuator/health
```

应该返回：
```json
{"status":"UP","components":{"discoveryClient":{"details":{"services":[
  "duda-id-generator",
  "providers:com.duda.id.api.IdGeneratorRpc:1.0.0:INFRA_GROUP"
]}}}
```

### 测试2: 完整注册流程

```bash
curl -X POST http://localhost:8083/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "password": "123456",
    "phone": "13800000000",
    "email": "test@test.com"
  }'
```

应该返回：
```json
{
  "code": 200,
  "message": "success",
  "data": 4925828868870144  ← 雪花ID
}
```

### 测试3: 验证数据库

```sql
SELECT id, username, phone FROM users_00 WHERE username = 'test';
```

应该看到：
```
id: 4925828868870144
username: test
phone: 13800000000
```

## 📌 版本管理规范

- **所有服务的默认版本**：`1.0.0`
- **版本号格式**：`主版本.次版本.修订版本` (例如：1.0.0, 1.1.0, 2.0.0)
- **升级策略**：
  - 修改 `@DubboService` 的 version
  - 修改 `@DubboReference` 的 version
  - 重新编译并部署 consumer 和 provider
- **多版本共存**：不同版本可以同时运行，consumer 指定调用哪个版本

## 🔄 已验证的配置

以下配置已在生产环境验证可用：

✅ **多 Registry 配置**
- infraRegistry (INFRA_GROUP) - 基础设施服务
- userRegistry (USER_GROUP) - 业务服务

✅ **跨 Group RPC 调用**
- user-provider → id-generator (INFRA_GROUP)
- user-api → user-provider (USER_GROUP)

✅ **雪花ID 生成**
- 连续生成4个不重复ID
- 数据成功保存到分表 (users_00)

✅ **分表配置**
- 100张表 (users_00 ~ users_99)
- 数据按 ID 取模路由

---

**文档版本**：v2.0 (多Registry配置)
**最后更新**：2026-03-11
**验证状态**：✅ 已验证可用
**维护者**：DudaNexus Team
