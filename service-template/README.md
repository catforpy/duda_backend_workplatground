# DudaNexus 服务模板

## 📋 模板说明

这是所有新服务的标准模板，包含了以下基础配置：

### ✅ 已包含的配置

1. **健康检查（Actuator）**
   - 端点：`/actuator/health`
   - 指标：`/actuator/metrics`
   - 信息：`/actuator/info`

2. **日志配置**
   - 统一日志格式
   - 日志级别管理

3. **Docker 支持**
   - 标准化 Dockerfile
   - 健康检查配置

## 🎯 使用方法

### 创建新服务

```bash
# 1. 复制模板
cp -r service-template your-new-service

# 2. 修改包名和类名
find your-new-service -type f -exec sed -i '' 's/com.duda.template/com.duda.yourpackage/g' {} +

# 3. 开始开发业务代码
```

## 📝 模板清单

- ✅ pom.xml（包含 Actuator 依赖）
- ✅ application.yml（健康检查配置）
- ✅ logback-spring.xml（日志配置）
- ✅ Dockerfile（容器化配置）

---

**创建时间**: 2026-03-13
**版本**: v1.0
