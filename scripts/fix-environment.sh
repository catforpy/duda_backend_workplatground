#!/bin/bash

# DudaNexus 环境快速修复脚本
# 用法: ./scripts/fix-environment.sh

echo "╔══════════════════════════════════════════════════════════╗"
echo "║     DudaNexus 环境快速修复                                 ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# 配置
MYSQL_HOST="120.26.170.213"
MYSQL_PORT="3306"
MYSQL_USER="root"
MYSQL_PASS="duda2024"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🔧 开始修复环境..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 1. 创建数据库
echo "1️⃣  创建数据库..."
echo ""

mysql -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} << EOF
CREATE DATABASE IF NOT EXISTS duda_auth
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS duda_user
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS duda_search
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS duda_content
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS duda_order
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS duda_notification
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS duda_monitor
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- 显示所有数据库
SHOW DATABASES LIKE 'duda_%';
EOF

if [ $? -eq 0 ]; then
    echo "✅ 数据库创建成功"
else
    echo "❌ 数据库创建失败"
    echo "💡 请检查: "
    echo "   1. MySQL 密码是否正确"
    echo "   2. MySQL 服务是否运行"
    echo "   3. 网络连接是否正常"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2️⃣  检查 Redis 连接..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

REDIS_HOST="120.26.170.213"
REDIS_PORT="6379"
REDIS_PASS="duda2024"

if redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} -a "${REDIS_PASS}" ping > /dev/null 2>&1; then
    echo "✅ Redis 连接正常"

    # 显示 Redis 信息
    echo ""
    echo "📊 Redis 信息:"
    redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} -a "${REDIS_PASS}" info server | head -n 5
else
    echo "❌ Redis 连接失败"
    echo ""
    echo "💡 可能的原因:"
    echo "   1. Redis 密码已更改"
    echo "   2. Redis 服务未启动"
    echo "   3. 防火墙阻止了连接"
    echo ""
    echo "🔧 尝试解决:"
    echo ""
    echo "   测试无密码连接:"
    echo "   redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} ping"
    echo ""
    echo "   测试有密码连接:"
    echo "   redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} -a ${REDIS_PASS} ping"
    echo ""
    echo "   查看 Redis 日志:"
    echo "   docker logs duda-redis"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3️⃣  检查端口占用..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

check_port() {
    local port=$1
    local service=$2

    if lsof -i:${port} > /dev/null 2>&1; then
        echo "⚠️  端口 ${port} (${service}) 被占用:"
        lsof -i:${port} | grep -v COMMAND | awk '{print "   PID: " $2 ", 用户: " $3, ", 命令: " $1}'
        echo ""
        echo "   💡 如果需要释放端口，可以运行:"
        echo "   kill -9 \$(lsof -ti:${port})"
        echo ""
    else
        echo "✅ 端口 ${port} (${service}) 可用"
    fi
}

check_port "8080" "网关"
check_port "8081" "认证服务"
check_port "8082" "用户服务"
check_port "8083" "搜索服务"
check_port "8084" "内容服务"
check_port "8085" "订单服务"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "修复完成"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📝 下一步:"
echo "  1. 运行测试脚本验证修复: ./scripts/quick-test.sh"
echo "  2. 在 IDEA 中启动服务"
echo "  3. 在 Nacos 控制台查看服务注册情况"
echo ""
