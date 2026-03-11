#!/bin/bash

# DudaNexus 服务器连接测试脚本
# 用法: ./test-connection.sh

echo "╔══════════════════════════════════════════════════════════╗"
echo "║     DudaNexus 服务器连接测试                                 ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# 服务器配置
NACOS_HOST="120.26.170.213"
NACOS_PORT="8848"
NACOS_USER="nacos"
NACOS_PASS="nacos"
REDIS_HOST="120.26.170.213"
REDIS_PORT="6379"
REDIS_PASS="duda2024"
MYSQL_HOST="120.26.170.213"
MYSQL_PORT="3306"
MYSQL_USER="root"
MYSQL_PASS="duda2024"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "测试基础设施服务连接"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 1. 测试 Nacos
echo "1️⃣  测试 Nacos..."
if curl -s --connect-timeout 3 -u "${NACOS_USER}:${NACOS_PASS}" http://${NACOS_HOST}:${NACOS_PORT}/nacos/v1/console/health/readiness | grep -q "UP"; then
    echo "   ✅ Nacos 连接成功"
    echo "   📍 控制台: http://${NACOS_HOST}:${NACOS_PORT}/nacos"
    echo "   👤 账号: ${NACOS_USER} / ${NACOS_PASS}"
else
    echo "   ❌ Nacos 连接失败"
    echo "   💡 请检查认证配置: username=${NACOS_USER}, password=${NACOS_PASS}"
fi
echo ""

# 2. 测试 Redis
echo "2️⃣  测试 Redis..."
if redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} -a "${REDIS_PASS}" ping > /dev/null 2>&1; then
    echo "   ✅ Redis 连接成功"
    redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} -a "${REDIS_PASS}" info server | head -n 5
else
    echo "   ❌ Redis 连接失败"
    echo "   💡 请检查密码配置: password=${REDIS_PASS}"
fi
echo ""

# 3. 测试 MySQL
echo "3️⃣  测试 MySQL..."
if nc -zv ${MYSQL_HOST} ${MYSQL_PORT} 2>&1 | grep -q "succeeded"; then
    echo "   ✅ MySQL 端口可访问"
    echo "   📍 主机: ${MYSQL_HOST}"
    echo "   📍 端口: ${MYSQL_PORT}"
    echo "   👤 用户: ${MYSQL_USER}"
    echo ""
    echo "   连接命令:"
    echo "   mysql -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS}"
else
    echo "   ❌ MySQL 端口无法访问"
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "测试完成"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

echo "📊 服务器信息汇总:"
echo "  • Nacos:   http://${NACOS_HOST}:${NACOS_PORT}/nacos"
echo "  • Redis:   ${REDIS_HOST}:${REDIS_PORT}"
echo "  • MySQL:   ${MYSQL_HOST}:${MYSQL_PORT}"
echo ""

echo "💡 提示:"
echo "  • Nacos 控制台: http://${NACOS_HOST}:${NACOS_PORT}/nacos (账号: ${NACOS_USER}/${NACOS_PASS})"
echo "  • MySQL 连接: mysql -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS}"
echo "  • Redis 连接: redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} -a ${REDIS_PASS}"
echo "  • 详细配置请查看: docs/服务器基础设施配置-认证版.md"
echo ""
