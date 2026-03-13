# DudaNexus (都达云台)

<div align="center">

![DudaNexus Logo](https://via.placeholder.com/200x200/4A90E2/ffffff?text=DN)

**都达网络科技有限公司 - 微服务综合平台**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-brightgreen.svg)](https://spring.io/projects/spring-cloud)
[![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2023.0.0.0-RC1-brightgreen.svg)](https://github.com/alibaba/spring-cloud-alibaba)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[文档](docs/) | [快速开始](#快速开始) | [技术栈](docs/技术栈详解.md) | [开发指南](docs/接口-实现分离代码结构示例.md)

</div>

---

## 🚀 快速开始

### ⚡ 一键启动（推荐）

**📖 必读**: [项目运行日志](PROJECT_LOG.md) - 包含完整的启动指南、配置说明和故障排查

```bash
cd /Volumes/DudaDate/DudaNexus
docker-compose up -d
```

### 🌐 访问地址

- **Swagger UI**: http://localhost:8083/swagger-ui/index.html
- **健康检查**: http://localhost:8083/actuator/health
- **Nacos 控制台**: http://120.26.170.213:8848/nacos (nacos/nacos)

### 📋 当前服务状态

| 服务 | 端口 | 状态 | 说明 |
|------|------|------|------|
| duda-id-generator | 9090 | ✅ 运行中 | ID生成器 (INFRA_GROUP) |
| duda-msg-provider | 9091 | ✅ 运行中 | 消息服务 (MSG_GROUP) |
| duda-user-provider | 8082 | ✅ 运行中 | 用户服务 (USER_GROUP) |
| duda-user-api | 8083 | ✅ 运行中 | 用户API (36个接口) |

**完整信息请查看**: [📚 PROJECT_LOG.md](PROJECT_LOG.md)

---

## 项目简介

DudaNexus（都达云台）是基于 Spring Cloud 技术栈构建的企业级微服务平台，采用**接口-实现分离**架构，为都达网络科技有限公司提供统一的技术底座，支撑多端应用（Web、App、小程序）的业务需求。

### 核心业务域

- **电商平台**：商品管理、订单交易、支付结算
- **视频直播**：直播推流、实时互动、礼物打赏
- **社交平台**：动态发布、评论点赞、关注关系
- **即时通讯**：单聊群聊、消息推送、文件传输

### 核心特性

- ✅ **接口-实现分离**：模块命名清晰（interface/provider）
- ✅ **微服务架构**：基于 Spring Cloud Alibaba
- ✅ **高可用设计**：支持服务注册发现、配置中心、负载均衡
- ✅ **开发效率高**：MyBatis-Plus + Lombok + MapStruct
- ✅ **国产化组件**：Spring Cloud Alibaba + Hutool

---

## 技术栈

### 核心框架

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 基础框架 |
| Spring Cloud | 2023.0.0 | 微服务框架 |
| Spring Cloud Alibaba | 2023.0.0.0-RC1 | 阿里微服务组件 |
| JDK | 17 (LTS) | 运行环境 |

### 微服务组件

| 组件 | 选型 | 版本 | 说明 |
|------|------|------|------|
| 注册中心 | Alibaba Nacos | 3.0.3 | 服务注册与配置管理 |
| API网关 | Spring Cloud Gateway | 4.1.0 | 统一API入口 |
| 服务调用 | OpenFeign | 4.1.0 | 声明式HTTP客户端 |
| 负载均衡 | LoadBalancer | 4.1.0 | 客户端负载均衡 |

### 数据存储

| 组件 | 选型 | 版本 | 用途 |
|------|------|------|------|
| 关系数据库 | MySQL | 8.0.33 | 核心业务数据 |
| 缓存 | Redis | 7.0+ | 分布式缓存 |
| ORM框架 | MyBatis-Plus | 3.5.5 | 增强ORM工具 |

### 工具类库

| 组件 | 版本 | 说明 |
|------|------|------|
| Hutool | 5.8.24 | Java工具类库 |
| Lombok | 1.18.30 | 简化Java代码 |
| MapStruct | 1.5.5.Final | 对象映射框架 |
| FastJson2 | 2.0.43 | JSON处理 |
| JJWT | 0.12.3 | JWT认证 |

---

## 项目结构

```
DudaNexus
├── docs/                                          # 项目文档
│   ├── 技术栈详解.md                             # 技术栈说明
│   ├── 接口-实现分离代码结构示例.md               # 代码示例
│   ├── 接口-实现分离模式对比分析.md               # 架构对比
│   ├── 模块命名重构计划.md                         # 重构计划
│   ├── 重构完成说明.md                            # 重构说明
│   └── 模块创建完成说明.md                         # 模块说明
│
├── duda-common/                                   # 公共模块
│   ├── duda-common-core/                          # 核心工具类
│   ├── duda-common-web/                           # Web相关
│   ├── duda-common-database/                      # 数据库相关
│   ├── duda-common-redis/                         # Redis相关
│   └── duda-common-security/                      # 安全相关
│
├── duda-gateway/                                  # API网关
│
├── duda-auth/                                     # 认证授权模块
│   ├── duda-auth-interface/                       # 认证接口
│   └── duda-auth-provider/                        # 认证实现
│
├── duda-usercenter/                               # 用户中心模块
│   ├── duda-user-interface/                       # 用户接口
│   ├── duda-user-provider/                        # 用户实现
│   ├── duda-profile-interface/                    # 资料接口
│   ├── duda-profile-provider/                     # 资料实现
│   ├── duda-relation-interface/                   # 关系接口
│   └── duda-relation-provider/                    # 关系实现
│
├── duda-ecommerce/                                # 电商模块
│   ├── duda-e-commerce-interface/                 # 电商接口
│   ├── duda-e-commerce-provider/                  # 电商实现
│   ├── duda-product-interface/                    # 商品接口
│   ├── duda-product-provider/                     # 商品实现
│   ├── duda-order-interface/                      # 订单接口
│   ├── duda-order-provider/                       # 订单实现
│   ├── duda-payment-interface/                    # 支付接口
│   └── duda-payment-provider/                     # 支付实现
│
├── duda-social/                                   # 社交模块
│   ├── duda-social-interface/                     # 社交接口
│   ├── duda-social-provider/                      # 社交实现
│   ├── duda-post-interface/                       # 动态接口
│   ├── duda-post-provider/                        # 动态实现
│   ├── duda-comment-interface/                    # 评论接口
│   ├── duda-comment-provider/                     # 评论实现
│   ├── duda-like-interface/                       # 点赞接口
│   └── duda-like-provider/                        # 点赞实现
│
├── duda-im/                                       # 即时通讯模块
│   ├── duda-im-interface/                         # IM接口
│   ├── duda-im-provider/                          # IM实现
│   ├── duda-push-interface/                       # 推送接口
│   ├── duda-push-provider/                        # 推送实现
│   ├── duda-group-interface/                      # 群组接口
│   └── duda-group-provider/                       # 群组实现
│
├── duda-live/                                     # 直播模块
│   ├── duda-live-interface/                       # 直播接口
│   ├── duda-live-provider/                        # 直播实现
│   ├── duda-stream-interface/                      # 流媒体接口
│   ├── duda-stream-provider/                       # 流媒体实现
│   ├── duda-gift-interface/                        # 礼物接口
│   └── duda-gift-provider/                         # 礼物实现
│
├── duda-content/                                  # 内容管理模块
│   ├── duda-content-service/                       # 内容服务
│   ├── duda-media-service/                         # 媒体服务
│   └── duda-cms-service/                           # CMS服务
│
├── duda-search/                                   # 搜索服务
├── duda-notification/                             # 通知服务
├── duda-monitor/                                  # 监控服务
├── duda-config/                                   # 配置服务
└── duda-logs/                                     # 日志服务
```

---

## 架构设计

### 接口-实现分离模式

项目采用**接口-实现分离**的微服务架构：

```
┌─────────────────────────────────────────────────────────────────┐
│                     其他服务（调用方）                          │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ 依赖（轻量）
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│              XXXXX-interface（接口模块）                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  DTO        │  │  常量        │  │  OpenFeign 接口     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │
                         │ 依赖（完整）
                         ↓
┌─────────────────────────────────────────────────────────────────┐
│              XXXXX-provider（实现模块）                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Controller │  │  Service     │  │  Mapper            │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

**优势：**
- ✅ 调用方只需依赖轻量的 interface 模块
- ✅ 接口定义与实现完全解耦
- ✅ 支持多版本共存
- ✅ 团队协作更清晰

**命名规范：**
- 接口模块：`duda-{业务}-interface`
- 实现模块：`duda-{业务}-provider`

---

## 快速开始

### 环境要求

- **JDK**: 17+ (必须，Spring Boot 3.x 强制要求)
- **Maven**: 3.9+
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **Nacos**: 3.0.3+

### 验证项目

```bash
# 克隆项目
git clone https://github.com/duda/DudaNexus.git
cd DudaNexus

# 验证项目结构
mvn validate

# 编译项目
mvn clean compile
```

### 启动服务

```bash
# 1. 启动基础设施（Nacos、MySQL、Redis）
# 建议使用 Docker Compose

# 2. 启动网关
cd duda-gateway
mvn spring-boot:run

# 3. 启动认证服务
cd duda-auth/duda-auth-provider
mvn spring-boot:run

# 4. 启动用户服务
cd duda-usercenter/duda-user-provider
mvn spring-boot:run
```

### 访问服务

- **Nacos控制台**: http://localhost:8848/nacos (nacos/nacos)
- **Gateway网关**: http://localhost:8080
- **认证服务**: http://localhost:8081

---

## 开发指南

### 模块开发流程

#### 1. 定义接口（interface 模块）

```java
// duda-user-interface/src/main/java/com/duda/user/api/dto/UserDTO.java
package com.duda.user.api.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
}
```

```java
// duda-user-interface/src/main/java/com/duda/user/api/feign/UserClient.java
package com.duda.user.api.feign;

import com.duda.user.api.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "duda-user-provider", path = "/user")
public interface UserClient {
    @GetMapping("/{userId}")
    UserDTO getById(@PathVariable Long userId);
}
```

#### 2. 实现业务（provider 模块）

```java
// duda-user-provider/src/main/java/com/duda/user/controller/UserController.java
package com.duda.user.controller;

