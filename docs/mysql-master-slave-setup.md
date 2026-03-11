# MySQL 主从复制配置指南

## 一、架构说明

```
┌─────────────────────────────────────────────────────────┐
│                    应用层 (Spring Boot)                  │
│  - Master: 写操作 (orders, payments, user updates)      │
│  - Slave:  读操作 (queries, statistics, reports)       │
└─────────────────────────────────────────────────────────┘
                          ↓
          ┌───────────────┴───────────────┐
          ↓                               ↓
    ┌──────────────┐              ┌──────────────┐
    │   Master     │              │    Slave     │
    │   (120.26.   │──────────────▶│   (备用IP)   │
    │   170.213)  │  Binlog复制   │              │
    │   Port:3306  │              │  Port:3307   │
    └──────────────┘              └──────────────┘
```

## 二、Master 配置（主库）

### 2.1 编辑 my.cnf

```bash
# 编辑主库配置文件
sudo vi /etc/my.cnf
```

添加以下配置：

```ini
[mysqld]
# 服务器ID（主库必须为1）
server-id=1

# 启用二进制日志
log-bin=mysql-bin
binlog_format=ROW

# 需要复制的数据库（可选，默认复制所有）
binlog-do-db=duda_user
binlog-do-db=duda_auth
binlog-do-db=duda_order
binlog-do-db=duda_content

# 不需要复制的数据库
binlog-ignore-db=mysql
binlog-ignore-db=information_schema
binlog-ignore-db=performance_schema

# 二进制日志过期时间（7天）
expire_logs_days=7

# 二进制日志大小
max_binlog_size=100M
```

### 2.2 重启 MySQL

```bash
# CentOS/RHEL
sudo systemctl restart mysqld

# Ubuntu/Debian
sudo systemctl restart mysql
```

### 2.3 创建复制用户

```sql
-- 登录MySQL
mysql -uroot -p;

-- 创建复制用户
CREATE USER 'replication'@'%' IDENTIFIED WITH mysql_native_password BY 'Replica@2024';

-- 授予复制权限
GRANT REPLICATION SLAVE ON *.* TO 'replication'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 查看主库状态
SHOW MASTER STATUS;

-- 记录 File 和 Position，配置从库时需要
-- 示例输出：
-- +------------------+----------+--------------+------------------+
-- | File             | Position | Binlog_Do_DB | Binlog_Ignore_DB |
-- +------------------+----------+--------------+------------------+
-- | mysql-bin.000001 |      154 | duda_user    | mysql            |
-- +------------------+----------+--------------+------------------+
```

## 三、Slave 配置（从库）

### 3.1 编辑 my.cnf

```bash
# 编辑从库配置文件
sudo vi /etc/my.cnf
```

添加以下配置：

```ini
[mysqld]
# 服务器ID（从库必须唯一且不能与主库相同）
server-id=2

# 中继日志
relay-log=mysql-relay-bin
relay-log-index=mysql-relay-bin.index

# 只读模式（可选，推荐）
read-only=1

# 超级用户可写（可选）
super-read-only=0

# 不需要复制的数据库
replicate-ignore-db=mysql
replicate-ignore-db=information_schema
replicate-ignore-db=performance_schema
replicate-ignore-db=sys

# 并行复制（可选，提升性能）
slave_parallel_type=LOGICAL_CLOCK
slave_parallel_workers=4
```

### 3.2 重启 MySQL

```bash
sudo systemctl restart mysqld
```

### 3.3 配置复制

```sql
-- 登录从库MySQL
mysql -uroot -p;

-- 停止从库（如果之前配置过）
STOP SLAVE;

-- 配置主库信息
CHANGE MASTER TO
  MASTER_HOST='120.26.170.213',
  MASTER_USER='replication',
  MASTER_PASSWORD='Replica@2024',
  MASTER_LOG_FILE='mysql-bin.000001',  -- 从SHOW MASTER STATUS获取
  MASTER_LOG_POS=154,                   -- 从SHOW MASTER STATUS获取
  MASTER_PORT=3306;

-- 启动从库
START SLAVE;

-- 查看从库状态
SHOW SLAVE STATUS\G

-- 检查以下两项是否为YES：
-- Slave_IO_Running: Yes
-- Slave_SQL_Running: Yes
-- Seconds_Behind_Master: 0 (延迟时间，0表示无延迟)
```

