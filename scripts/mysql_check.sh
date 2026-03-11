#!/bin/bash
# =============================================
# MySQL 主从复制状态检查脚本
# =============================================

MYSQL_USER="root"
MYSQL_PASSWORD="Duda@2025"
MASTER_HOST="192.168.1.10"
SLAVE_HOST="192.168.1.11"

echo "========================================="
echo "MySQL 主从复制状态检查"
echo "时间: $(date)"
echo "========================================="

# 检查主库状态
echo ""
echo "【主库状态】 $MASTER_HOST"
echo "========================================="
mysql -h$MASTER_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD -e "
    SHOW MASTER STATUS;
" 2>/dev/null

# 检查从库状态
echo ""
echo "【从库状态】 $SLAVE_HOST"
echo "========================================="
mysql -h$SLAVE_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD -e "
    SHOW SLAVE STATUS\G
" 2>/dev/null | grep -E "Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master|Last_Error"

# 检查延迟
echo ""
echo "【复制延迟检查】"
echo "========================================="
DELAY=$(mysql -h$SLAVE_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD -e "
    SHOW SLAVE STATUS;
" 2>/dev/null | grep "Seconds_Behind_Master" | awk '{print $2}')

if [ "$DELAY" = "NULL" ]; then
    echo "❌ 从库未连接或复制已停止"
elif [ "$DELAY" -eq 0 ]; then
    echo "✅ 主从同步正常，无延迟"
else
    echo "⚠️  主从延迟: $DELAY 秒"
fi

# 检查主库binlog
echo ""
echo "【主库Binlog文件】"
echo "========================================="
mysql -h$MASTER_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD -e "
    SHOW BINARY LOGS;
" 2>/dev/null | tail -5

echo ""
echo "========================================="
echo "检查完成"
echo "========================================="
