# Nacos 命名空间设计建议

## 🎯 推荐方案：环境隔离 + Group 业务隔离

### 命名空间规划

| 命名空间 ID | 名称 | 描述 |
|-----------|------|------|
| `public` | 生产环境 | 线上生产环境 |
| `duda-dev` | 开发环境 | 本地开发测试 |
| `duda-test` | 测试环境 | 预发布测试 |

### Group 规划（业务隔离）

| Group | 用途 | 包含服务 |
|-------|------|---------|
| `AUTH_GROUP` | 认证授权业务 | duda-auth-provider |
| `USER_GROUP` | 用户业务 | duda-user-provider |
| `ORDER_GROUP` | 订单业务 | duda-order-provider |
| `CONTENT_GROUP` | 内容业务 | duda-content-provider |
| `SEARCH_GROUP` | 搜索业务 | duda-search-provider |
| `NOTIFICATION_GROUP` | 通知业务 | duda-notification-provider |
| `GATEWAY_GROUP` | 网关业务 | duda-gateway |
| `COMMON_GROUP` | 公共配置 | 无服务，仅配置 |

---

## ✅ 这种设计的优势

### 1. 简单清晰
- 开发/测试/生产环境完全隔离
- 配置不会相互影响
- 团队易于理解和使用

### 2. 业务隔离（通过 Group）
- 不同业务使用不同的 Group
- 配置按业务分组管理
- 易于权限控制

### 3. 易于拆分和迁移
当需要将某个业务拆分到独立服务器时：

**场景：将订单业务迁移到独立服务器**

1. 在新服务器上部署订单相关服务
2. 只需要修改 `bootstrap.yml` 中的 Nacos 地址
3. 服务仍然注册到同一个命名空间
4. 通过 Nacos 的服务发现自动打通

**关键优势：**
- ✅ 不需要创建新的命名空间
- ✅ 配置可以复用
- ✅ 服务间调用不需要大改

### 4. 灵活的配置管理

```yaml
# 公共配置（所有服务共享）
Data ID: common-dev.yml
Group: COMMON_GROUP

# 认证服务配置
Data ID: duda-auth-provider-dev.yml
Group: AUTH_GROUP

# 用户服务配置
Data ID: duda-user-provider-dev.yml
Group: USER_GROUP
```

---

## 🔧 实施步骤

### 步骤 1：创建命名空间

登录 Nacos 控制台，创建以下命名空间：

```
1. duda-dev（开发环境）
2. duda-test（测试环境）
```

**注意：** `public` 命名空间默认存在，用于生产环境

### 步骤 2：在 duda-dev 命名空间下创建 Group

Nacos 会自动创建 Group，你只需要在创建配置时选择对应的 Group 即可。

### 步骤 3：创建配置文件

#### 公共配置（所有服务共享）

```
命名空间: duda-dev
Data ID: common-dev.yml
Group: COMMON_GROUP
```

#### 认证服务配置

```
命名空间: duda-dev
Data ID: duda-auth-provider-dev.yml
Group: AUTH_GROUP
```

#### 用户服务配置

```
命名空间: duda-dev
Data ID: duda-user-provider-dev.yml
Group: USER_GROUP
```

---

## 🚀 服务拆分到独立服务器的步骤

### 场景：将订单业务拆分到独立服务器

#### 当前架构
```
服务器 A (120.26.170.213)
├── Nacos (8848)
├── Redis (6379)
├── MySQL (3306)
├── 所有微服务
│   ├── duda-auth-provider
│   ├── duda-user-provider
│   ├── duda-order-provider  ← 需要迁移
│   └── ...
```

#### 目标架构
```
服务器 A (120.26.170.213)
├── Nacos (8848)
├── Redis (6379)
├── MySQL (3306)
├── 核心服务
│   ├── duda-auth-provider
│   ├── duda-user-provider
│   └── ...

服务器 B (新服务器)
├── duda-order-provider  ← 迁移到这里
├── duda-payment-provider
└── 其他订单相关服务
```

#### 迁移步骤

**1. 在新服务器上部署服务**
```bash
# 新服务器上的配置
spring:
  cloud:
    nacos:
      server-addr: 120.26.170.213:8848  # 仍然指向原 Nacos
      discovery:
        namespace: duda-dev
        group: ORDER_GROUP
```

**2. 服务启动后**
- 服务自动注册到 Nacos
- 其他服务（如用户服务）可以通过 Nacos 发现订单服务
- 调用地址透明，不需要修改代码

