# 🚀 Nacos 配置快速创建索引

## 📂 所有配置文件位置

所有配置内容都已经准备好了，直接复制即可！

---

## 🎯 创建步骤

### 第一步：打开 Nacos

```
http://120.26.170.213:8848/nacos
账号: nacos
密码: nacos
```

### 第二步：创建命名空间

- 点击「命名空间」
- 新建命名空间：
  ```
  命名空间 ID: duda-dev
  名称: 开发环境
  ```

### 第三步：切换到 duda-dev 命名空间

- 在顶部下拉框选择「duda-dev」

### 第四步：创建配置（按顺序）

---

## 📝 配置清单

### 配置 1️⃣：common-dev.yml

| 字段 | 值 |
|------|-----|
| Data ID | common-dev.yml |
| Group | **COMMON_GROUP** |
| 配置格式 | YAML |

**📄 配置文件位置：**
```
docs/nacos-configs/common-dev-COMMON_GROUP.yml
```

**📋 复制方式：**
1. 打开上面的文件
2. 复制全部内容
3. 粘贴到 Nacos 配置编辑器
4. 点击「发布」

---

### 配置 2️⃣：duda-auth-provider-dev.yml

| 字段 | 值 |
|------|-----|
| Data ID | duda-auth-provider-dev.yml |
| Group | **AUTH_GROUP** |
| 配置格式 | YAML |

**📄 配置文件位置：**
```
docs/nacos-configs/duda-auth-provider-dev-AUTH_GROUP.yml
```

---

### 配置 3️⃣：duda-user-provider-dev.yml

| 字段 | 值 |
|------|-----|
| Data ID | duda-user-provider-dev.yml |
| Group | **USER_GROUP** |
| 配置格式 | YAML |

**📄 配置文件位置：**
```
docs/nacos-configs/duda-user-provider-dev-USER_GROUP.yml
```

---

### 配置 4️⃣：duda-gateway-dev.yml

| 字段 | 值 |
|------|-----|
| Data ID | duda-gateway-dev.yml |
| Group | **GATEWAY_GROUP** |
| 配置格式 | YAML |

**📄 配置文件位置：**
```
docs/nacos-configs/duda-gateway-dev-GATEWAY_GROUP.yml
```

---

### 配置 5️⃣：duda-search-provider-dev.yml（可选）

| 字段 | 值 |
|------|-----|
| Data ID | duda-search-provider-dev.yml |
| Group | **SEARCH_GROUP** |
| 配置格式 | YAML |

**📄 配置文件位置：**
```
docs/nacos-configs/duda-search-provider-dev-SEARCH_GROUP.yml
```

---

### 配置 6️⃣：duda-content-provider-dev.yml（可选）

| 字段 | 值 |
|------|-----|
| Data ID | duda-content-provider-dev.yml |
| Group | **CONTENT_GROUP** |
| 配置格式 | YAML |

**📄 配置文件位置：**
```
docs/nacos-configs/duda-content-provider-dev-CONTENT_GROUP.yml
```

---

### 配置 7️⃣：duda-order-provider-dev.yml（可选）

| 字段 | 值 |
|------|-----|
| Data ID | duda-order-provider-dev.yml |
| Group | **ORDER_GROUP** |
| 配置格式 | YAML |

**📄 配置文件位置：**
```
docs/nacos-configs/duda-order-provider-dev-ORDER_GROUP.yml
```

---

## 🎯 快速操作指南

### 方法 A：使用命令行查看配置（推荐）

```bash
cd /Volumes/DudaDate/DudaNexus

# 查看所有配置文件
ls -la docs/nacos-configs/

# 查看具体配置内容
cat docs/nacos-configs/common-dev-COMMON_GROUP.yml
cat docs/nacos-configs/duda-auth-provider-dev-AUTH_GROUP.yml
# ... 其他配置
```

### 方法 B：使用 IDE 打开

1. 在 IDEA/VSCode 中打开项目
2. 导航到 `docs/nacos-configs/` 目录
3. 双击打开对应的 `.yml` 文件
4. 复制内容到 Nacos

---

## ✅ 完成检查

创建完配置后，在 Nacos 控制台的 `duda-dev` 命名空间下应该看到：

```
配置列表：
├── common-dev.yml                    [COMMON_GROUP]
├── duda-auth-provider-dev.yml        [AUTH_GROUP]
├── duda-user-provider-dev.yml        [USER_GROUP]
├── duda-gateway-dev.yml              [GATEWAY_GROUP]
├── duda-search-provider-dev.yml      [SEARCH_GROUP] (可选)
├── duda-content-provider-dev.yml     [CONTENT_GROUP] (可选)
└── duda-order-provider-dev.yml       [ORDER_GROUP] (可选)
```

---

## 🚀 下一步

### 1. 创建数据库

```bash
mysql -h 120.26.170.213 -P 3306 -u root -pduda2024
```

执行：
```sql
CREATE DATABASE IF NOT EXISTS duda_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS duda_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS duda_search DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS duda_content DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS duda_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 启动服务

在 IDEA 中启动 `duda-auth-provider`，观察日志

### 3. 验证

```bash
curl http://localhost:8081/actuator/health
```

---

## 📚 相关文档

- `docs/Nacos配置操作指南-分步创建.md` - 详细的图文步骤
- `docs/服务测试指南.md` - 如何测试服务
- `docs/Nacos命名空间设计建议.md` - 为什么要这样设计

---

**所有配置文件都在这里，直接复制粘贴即可！** 🎉