import com.duda.user.api.dto.UserDTO;
import com.duda.user.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;

    @GetMapping("/{userId}")
    public UserDTO getById(@PathVariable Long userId) {
        return userService.getById(userId);
    }
}
```

### 命名规范

#### 模块命名
- **接口模块**: `duda-{业务}-{功能}-interface`
- **实现模块**: `duda-{业务}-{功能}-provider`

#### 包命名
- **接口包**: `com.duda.{业务}.api.{dto|feign|constants}`
- **实现包**: `com.duda.{业务}.{controller|service|mapper}`

#### 类命名
- **DTO**: `{业务名}DTO`
- **Feign接口**: `{业务名}Client`
- **Controller**: `{业务名}Controller`
- **Service**: `I{业务名}Service`
- **Mapper**: `{业务名}Mapper`

---

## 文档导航

### 架构文档
- [技术栈详解](docs/技术栈详解.md) - 完整的技术栈说明
- [接口-实现分离模式对比分析](docs/接口-实现分离模式对比分析.md) - 架构对比
- [接口-实现分离代码结构示例](docs/接口-实现分离代码结构示例.md) - 代码示例

### 重构文档
- [重构完成说明](docs/重构完成说明.md) - 接口-实现分离重构说明
- [模块命名重构计划](docs/模块命名重构计划.md) - 重构映射表

### 其他文档
- [模块创建完成说明](docs/模块创建完成说明.md) - 模块创建说明
- [Java版本升级配置说明](docs/Java版本升级配置说明.md) - Java 17 升级说明

---

## 开发规范

### Git 提交规范

**提交格式：**
```
<type>: <subject>