**3. 数据库迁移（可选）**
```sql
-- 如果订单数据库也需要迁移
-- 1. 导出数据
mysqldump -h 120.26.170.213 -u root -p duda_order > order_backup.sql

-- 2. 导入到新数据库
mysql -h 新服务器 -u root -p duda_order < order_backup.sql

-- 3. 修改配置
spring:
  datasource:
    url: jdbc:mysql://新服务器:3306/duda_order
```

---

## 🎨 配置文件示例

### bootstrap.yml（使用 Group）

```yaml
spring:
  application:
    name: duda-order-provider

  cloud:
    nacos:
      username: nacos
      password: nacos

      # 服务发现
      discovery:
        server-addr: 120.26.170.213:8848
        namespace: duda-dev
        group: ORDER_GROUP  # ← 业务 Group

      # 配置中心
      config:
        server-addr: 120.26.170.213:8848
        namespace: duda-dev
        group: ORDER_GROUP  # ← 业务 Group
        file-extension: yml

        # 共享配置
        shared-configs:
          - data-id: common-dev.yml
            group: COMMON_GROUP  # ← 引用公共 Group 的配置
            refresh: true
```

---

## 📊 完整的命名空间 + Group 规划

### duda-dev 命名空间

| Group | 服务 | 配置文件 |
|-------|------|---------|
| COMMON_GROUP | - | common-dev.yml |
| AUTH_GROUP | duda-auth-provider | duda-auth-provider-dev.yml |
| USER_GROUP | duda-user-provider | duda-user-provider-dev.yml |
| ORDER_GROUP | duda-order-provider | duda-order-provider-dev.yml |
| CONTENT_GROUP | duda-content-provider | duda-content-provider-dev.yml |
| SEARCH_GROUP | duda-search-provider | duda-search-provider-dev.yml |
| NOTIFICATION_GROUP | duda-notification-provider | duda-notification-provider-dev.yml |
| GATEWAY_GROUP | duda-gateway | duda-gateway-dev.yml |

### duda-test 命名空间

同样的 Group 结构，但配置文件后缀为 `-test.yml`

### public 命名空间（生产环境）

同样的 Group 结构，但配置文件后缀为 `-prod.yml`

---

## ⚠️ 注意事项

### 1. 跨 Group 调用

不同 Group 的服务可以相互调用，Nacos 服务发现会自动处理：

```java
// 认证服务（AUTH_GROUP）调用用户服务（USER_GROUP）
@FeignClient(name = "duda-user-provider")
public interface UserClient {
    // Nacos 会自动解析服务地址，不关心 Group
}
```

### 2. 配置优先级

```
服务自己的配置 > shared-configs 配置
```

如果配置有冲突，服务自己的配置优先。

### 3. 权限管理

Nacos 支持按 Group 配置权限：

```
认证团队 → AUTH_GROUP 的读写权限
用户团队 → USER_GROUP 的读写权限
运维团队 → 所有 Group 的权限
```

---

## 🚀 快速开始

### 1. 创建命名空间

在 Nacos 控制台创建：
- `duda-dev`（开发环境）
- `duda-test`（测试环境）

### 2. 修改 bootstrap.yml

将 Group 改为对应的业务 Group：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        group: AUTH_GROUP  # 认证服务使用 AUTH_GROUP
      config:
        group: AUTH_GROUP
        shared-configs:
          - data-id: common-dev.yml
            group: COMMON_GROUP  # 公共配置使用 COMMON_GROUP
```

### 3. 在 Nacos 创建配置

按 Group 创建配置文件（参考上面的表格）

---

## 🎯 总结

**推荐使用：环境隔离（命名空间）+ 业务隔离（Group）**

这种方案的优势：
1. ✅ 简单清晰，易于管理
2. ✅ 环境隔离，配置不会冲突
3. ✅ 业务隔离，便于权限控制
4. ✅ 易于服务拆分和迁移
5. ✅ 符合微服务最佳实践

**什么时候考虑方案二（纯业务命名空间）？**
- 业务线非常独立（如不同的子公司）
- 每个业务线有独立的运维团队
- 需要完全的配置隔离
- 预计会完全拆分到不同的 Nacos 集群

对于 DudaNexus 项目，**方案一（环境隔离 + Group）是最优选择**。

---

**更新时间**: 2026-03-10
