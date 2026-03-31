# Duda Tenant 供应链系统 - 文档索引

> 📅 最后更新: 2026年03月30日
> 📂 文档路径: `/Volumes/DudaDate/DudaNexus/工作计划/duda_tenant/`

---

## 📚 文档清单

### 🔴 必读文档（按顺序阅读）

| 序号 | 文档名称 | 说明 | 状态 |
|------|---------|------|------|
| 1 | **[00-架构规范与设计原则.md](./00-架构规范与设计原则.md)** | ⚠️ 最重要！架构规范、使用已有组件、三层架构 | ✅ 完成 |
| 2 | **[01-技术架构设计.md](./01-技术架构设计.md)** | 技术架构、数据一致性、Redis/MQ使用 | ✅ 完成 |
| 3 | **[02-业务流程设计文档.md](./商品供应链分发系统-业务流程设计文档.md)** | 业务场景、流程图、时序图 | ✅ 完成 |
| 4 | **[03-API接口文档.md](./商品供应链分发系统-API接口文档.md)** | 21个API接口完整定义 | ✅ 完成 |
| 5 | **[04-结算算法设计文档.md](./商品供应链分发系统-结算算法设计文档.md)** | 14个核心算法设计 | ✅ 完成 |
| 6 | **[05-文档整合与评审报告.md](./文档整合与评审报告.md)** | 完整性检查、可执行性评估 | ✅ 完成 |
| 7 | **[06-工作计划.md](./2026-03-30_商品供应链分发系统-接口与算法设计工作计划.md)** | 工作计划、执行记录 | ✅ 完成 |

---

## ⚠️ 核心原则（必须遵守）

### 1. 使用已有组件

**🔴 禁止重复造轮子**:

| 组件 | 模块名 | 状态 |
|------|--------|------|
| Redis | **duda-common-redis** | ✅ 已有，必须使用 |
| RocketMQ | **duda-common-rocketmq** | ✅ 已有，必须使用 |

**❌ 不要这样做**:
```java
// ❌ 不要自己创建Redis客户端
@Autowired
private RedisTemplate redisTemplate;

// ❌ 不要自己创建MQ客户端
@Autowired
private RocketMQTemplate rocketMQTemplate;
```

**✅ 应该这样做**:
```java
// ✅ 使用注入的公共组件
@Autowired
private DudaRedisClient dudaRedisClient;

@Autowired
private DudaRedissonClient dudaRedissonClient;

@Autowired
private DudaRocketMQTemplate dudaRocketMQTemplate;
```

---

### 2. 三层架构规范

所有模块必须遵循统一架构:

```
duda-supply-api (网关层)
  ├─ Controller → Service → ServiceImpl (调用Nacos RPC)

duda-supply-interface (接口定义层)
  ├─ DTO、枚举、RPC接口定义

duda-supply-provider (服务提供者)
  ├─ RpcImpl (@Service, 注册到Nacos) → Service → ServiceImpl → Mapper
```

**调用链路**:
```
Controller (API层)
  ↓
ApiService (API层)
  ↓
ApiServiceImpl (API层)
  ↓ (Feign + Nacos)
RPC接口 (Interface层)
  ↓ 实现
RpcImpl (Provider层, @Service)
  ↓
Service (Provider层)
  ↓
ServiceImpl (Provider层)
  ↓
Mapper (Provider层)
  ↓
数据库
```

---

## 🚀 快速开始

### 开发前必读

1. ✅ 阅读 **[00-架构规范与设计原则.md](./00-架构规范与设计原则.md)**
   - 了解架构规范
   - 了解如何使用已有的Redis和MQ组件
   - 了解三层架构职责

2. ✅ 阅读 **[01-技术架构设计.md](./01-技术架构设计.md)**
   - 了解数据一致性方案
   - 了解Redis使用场景
   - 了解RocketMQ使用场景

3. ✅ 阅读 **[02-业务流程设计文档.md](./商品供应链分发系统-业务流程设计文档.md)**
   - 了解6个核心业务场景
   - 查看业务流程图和时序图

4. ✅ 阅读 **[03-API接口文档.md](./商品供应链分发系统-API接口文档.md)**
   - 了解21个API接口定义
   - 了解请求参数和响应格式

### 代码检查清单

开发完成后，使用以下清单检查代码:

- [ ] 是否使用了`DudaRedisClient`而不是`RedisTemplate`？
- [ ] 是否使用了`DudaRedissonClient`而不是自己创建Redisson？
- [ ] 是否使用了`DudaRocketMQTemplate`而不是`RocketMQTemplate`？
- [ ] RpcImpl是否使用`@Service`注解注册到Nacos？
- [ ] Controller是否只调用Service，不直接调用RPC？
- [ ] ServiceImpl是否调用RPC接口(Nacos)？
- [ ] Provider的ServiceImpl是否调用Mapper？
- [ ] 所有金额计算是否使用`BigDecimal`？

---

## 📋 项目结构

### 新增模块

