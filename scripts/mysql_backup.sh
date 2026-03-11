#!/bin/bash
# =============================================
# MySQL 自动备份脚本
# =============================================

# 配置变量
BACKUP_DIR="/data/mysql/backup"
MYSQL_USER="root"
MYSQL_PASSWORD="Duda@2025"
DATABASES="duda_nexus"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=7

# 创建备份目录
mkdir -p $BACKUP_DIR

echo "========================================="
echo "MySQL 备份开始"
echo "时间: $(date)"
echo "========================================="

# 备份函数
backup_database() {
    local db=$1
    local backup_file="$BACKUP_DIR/${db}_${DATE}.sql.gz"

    echo "开始备份数据库: $db"

    # 执行备份并压缩
    mysqldump -u$MYSQL_USER -p$MYSQL_PASSWORD \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        $db | gzip > $backup_file

    if [ $? -eq 0 ]; then
        echo "备份成功: $backup_file"
        # 显示文件大小
        local size=$(du -h $backup_file | cut -f1)
        echo "文件大小: $size"
    else
        echo "备份失败: $db"
        return 1
    fi
}

# 备份所有数据库
for db in $DATABASES; do
    backup_database $db
done

# 清理过期备份
echo ""
echo "清理 $RETENTION_DAYS 天前的备份..."
find $BACKUP_DIR -name "*.sql.gz" -mtime +$RETENTION_DAYS -exec rm {} \;

# 显示保留的备份
echo ""
echo "当前保留的备份:"
ls -lh $BACKUP_DIR/*.sql.gz 2>/dev/null | awk '{print $9, $5}'

# 记录日志
echo ""
echo "备份完成: $(date)" >> $BACKUP_DIR/backup.log

echo "========================================="
echo "备份脚本执行完成"
echo "========================================="
