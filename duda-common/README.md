# DudaNexus 公共模块

## 📦 模块列表

### duda-common-core
**核心公共模块**，包含：
- 工具类：`IdGenerator`、`BeanUtils`
- 领域对象：`Result`、`PageResult`、`PageQuery`
- 实体类：`BaseEntity`
- 枚举类：`StatusEnum`、`HttpCodeEnum`

**依赖：**
- lombok
- mybatis-plus-core
- spring-boot-starter-web

### duda-common-web
**Web公共模块**，包含：
- 全局异常处理器：`GlobalExceptionHandler`
- 业务异常类：`BizException`
- 全局响应增强：`ResponseAdvisor`
- 跨域配置：`CorsConfig`
- 注解：`@IgnoreResponseWrapper`

**依赖：**
- duda-common-core
- spring-boot-starter-web
- spring-boot-starter-validation

### duda-common-database
**数据库公共模块**，包含：
- MyBatis-Plus自动填充：`MyMetaObjectHandler`

**依赖：**
- duda-common-core
- mybatis-plus-spring-boot3-starter

### duda-common-redis
**Redis公共模块**（待完善）

### duda-common-security
**安全公共模块**（待完善）

---

## 🎯 使用方法

### 1. 添加依赖

在其他服务的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>com.duda</groupId>
    <artifactId>duda-common-core</artifactId>
</dependency>

<dependency>
    <groupId>com.duda</groupId>
    <artifactId>duda-common-web</artifactId>
</dependency>

<dependency>
    <groupId>com.duda</groupId>
    <artifactId>duda-common-database</artifactId>
</dependency>
```

### 2. 快速开始

参考文档：`docs/公共模块使用指南.md`

---

## 📝 工具类清单

| 工具类 | 位置 | 功能 |
|--------|------|------|
| IdGenerator | com.duda.common.util | 生成雪花ID |
| BeanUtils | com.duda.common.util | 对象转换 |
| Result | com.duda.common.domain | 统一响应结果 |
| PageResult | com.duda.common.domain | 分页结果 |
| PageQuery | com.duda.common.domain | 分页查询参数 |
| BaseEntity | com.duda.common.entity | 基础实体类 |
| BizException | com.duda.common.web.exception | 业务异常 |

---

## 🚀 开始开发

详细使用教程：`docs/公共模块使用指南.md`