```
duda-tenant (父项目)
├── duda-tenant-interface (现有)
├── duda-tenant-provider (现有)
├── duda-tenant-api (现有)
├── duda-supply-interface (新增) - 供应链接口定义
├── duda-supply-provider (新增) - 供应链服务提供者
└── duda-supply-api (新增) - 供应链网关
```

### duda-supply-interface

**职责**: 定义DTO、枚举、RPC接口

```
duda-supply-interface/
└── src/main/java/com/duda/supply/
    ├── api/dto/
    │   ├── SupplyProductDTO.java
    │   ├── SupplyDistributionDTO.java
    │   ├── SupplyOrderDTO.java
    │   ├── SupplySettlementDTO.java
    │   └── enums/
    │       ├── ProductTypeEnum.java
    │       ├── OrderStatusEnum.java
    │       └── SettlementStatusEnum.java
    └── rpc/
        ├── SupplyProductRpc.java
        ├── SupplyDistributionRpc.java
        ├── SupplyOrderRpc.java
        └── SupplySettlementRpc.java
```

### duda-supply-provider

**职责**: 实现RPC接口、业务逻辑

```
duda-supply-provider/
└── src/main/java/com/duda/supply/
    ├── po/ - 持久化对象
    ├── mapper/ - MyBatis Mapper
    ├── rpcimpl/ - RPC接口实现（注册到Nacos）
    ├── service/ - 业务Service接口
    └── serviceimpl/ - 业务Service实现
```

### duda-supply-api

**职责**: 对外提供REST API

```
duda-supply-api/
└── src/main/java/com/duda/supply/
    ├── config/ - 配置
    ├── controller/ - Controller
    ├── service/ - ApiService接口
    └── serviceimpl/ - ApiServiceImpl实现（调用Nacos）
```

---

## 🔧 技术栈

### 核心组件

| 组件 | 选型 | 版本 | 说明 |
|------|------|------|------|
| 服务注册 | Nacos | 2.x | 服务注册与发现 |
| RPC调用 | OpenFeign | 3.x | 声明式HTTP客户端 |
| 分布式锁 | Redisson (duda-common-redis) | 3.x | 分布式锁 |
| 缓存 | Redis (duda-common-redis) | 6.x/7.x | 缓存 |
| 消息队列 | RocketMQ (duda-common-rocketmq) | 4.x/5.x | 消息队列 |
| 持久化 | MyBatis Plus | 3.x | ORM框架 |

---

## 📊 开发进度

### 设计阶段 (✅ 已完成)

- [x] 业务流程设计
- [x] API接口设计
- [x] 结算算法设计
- [x] 技术架构设计
- [x] 文档评审

### 实现阶段 (🔴 待开始)

- [ ] 第一阶段: 基础功能 (1-2周)
  - [ ] 供应商上架商品
  - [ ] 分销商浏览商品
  - [ ] 一键上架
  - [ ] 客户下单
  - [ ] 库存扣减（分布式锁）

- [ ] 第二阶段: 完善功能 (2-3周)
  - [ ] 供应商发货
  - [ ] 物流信息同步（MQ）
  - [ ] 结算计算
  - [ ] 结算转账（MQ）

- [ ] 第三阶段: 服务拆分 (1周)
  - [ ] 新建duda-supply项目
  - [ ] 配置Nacos注册
  - [ ] 配置Feign调用

---

## 💡 常见问题

### Q1: 为什么不能自己引入Redis或MQ依赖？

**A**: 项目已经有统一的公共组件`duda-common-redis`和`duda-common-rocketmq`，这些组件已经封装好了统一的使用方式、配置、异常处理等。重复引入会导致:
- 配置冲突
- 依赖版本不一致
- 运维困难
- 代码风格不统一

### Q2: RpcImpl为什么要注册到Nacos？

**A**: RpcImpl是RPC接口的实现，需要注册到Nacos才能被其他服务调用。如果不注册:
- API层无法通过Feign调用
- 服务发现失败
- 跨服务调用失败

### Q3: 为什么Controller不能直接调用RPC？

**A**: 这是三层架构的设计规范:
- Controller (API层) 应该只接收HTTP请求
- ApiService (API层) 封装业务逻辑
- ApiServiceImpl (API层) 调用RPC (Nacos)
- 这样层次清晰，职责分明

### Q4: 分布式锁什么时候用？

**A**:
- ✅ 库存扣减（防止超卖）
- ✅ 结算转账（防止重复结算）
- ❌ 简单查询（不需要）
- ❌ 单纯更新（无并发冲突时不需要）

---

## 📞 联系方式

如有问题，请参考:
1. [00-架构规范与设计原则.md](./00-架构规范与设计原则.md)
2. [01-技术架构设计.md](./01-技术架构设计.md)

---

**文档版本**: v1.0
**最后更新**: 2026-03-30
**维护者**: AI Assistant

---

*⚠️ 重要提醒：所有开发必须严格遵守架构规范，使用已有的公共组件！*