<body>

<footer>
```

**类型（type）：**
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式化
- `refactor`: 重构
- `test`: 测试
- `chore`: 构建/工具链更新

**示例：**
```
feat: 新增用户登录功能

- 添加登录接口
- 集成JWT认证
- 添加登录验证

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

### 代码规范

遵循《阿里巴巴Java开发手册》：

- ✅ 使用驼峰命名法
- ✅ 类名使用大驼峰
- ✅ 方法名和变量名使用小驼峰
- ✅ 常量使用全大写下划线分隔
- ✅ 包名全小写，使用点分隔

---

## 版本规划

### v1.0.0 (2026 Q1)
- [x] 基础服务搭建
- [x] 接口-实现分离架构
- [x] 公共模块开发
- [ ] 用户中心上线
- [ ] 电商平台上线

### v1.1.0 (2026 Q2)
- [ ] 社交平台上线
- [ ] IM系统上线

### v1.2.0 (2026 Q3)
- [ ] 直播平台上线
- [ ] 内容管理上线

---

## 常见问题

### Q: 为什么使用接口-实现分离？

**A:** 主要优势：
1. 调用方只需依赖轻量的 interface 模块
2. 接口定义与实现完全解耦
3. 支持多版本共存
4. 团队协作更清晰

详细说明请参考：[接口-实现分离模式对比分析](docs/接口-实现分离模式对比分析.md)

### Q: Spring Boot 3.x 与 2.x 有什么区别？

**A:** 主要区别：
1. 必须使用 Java 17+
2. 包名从 `javax.*` 改为 `jakarta.*`
3. 性能提升 10-15%
4. 支持虚拟线程（Java 21+）

详细说明请参考：[技术栈详解](docs/技术栈详解.md)

### Q: 如何在 IDEA 中配置项目？

**A:**
1. File → Project Structure → Project
2. SDK: 选择 Java 17
3. Language level: 选择 17
4. File → Reload Maven Project

---

## 贡献指南

欢迎贡献代码！请遵循以下流程：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### Pull Request 规范

- 标题清晰描述更改内容
- 遵循代码规范
- 添加必要的测试
- 通过 Code Review

---

## 联系方式

- **项目地址**: https://github.com/duda/DudaNexus
- **问题反馈**: https://github.com/duda/DudaNexus/issues

---

## 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) 许可证

---

## 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [Spring Cloud Alibaba](https://github.com/alibaba/spring-cloud-alibaba)
- [Nacos](https://github.com/alibaba/nacos)
- [MyBatis-Plus](https://baomidou.com/)
- [Hutool](https://hutool.cn/)

---

<div align="center">

**Made with ❤️ by Duda Network Technology Co., Ltd.**

**接口-实现分离架构 | Spring Boot 3.x | Java 17**

</div>
