# Swagger/OpenAPI 修复手册

## 问题概述

**现象：**
- Swagger UI 可以访问，但显示 "No API definition provided"
- `/v3/api-docs` 端点返回 base64 编码的字符串，而不是 JSON 对象
- OpenAPI 文档无法正常生成

**影响范围：**
- 所有 SpringDoc OpenAPI 相关端点
- Swagger UI 无法显示 API 文档
- 无法进行 API 测试

---

## 根本原因分析

### 问题定位过程

1. **初步排查：检查 Swagger 配置**
   - 配置文件正常：`application.yml` 中的 SpringDoc 配置无误
   - 依赖版本正常：使用 SpringDoc 2.2.0

2. **深入调查：检查响应内容**
   ```bash
   curl http://localhost:8083/v3/api-docs
   # 返回: "eyJvcGVhYXBpIjoiMy4wLjEiLC..." (base64 编码的字符串)

   # 解码后发现是正常的 OpenAPI JSON
   echo "eyJ..." | base64 -d
   # {"openapi":"3.0.1","info":{"title":"DudaNexus API"...}
   ```

3. **关键发现：响应体类型错误**
   - 添加日志到 `ResponseAdvisor` 发现：
   ```
   body type: [B  (字节数组 byte[])
   ```
   - SpringDoc 返回的是 `byte[]` 而不是对象
   - Jackson 将字节数组序列化为 base64 编码的字符串

4. **定位根本原因：WebMvcConfig 配置错误**
   - `WebMvcConfig.java` 使用了 `configureMessageConverters()`
   - 这个方法会**替换**所有消息转换器
   - 破坏了 Spring Boot 默认的 Jackson 配置
   - 导致 SpringDoc 无法正确序列化 OpenAPI 对象

---

## 解决方案

### 1. 修改 WebMvcConfig.java ⭐ 核心修复

**文件路径：** `duda-common/duda-common-web/src/main/java/com/duda/common/web/config/WebMvcConfig.java`

**修改前（错误）：**
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // ❌ 这会替换所有转换器，破坏 Spring Boot 默认配置
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);
        converters.add(stringConverter);

        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setSupportedMediaTypes(List.of(
            MediaType.APPLICATION_JSON,
            new MediaType("application", "*+json")
        ));
        converters.add(0, jsonConverter);  // ❌ 添加到索引 0，替换了默认转换器
    }
}
```

**修改后（正确）：**
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // ✅ 使用 extendMessageConverters 保留默认配置
        // Spring Boot 默认已经配置好了 Jackson 转换器

        // 找到 Jackson 转换器并确认存在
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jsonConverter) {
                // Spring Boot 默认配置，不需要修改
                break;
            }
        }

        // 添加字符串转换器（如果还没有）
        boolean hasStringConverter = false;
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof StringHttpMessageConverter) {
                hasStringConverter = true;
                break;
            }
        }

        if (!hasStringConverter) {
            StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
            stringConverter.setWriteAcceptCharset(false);
            converters.add(stringConverter);
        }
    }
}
```

**关键区别：**
- `configureMessageConverters()` - **替换**所有转换器
- `extendMessageConverters()` - **扩展**现有转换器，保留默认配置 ⭐

---

### 2. 优化 ResponseAdvisor.java

**文件路径：** `duda-common/duda-common-web/src/main/java/com/duda/common/web/config/ResponseAdvisor.java`

**修改内容：**
```java
@RestControllerAdvice
public class ResponseAdvisor implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 检查 @IgnoreResponseWrapper 注解
        IgnoreResponseWrapper annotation = returnType.getMethodAnnotation(IgnoreResponseWrapper.class);
        if (annotation != null && !annotation.value()) {
            return false;
        }

        // ✅ 新增：检查方法返回类型
        Class<?> methodReturnType = returnType.getMethod().getReturnType();
        if (byte[].class.equals(methodReturnType) || void.class.equals(methodReturnType)) {
            return false;  // 不包装字节数组和 void
        }

        // ✅ 新增：排除 SpringDoc 相关的类
        String className = returnType.getDeclaringClass().getName();
        if (className.contains("springdoc") || className.contains("OpenApi")) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, ...) {
        // ✅ 新增：字节数组直接返回，不包装
        if (body instanceof byte[]) {
            return body;
        }

        if (body instanceof Result) {
            return body;
        }

        if (body == null) {
            return Result.success();
        }

        return Result.success(body);
    }
}
```