## 四、Spring Boot 配置

### 4.1 pom.xml 添加依赖

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.5</version>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-3-starter</artifactId>
    <version>1.2.20</version>
</dependency>
```

### 4.2 application.yml 配置

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    # 主库（写）
    url: jdbc:mysql://120.26.170.213:3306/duda_user?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: duda2024
    # Druid连接池配置
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20

# MyBatis-Plus配置
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.duda.user.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: ASSIGN_ID
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

## 五、测试主从复制

### 5.1 在主库创建测试数据

```sql
-- 在主库执行
USE duda_user;

CREATE TABLE test_replication (
  id INT PRIMARY KEY AUTO_INCREMENT,
  message VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO test_replication (message) VALUES ('测试主从复制');
```

### 5.2 在从库检查

```sql
-- 在从库执行
USE duda_user;

SELECT * FROM test_replication;

-- 应该能看到主库插入的数据
```

### 5.3 查看从库状态

```sql
-- 在从库执行
SHOW SLAVE STATUS\G

-- 关键指标：
-- Slave_IO_Running: Yes
-- Slave_SQL_Running: Yes
-- Seconds_Behind_Master: 延迟秒数（0表示实时同步）
```

## 六、常用维护命令

### 6.1 查看主库状态

```sql
-- 查看二进制日志文件
SHOW BINARY LOGS;

-- 查看当前主库状态
SHOW MASTER STATUS;

-- 查看二进制日志事件
SHOW BINLOG EVENTS IN 'mysql-bin.000001' LIMIT 10;
```

### 6.2 查看从库状态

```sql
-- 查看从库状态
SHOW SLAVE STATUS\G

-- 只看关键信息
SHOW SLAVE STATUS\G
WHERE Slave_IO_Running = 'Yes'
AND Slave_SQL_Running = 'Yes';
```

### 6.3 停止和启动复制

```sql
-- 停止从库复制
STOP SLAVE;

-- 启动从库复制
START SLAVE;

-- 重置从库（慎用）
RESET SLAVE;
```

### 6.4 主从切换（紧急情况）

**主库故障时，将从库提升为主库：**

```sql
-- 1. 停止从库复制
STOP SLAVE;

-- 2. 重置从库状态
RESET SLAVE ALL;

-- 3. 关闭只读模式
SET GLOBAL read_only = OFF;

-- 4. 现在从库成为新的主库
```

## 七、监控建议

### 7.1 监控指标

1. **复制延迟**
   ```sql
   SHOW SLAVE STATUS\G
   -- 关注 Seconds_Behind_Master（建议 < 5秒）
   ```

2. **主库二进制日志**
   ```sql
   SHOW BINARY LOGS;
   -- 关注文件大小和数量
   ```

3. **网络连接**
   ```bash
   # 检查主从连接
   netstat -an | grep 3306
   ```

### 7.2 告警规则

- 延迟超过10秒 → 发送告警
- 复制中断 → 紧急告警
- 磁盘空间不足80% → 预警告警

## 八、备份策略

### 8.1 主库备份

```bash
# 每天凌晨2点全量备份
0 2 * * * /usr/bin/mysqldump -uroot -p'password' --all-databases --single-transaction --quick --lock-tables=false | gzip > /backup/mysql_$(date +\%Y\%m\%d).sql.gz
```

### 8.2 从库备份

```bash
# 从库可用于备份，不影响主库性能
mysqldump -uroot -p'password' --all-databases --single-transaction > /backup/slave_full_$(date +%Y%m%d).sql
```

---

**配置完成后，请测试：**
1. ✅ 主库写入数据
2. ✅ 从库实时同步
3. ✅ 应用读写分离
4. ✅ 主从切换演练
