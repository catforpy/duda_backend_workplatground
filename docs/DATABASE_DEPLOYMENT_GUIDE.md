# 数据库部署和主从配置指南

## 📋 目录
1. [快速开始](#快速开始)
2. [单机部署](#单机部署)
3. [主从复制部署](#主从复制部署)
4. [备份恢复](#备份恢复)
5. [监控维护](#监控维护)

---

## 快速开始

### 前置准备

**服务器要求:**
- MySQL 8.0+
- 内存: 最低2GB，推荐4GB+
- 磁盘: 最低20GB，推荐50GB+
- 网络: 开放3306端口

### 一键初始化（单机）

```bash
# 1. 登录服务器
ssh root@your_server_ip

# 2. 创建数据库目录
mkdir -p /data/mysql/{data,logs,binlog}

# 3. 上传SQL文件到服务器
scp sql/init-schema.sql root@your_server_ip:/data/mysql/
scp sql/init-data.sql root@your_server_ip:/data/mysql/

# 4. 登录MySQL并执行
mysql -u root -p < /data/mysql/init-schema.sql
mysql -u root -p < /data/mysql/init-data.sql

# 5. 验证
mysql -u root -p -e "USE duda_nexus; SHOW TABLES;"
```

### 验证安装

```sql
-- 查看数据库
SHOW DATABASES;

-- 查看表
USE duda_nexus;
SHOW TABLES;

-- 查看管理员账号
SELECT id, username, real_name, user_type, status
FROM users
WHERE username = 'admin';

-- 查看表记录数
SELECT
  'users' AS table_name,
  COUNT(*) AS count
FROM users
UNION ALL
SELECT 'roles', COUNT(*) FROM roles
UNION ALL
SELECT 'permissions', COUNT(*) FROM permissions;
```

---

## 单机部署

### 方式一：Docker 部署（推荐开发环境）

#### 1. 创建 docker-compose-mysql.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: duda-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: Duda@2025
      MYSQL_DATABASE: duda_nexus
      TZ: Asia/Shanghai
    volumes:
      - mysql-data:/var/lib/mysql
      - ./sql:/docker-entrypoint-initdb.d
      - ./mysql/conf.d:/etc/mysql/conf.d
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-authentication-plugin=mysql_native_password
      - --max_connections=1000
      - --default-time-zone='+08:00'
    networks:
      - duda-network
    restart: unless-stopped

volumes:
  mysql-data:
    driver: local

networks:
  duda-network:
    driver: bridge
```

#### 2. 启动 MySQL

```bash
cd /Volumes/DudaDate/DudaNexus
docker-compose -f docker-compose-mysql.yml up -d

# 查看日志
docker-compose -f docker-compose-mysql.yml logs -f mysql

# 进入容器
docker exec -it duda-mysql mysql -u root -p
```

### 方式二：本地安装 MySQL

#### 1. 下载并安装 MySQL

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql
```

**CentOS/RHEL:**
```bash
sudo yum install mysql-server -y
sudo systemctl start mysqld
sudo systemctl enable mysqld
```

**macOS:**
```bash
brew install mysql
brew services start mysql
```

#### 2. 创建数据库

```bash
# 登录MySQL
mysql -u root -p

# 或使用Docker中的MySQL
docker exec -it duda-mysql mysql -u root -pDuda@2025
```

```sql
-- 执行初始化脚本
source /data/mysql/init-schema.sql;
source /data/mysql/init-data.sql;

-- 或直接执行
\. /data/mysql/init-schema.sql
\. /data/mysql/init-data.sql
```

#### 3. 配置远程访问

```sql
-- 创建远程用户
CREATE USER 'duda_admin'@'%' IDENTIFIED BY 'Duda@2025';

-- 授予权限
GRANT ALL PRIVILEGES ON duda_nexus.* TO 'duda_admin'@'%';

-- 刷新权限
FLUSH PRIVILEGES;

-- 验证用户
SELECT user, host FROM mysql.user;
```

#### 4. 修改应用配置

**文件:** `duda-user-provider/src/main/resources/bootstrap.yml`

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://your_server_ip:3306/duda_nexus?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: duda_admin
    password: Duda@2025
```

---

## 主从复制部署

### 架构设计

```
┌─────────────────────────────────────────────────────────┐
│                    应用服务器                           │
│  (duda-user-provider, duda-user-api)                    │
└─────────────┬───────────────────────┬───────────────────┘
              │                       │
         写操作                    读操作
              │                       │
    ┌─────────▼─────────┐   ┌────────▼────────┐
    │  Master (主库)    │──▶│  Slave (从库)   │
    │  192.168.1.10    │   │  192.168.1.11   │
    │  Port: 3306      │   │  Port: 3306     │
    └──────────────────┘   └─────────────────┘
              │                       │
         Binlog                  Relay Log
```

### 主库配置 (Master)

#### 1. 修改主库配置文件

**文件:** `/etc/mysql/mysql.conf.d/mysqld.cnf` (Ubuntu)
或 `/etc/my.cnf` (CentOS)

```ini
[mysqld]
# 服务器ID，唯一标识
server-id = 1

# 开启二进制日志
log-bin = mysql-bin
binlog_format = ROW
binlog_row_image = FULL

# 二进制日志过期时间（天）
expire_logs_days = 7

# 二进制日志大小
max_binlog_size = 100M

# 需要同步的数据库（可选，不设置则同步所有）
binlog-do-db = duda_nexus

# 不同步的数据库（可选）
binlog-ignore-db = mysql
binlog-ignore-db = information_schema
binlog-ignore-db = performance_schema
binlog-ignore-db = sys

# GTID模式（推荐）
gtid-mode = ON
enforce-gtid-consistency = ON
```

#### 2. 重启主库

```bash
sudo systemctl restart mysql
# 或
sudo service mysql restart
```

#### 3. 创建复制用户

```sql
-- 登录主库
mysql -u root -p -h 192.168.1.10

-- 创建复制用户
CREATE USER 'repl_user'@'192.168.1.11' IDENTIFIED WITH mysql_native_password BY 'Replica@2025';

-- 授予复制权限
GRANT REPLICATION SLAVE ON *.* TO 'repl_user'@'192.168.1.11';

-- 刷新权限
FLUSH PRIVILEGES;

-- 查看主库状态
SHOW MASTER STATUS;
```

**输出示例:**
```
+------------------+----------+--------------+------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB |
+------------------+----------+--------------+------------------+
| mysql-bin.000001 |      154 | duda_nexus   | mysql,sys        |
+------------------+----------+--------------+------------------+
```

**记录这两个值:**
- `File`: mysql-bin.000001
- `Position`: 154

### 从库配置 (Slave)

#### 1. 修改从库配置文件

```ini
[mysqld]
# 服务器ID，必须与主库不同
server-id = 2

# 开启中继日志
relay-log = mysql-relay-bin
relay_log_recovery = ON

# 只读模式（推荐）
read_only = 1
super_read_only = 1

# GTID模式
gtid-mode = ON
enforce-gtid-consistency = ON

# 从库复制线程不记录自己的binlog（可选）
log_slave_updates = ON
```

#### 2. 重启从库

```bash
sudo systemctl restart mysql
```

#### 3. 配置主从复制

```sql
-- 登录从库
mysql -u root -p -h 192.168.1.11

-- 停止从库线程（如果之前配置过）
STOP SLAVE;

-- 配置主库信息
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

#### 4. 检查从库状态

```sql
-- 查看从库状态
SHOW SLAVE STATUS\G

-- 关键指标:
-- Slave_IO_Running: Yes
-- Slave_SQL_Running: Yes
-- Seconds_Behind_Master: 0 (或很小的数字)
```

**如果都正常，说明主从复制配置成功！**

### 应用层读写分离

#### 1. 配置多数据源

**主库配置:**
```yaml
spring:
  datasource:
    master:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://192.168.1.10:3306/duda_nexus?useUnicode=true&characterEncoding=utf8
      username: duda_admin
      password: Duda@2025
```

**从库配置:**
```yaml
spring:
  datasource:
    slave:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://192.168.1.11:3306/duda_nexus?useUnicode=true&characterEncoding=utf8
      username: duda_admin
      password: Duda@2025
```

#### 2. 使用读写分离中间件

**推荐方案:**
- **ShardingSphere** - Apache开源，功能强大
- **MyCat** - 国内使用广泛
- **ProxySQL** - 高性能
- **Vitess** - YouTube开源

**ShardingSphere配置示例:**
```yaml
dataSources:
  master:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://192.168.1.10:3306/duda_nexus
    username: duda_admin
    password: Duda@2025

  slave:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://192.168.1.11:3306/duda_nexus
    username: duda_admin
    password: Duda@2025

rules:
  - !SHARDING
    tables:
      users:
        actualDataNodes: master.users,slave.users
    # 读写分离规则
    masterSlaveRule:
      name: ms_master_slave
      masterDataSourceName: master
      slaveDataSourceNames:
        - slave
      loadBalanceAlgorithmType: ROUND_ROBIN
```

---

## 备份恢复

### 逻辑备份 (mysqldump)

#### 1. 全库备份

```bash
# 备份所有数据库
mysqldump -u root -p --all-databases > /data/backup/all_$(date +%Y%m%d).sql

# 只备份结构
mysqldump -u root -p --no-data duda_nexus > /data/backup/schema_$(date +%Y%m%d).sql

# 只备份数据
mysqldump -u root -p --no-create-info duda_nexus > /data/backup/data_$(date +%Y%m%d).sql
```

#### 2. 单库备份

```bash
# 备份duda_nexus数据库
mysqldump -u root -p duda_nexus > /data/backup/duda_nexus_$(date +%Y%m%d).sql

# 压缩备份
mysqldump -u root -p duda_nexus | gzip > /data/backup/duda_nexus_$(date +%Y%m%d).sql.gz
```

#### 3. 自动备份脚本

```bash
#!/bin/bash
# 文件: /data/scripts/mysql_backup.sh

BACKUP_DIR="/data/backup"
MYSQL_USER="root"
MYSQL_PASSWORD="Duda@2025"
DATABASE="duda_nexus"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份
mysqldump -u$MYSQL_USER -p$MYSQL_PASSWORD $DATABASE | gzip > $BACKUP_DIR/${DATABASE}_${DATE}.sql.gz

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete

# 记录日志
echo "Backup completed: ${DATABASE}_${DATE}.sql.gz" >> $BACKUP_DIR/backup.log
```

**配置定时任务:**
```bash
# 添加到crontab
crontab -e

# 每天凌晨2点执行备份
0 2 * * * /data/scripts/mysql_backup.sh
```

### 物理备份 (MySQL Enterprise Backup / Percona XtraBackup)

#### 使用 Percona XtraBackup

**安装:**
```bash
# Ubuntu/Debian
sudo apt install percona-xtrabackup-64 -y

# CentOS/RHEL
sudo yum install percona-xtrabackup-64 -y
```

**全量备份:**
```bash
# 备份
xtrabackup --backup --target-dir=/data/backup/full --user=root --password=Duda@2025

# 恢复
xtrabackup --prepare --target-dir=/data/backup/full
xtrabackup --copy-back --target-dir=/data/backup/full
```

### 数据恢复

#### 从逻辑备份恢复

```bash
# 解压（如果是压缩文件）
gunzip duda_nexus_20260311.sql.gz

# 恢复
mysql -u root -p duda_nexus < duda_nexus_20260311.sql
```

#### 从主库恢复从库

**如果从库损坏，可以重置从库:**

```sql
-- 在从库执行
STOP SLAVE;

-- 重置从库
RESET SLAVE ALL;

-- 重新配置主库信息
CHANGE MASTER TO
  MASTER_HOST='192.168.1.10',
  MASTER_USER='repl_user',
  MASTER_PASSWORD='Replica@2025',
  MASTER_LOG_FILE='mysql-bin.000001',
  MASTER_LOG_POS=154;

-- 启动从库
START SLAVE;
```

---

## 监控维护

### 性能监控

#### 1. 查看连接数

```sql
-- 当前连接数
SHOW PROCESSLIST;

-- 连接数统计
SHOW STATUS LIKE 'Threads_connected';
SHOW STATUS LIKE 'Max_used_connections';

-- 查看最大连接数
SHOW VARIABLES LIKE 'max_connections';
```

#### 2. 查看慢查询

```sql
-- 查看慢查询配置
SHOW VARIABLES LIKE 'slow_query%';
SHOW VARIABLES LIKE 'long_query_time';

-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;  -- 超过2秒的查询记录

-- 查看慢查询
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 10;
```

#### 3. 查看主从状态

**主库:**
```sql
-- 查看binlog文件
SHOW BINARY LOGS;

-- 查看当前binlog位置
SHOW MASTER STATUS;
```

**从库:**
```sql
-- 查看从库状态
SHOW SLAVE STATUS\G

-- 关键指标
SELECT
  Slave_IO_Running,
  Slave_SQL_Running,
  Seconds_Behind_Master
FROM information_schema.PROCESSLIST
WHERE User = 'system user';
```

### 日常维护

#### 1. 清理binlog日志

```sql
-- 查看binlog日志
SHOW BINARY LOGS;

-- 清理7天前的binlog
PURGE BINARY LOGS BEFORE DATE_SUB(NOW(), INTERVAL 7 DAY);

-- 或手动删除
PURGE BINARY LOGS TO 'mysql-bin.000010';
```

#### 2. 优化表

```sql
-- 优化表（整理碎片）
OPTIMIZE TABLE users;
OPTIMIZE TABLE companies;

-- 分析表（更新统计信息）
ANALYZE TABLE users;
```

#### 3. 检查表

```sql
-- 检查表完整性
CHECK TABLE users;
CHECK TABLE companies;

-- 修复表
REPAIR TABLE users;
```

### 安全加固

#### 1. 修改root密码

```sql
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'NewStrong@Password2025';
FLUSH PRIVILEGES;
```

#### 2. 删除空密码用户

```sql
SELECT user, host, authentication_string FROM mysql.user WHERE user = '';
DROP USER ''@'localhost';
```

#### 3. 限制远程访问

```sql
-- 只允许特定IP访问
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'duda_admin'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON duda_nexus.* TO 'duda_admin'@'192.168.1.%';
FLUSH PRIVILEGES;
```

---

## 故障排查

### 常见问题

#### 1. 主从同步延迟

**原因:** 从库执行速度跟不上主库写入速度

**解决:**
```sql
-- 查看延迟时间
SHOW SLAVE STATUS\G

-- 调整从库参数
SET GLOBAL slave_parallel_workers = 4;
SET GLOBAL slave_parallel_type = 'LOGICAL_CLOCK';
```

#### 2. 主从复制中断

**原因:** 从库执行出错，复制线程停止

**解决:**
```sql
-- 查看错误信息
SHOW SLAVE STATUS\G

-- 跳过一个错误（谨慎使用）
STOP SLAVE;
SET GLOBAL sql_slave_skip_counter = 1;
START SLAVE;
```

#### 3. 连接数过多

**错误:** `Too many connections`

**解决:**
```sql
-- 查看当前连接
SHOW PROCESSLIST;

-- 杀掉长时间查询的连接
KILL 12345;

-- 临时增加最大连接数
SET GLOBAL max_connections = 500;

-- 永久修改（修改my.cnf）
[mysqld]
max_connections = 500
```

#### 4. 磁盘空间不足

```bash
# 查看磁盘使用
df -h

# 清理binlog
mysql -u root -p -e "PURGE BINARY LOGS BEFORE DATE_SUB(NOW(), INTERVAL 3 DAY);"

# 清理慢查询日志
echo > /var/log/mysql/slow-query.log
```

---

## 快速参考

### 连接MySQL

```bash
# 本地连接
mysql -u root -p

# 远程连接
mysql -h 192.168.1.10 -u duda_admin -p

# Docker容器连接
docker exec -it duda-mysql mysql -u root -p
```

### 常用命令

```sql
-- 查看数据库
SHOW DATABASES;

-- 切换数据库
USE duda_nexus;

-- 查看表
SHOW TABLES;

-- 查看表结构
DESC users;

-- 查看表创建语句
SHOW CREATE TABLE users;

-- 查看索引
SHOW INDEX FROM users;

-- 查看表状态
SHOW TABLE STATUS LIKE 'users';
```

### 导入导出

```bash
# 导出
mysqldump -u root -p duda_nexus > backup.sql

# 导入
mysql -u root -p duda_nexus < backup.sql

# 压缩导出
mysqldump -u root -p duda_nexus | gzip > backup.sql.gz

# 解压导入
gunzip < backup.sql.gz | mysql -u root -p duda_nexus
```

---

## 总结

✅ **已完成:**
- 数据库脚本完整
- 支持单机部署
- 支持主从复制
- 提供备份恢复方案

🔧 **服务器部署步骤:**
1. 上传SQL文件到服务器
2. 执行 `init-schema.sql` 创建表结构
3. 执行 `init-data.sql` 初始化数据
4. 配置应用连接数据库
5. 启动应用服务

📖 **相关文档:**
- [数据库表结构](../sql/init-schema.sql)
- [初始化数据](../sql/init-data.sql)
- [Docker部署](../DOCKER_DEPLOYMENT_MANUAL.md)

---

**文档版本:** 1.0
**更新时间:** 2026-03-11
**作者:** DudaNexus Team