---

### 3. 修复 Controller 参数名

**文件路径：** `duda-usercenter/duda-user-api/src/main/java/com/duda/user/api/controller/UserController.java`

**问题：** `@RequestParam` 没有指定参数名，导致编译后丢失参数信息

**修改前：**
```java
@GetMapping("/page")
public Result pageUsers(
        @RequestParam(required = false) String userType,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
    ...
}
```

**修改后：**
```java
@GetMapping("/page")
public Result pageUsers(
        @RequestParam(value = "userType", required = false) String userType,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "keyword", required = false) String keyword,
        @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
    ...
}
```

---

### 4. 清理 pom.xml

**文件路径：** `/Volumes/DudaDate/DudaNexus/pom.xml`

**问题：** 包含大量空模块导致构建失败

**解决：** 只保留有代码的模块

```xml
<modules>
    <!-- 公共模块 -->
    <module>duda-common/duda-common-core</module>
    <module>duda-common/duda-common-web</module>
    <module>duda-common/duda-common-database</module>
    <module>duda-common/duda-common-redis</module>
    <module>duda-common/duda-common-rocketmq</module>
    <module>duda-common/duda-common-security</module>

    <!-- 用户中心 -->
    <module>duda-usercenter/duda-user-interface</module>
    <module>duda-usercenter/duda-user-api</module>
    <module>duda-usercenter/duda-user-provider</module>
</modules>
```

**移除的空模块：**
- `duda-auth` - 认证授权（空）
- `duda-ecommerce` - 电商模块（空）
- `duda-social` - 社交模块（空）
- `duda-im` - 即时通讯（空）
- `duda-live` - 直播模块（空）
- 其他支撑服务（空）

---

## 验证修复

### 1. 检查 OpenAPI 文档

```bash
curl -s http://localhost:8083/v3/api-docs | jq .
```

**预期输出：**
```json
{
  "openapi": "3.0.1",
  "info": {
    "title": "DudaNexus API",
    "description": "DudaNexus 微服务系统 API 接口文档",
    "version": "1.0"
  },
  "paths": {
    "/user/update": {...},
    "/user/register": {...},
    "/user/login": {...},
    "/user/{userId}": {...},
    "/user/username/{username}": {...},
    "/user/page": {...}
  }
}
```

### 2. 检查 Swagger UI

访问：http://localhost:8083/swagger-ui.html

**预期结果：**
- ✅ 显示 API 标题："DudaNexus API"
- ✅ 显示 6 个端点
- ✅ 可以展开查看每个接口的详细信息
- ✅ 可以直接在 UI 中测试 API

### 3. 测试 API 调用

```bash
# 测试分页查询
curl "http://localhost:8083/user/page?pageNum=1&pageSize=10"

# 预期返回正确格式的 JSON
{
  "code": 200,
  "message": "success",
  "data": {...},
  "timestamp": ...,
  "success": true
}
```

---

## 技术原理

### Spring Boot 消息转换器机制

**默认转换器列表（按优先级）：**
1. `ByteArrayHttpMessageConverter` - 字节数组
2. `StringHttpMessageConverter` - 字符串
3. `ResourceHttpMessageConverter` - 资源文件
4. `AllEncompassingFormHttpMessageConverter` - 表单数据
5. `MappingJackson2HttpMessageConverter` - JSON ⭐

**问题分析：**
```java
// ❌ 错误做法：configureMessageConverters
public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(0, new MappingJackson2HttpMessageConverter());
    // 问题：添加到索引 0 会覆盖默认的 Jackson 转换器配置
    // 导致 SpringDoc 的 Jackson 配置丢失
}
```

