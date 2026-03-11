# MySQL 主从复制快速配置

## 主库配置 (192.168.1.10)

### 1. 修改配置文件

**文件:** `/etc/mysql/mysql.conf.d/mysqld.cnf`

```ini
[mysqld]
server-id = 1
log-bin = mysql-bin
binlog_format = ROW
binlog_do_db = duda_nexus
gtid-mode = ON
enforce-gtid-consistency = ON
```

### 2. 重启并创建用户

```bash
# 重启MySQL
sudo systemctl restart mysql

# 登录MySQL
mysql -u root -p
```

```sql
-- 创建复制用户
CREATE USER 'repl_user'@'192.168.1.11' IDENTIFIED WITH mysql_native_password BY 'Replica@2025';

-- 授予权限
GRANT REPLICATION SLAVE ON *.* TO 'repl_user'@'192.168.1.11';

FLUSH PRIVILEGES;

-- 查看主库状态（记下File和Position）
SHOW MASTER STATUS;
```

**输出示例:**
```
+------------------+----------+
| File             | Position |
+------------------+----------+
| mysql-bin.000001 |      154 |
+------------------+----------+
```

**记下这两个值！**
- File: `mysql-bin.000001`
- Position: `154`

---

## 从库配置 (192.168.1.11)

### 1. 修改配置文件

```ini
[mysqld]
server-id = 2
relay-log = mysql-relay-bin
read_only = 1
gtid-mode = ON
enforce-gtid-consistency = ON
```

### 2. 重启并配置主从

```bash
# 重启MySQL
sudo systemctl restart mysql

# 登录MySQL
mysql -u root -p
```

```sql
-- 停止从库线程
STOP SLAVE;

-- 配置主库信息（修改MASTER_LOG_FILE和MASTER_LOG_POS为主库的值）
CHANGE MASTER TO
  MASTER_HOST='192.168.1.10',
  MASTER_USER='repl_user',
  MASTER_PASSWORD='Replica@2025',
  MASTER_LOG_FILE='mysql-bin.000001',
  MASTER_LOG_POS=154,
  MASTER_PORT=3306;

-- 启动从库线程
START SLAVE;

-- 查看从库状态
SHOW SLAVE STATUS\G
```

### 3. 检查关键指标

```sql
-- 必须都是Yes
Slave_IO_Running: Yes
Slave_SQL_Running: Yes

-- 延迟应该为0或很小
Seconds_Behind_Master: 0
```

---

## 验证主从复制

### 在主库创建测试数据

```sql
USE duda_nexus;

INSERT INTO users (id, username, password, real_name, user_type, status)
VALUES (
  999999,
  'test_replication',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
  '复制测试',
  'NORMAL',
  'active'
);
```

### 在从库查询验证

```sql
USE duda_nexus;

SELECT id, username, real_name
FROM users
WHERE id = 999999;
```

**如果能查到数据，说明主从复制成功！**

---

## 故障处理

### 从库复制中断

```sql
-- 查看错误
SHOW SLAVE STATUS\G

-- 跳过一个错误（谨慎使用）
STOP SLAVE;
SET GLOBAL sql_slave_skip_counter = 1;
START SLAVE;
```

### 重置从库

```sql
-- 完全重置从库
STOP SLAVE;
RESET SLAVE ALL;

-- 重新配置
CHANGE MASTER TO ...
START SLAVE;
```

---

## 应用层配置

### 修改应用配置

**主库（写）:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.1.10:3306/duda_nexus
    username: duda_admin
    password: Duda@2025
```

**从库（读）:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.1.11:3306/duda_nexus
    username: duda_admin
    password: Duda@2025
```

---

**配置完成后，建议使用上面的检查脚本定期监控主从状态。**