```java
// ✅ 正确做法：extendMessageConverters
public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    // converters 已经包含了 Spring Boot 配置的所有转换器
    // 只需要添加自定义的转换器即可
    // Spring Boot 的 Jackson 配置得以保留
}
```

### SpringDoc 序列化流程

**正常流程（extendMessageConverters）：**
```
OpenAPI 对象 → Jackson ObjectMapper → JSON 字符串 → HTTP 响应
```

**错误流程（configureMessageConverters）：**
```
OpenAPI 对象 → ❌ Jackson 配置丢失 → byte[] → Base64 编码 → "eHl6..."
```

---

## 经验总结

### 关键要点

1. **⚠️ 永远不要使用 `configureMessageConverters()`**
   - 除非你完全知道自己在做什么
   - 这个方法会替换所有默认转换器
   - 应该使用 `extendMessageConverters()`

2. **Spring Boot 自动配置的强大**
   - Spring Boot 已经为 Jackson 提供了完善的默认配置
   - 大多数情况下不需要自定义消息转换器
   - 如果需要，使用 `extend` 方法而不是 `configure`

3. **全局响应包装的注意事项**
   - 需要正确排除第三方端点（SpringDoc、Actuator 等）
   - 检查返回类型（byte[]、void 等）
   - 检查声明类（springdoc、actuator 等）

4. **调试技巧**
   - 添加日志查看响应体类型
   - 使用 `curl` 检查原始响应
   - 使用 `jq` 格式化 JSON 输出
   - 检查端口占用：`lsof -i:port`

### 相关知识点

- **ResponseBodyAdvice**：Spring MVC 响应体增强器
- **HttpMessageConverter**：HTTP 消息转换器
- **SpringDoc OpenAPI**：OpenAPI 3.0 规范的 Spring Boot 实现
- **Jackson ObjectMapper**：JSON 序列化框架
- **Dubbo RPC**：远程过程调用框架

---

## 依赖版本

```xml
<properties>
    <spring-boot.version>3.2.0</spring-boot.version>
    <springdoc.version>2.2.0</springdoc.version>
    <dubbo.version>3.3.2</dubbo.version>
</properties>
```

**兼容性：**
- ✅ Spring Boot 3.2.0 + SpringDoc 2.2.0
- ✅ Java 17
- ✅ Dubbo 3.3.2

---

## 其他相关文件

### application.yml（Swagger 配置）

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    disable-swagger-default-url: true
  show-actuator: false
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
```

### pom.xml（依赖管理）

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${springdoc.version}</version>
</dependency>
```

---

## 故障排查

### 问题 1：Swagger UI 显示 "No API definition provided"

**可能原因：**
- WebMvcConfig 使用了 `configureMessageConverters()`
- ResponseAdvisor 包装了 SpringDoc 响应

**解决方法：**
1. 改用 `extendMessageConverters()`
2. 在 ResponseAdvisor 中排除 SpringDoc 相关路径

### 问题 2：/v3/api-docs 返回 base64 字符串

**可能原因：**
- Jackson 转换器配置被破坏
- OpenAPI 对象被序列化为 byte[]

**解决方法：**
- 检查 WebMvcConfig 配置
- 使用 `extendMessageConverters()`

### 问题 3：端口冲突 "Port 8082 was already in use"

**可能原因：**
- 之前的服务实例没有停止
- 多次启动导致端口占用

**解决方法：**
```bash
# 查找占用端口的进程
lsof -i:8082

# 停止旧进程
lsof -ti:8082 | xargs kill -9
```

---

## 附录：完整的修改文件列表

1. ✅ `duda-common/duda-common-web/src/main/java/com/duda/common/web/config/WebMvcConfig.java`
2. ✅ `duda-common/duda-common-web/src/main/java/com/duda/common/web/config/ResponseAdvisor.java`
3. ✅ `duda-usercenter/duda-user-api/src/main/java/com/duda/user/api/controller/UserController.java`
4. ✅ `/Volumes/DudaDate/DudaNexus/pom.xml`

---

**文档版本：** 1.0
**最后更新：** 2026-03-11
**作者：** DudaNexus 开发团队
**状态：** ✅ 已验证通过
