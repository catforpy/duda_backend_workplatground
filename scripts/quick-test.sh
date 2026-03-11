#!/bin/bash

# DudaNexus 服务启动前快速检查脚本
# 用法: ./scripts/quick-test.sh

echo "╔══════════════════════════════════════════════════════════╗"
echo "║     DudaNexus 服务启动前检查                               ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# 配置
NACOS_HOST="120.26.170.213"
NACOS_PORT="8848"
NACOS_USER="nacos"
NACOS_PASS="nacos"
NAMESPACE="duda-dev"

MYSQL_HOST="120.26.170.213"
MYSQL_PORT="3306"
MYSQL_USER="root"
MYSQL_PASS="duda2024"

REDIS_HOST="120.26.170.213"
REDIS_PORT="6379"
REDIS_PASS="duda2024"

PASS_COUNT=0
FAIL_COUNT=0

# 测试函数
test_item() {
    local name=$1
    local command=$2

    echo -n "🔍 测试 $name... "

    if eval "$command" > /dev/null 2>&1; then
        echo "✅ 通过"
        ((PASS_COUNT++))
        return 0
    else
        echo "❌ 失败"
        ((FAIL_COUNT++))
        return 1
    fi
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1️⃣  基础设施检查"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 测试 Nacos
test_item "Nacos 服务" "curl -s --connect-timeout 3 http://${NACOS_HOST}:${NACOS_PORT}/nacos/"

# 测试 Nacos 认证
test_item "Nacos 认证" "curl -s --connect-timeout 3 -u ${NACOS_USER}:${NACOS_PASS} http://${NACOS_HOST}:${NACOS_PORT}/nacos/v1/console/health/readiness"

# 测试 Redis
test_item "Redis 连接" "redis-cli -h ${REDIS_HOST} -p ${REDIS_PORT} -a '${REDIS_PASS}' ping"

# 测试 MySQL
test_item "MySQL 连接" "nc -zv ${MYSQL_HOST} ${MYSQL_PORT} 2>&1 | grep -q succeeded"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2️⃣  Nacos 配置检查"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 检查 Nacos 配置
check_nacos_config() {
    local data_id=$1
    echo -n "🔍 检查配置: $data_id... "

    local result=$(curl -s -u "${NACOS_USER}:${NACOS_PASS}" \
        "http://${NACOS_HOST}:${NACOS_PORT}/nacos/v1/cs/configs?dataId=${data_id}&group=DEFAULT_GROUP&tenant=${NAMESPACE}")

    if [ "$result" != "" ] && [ "$result" != "config data not exist" ]; then
        echo "✅ 存在"
        ((PASS_COUNT++))
    else
        echo "❌ 不存在"
        ((FAIL_COUNT++))
    fi
}

check_nacos_config "common-dev.yml"
check_nacos_config "duda-auth-provider-dev.yml"
check_nacos_config "duda-user-provider-dev.yml"
check_nacos_config "duda-gateway-dev.yml"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3️⃣  数据库检查"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 检查数据库
check_database() {
    local db_name=$1
    echo -n "🔍 检查数据库: $db_name... "

    local result=$(mysql -h ${MYSQL_HOST} -P ${MYSQL_PORT} -u ${MYSQL_USER} -p${MYSQL_PASS} \
        -e "SHOW DATABASES LIKE '${db_name}';" -s 2>/dev/null | grep -c "${db_name}")

    if [ "$result" -gt 0 ]; then
        echo "✅ 存在"
        ((PASS_COUNT++))
    else
        echo "❌ 不存在"
        ((FAIL_COUNT++))
    fi
}

check_database "duda_auth"
check_database "duda_user"
check_database "duda_search"
check_database "duda_content"
check_database "duda_order"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4️⃣  本地端口检查"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# 检查端口占用
check_port() {
    local port=$1
    local service=$2

    echo -n "🔍 检查端口 ${port} (${service})... "

    if lsof -i:${port} > /dev/null 2>&1; then
        echo "⚠️  已占用"
        echo "   可能导致启动失败，请检查是否有其他服务在使用"
    else
        echo "✅ 可用"
        ((PASS_COUNT++))
    fi
}

check_port "8080" "网关"
check_port "8081" "认证服务"
check_port "8082" "用户服务"

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "检查结果汇总"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "✅ 通过: ${PASS_COUNT}"
echo "❌ 失败: ${FAIL_COUNT}"
echo ""

if [ ${FAIL_COUNT} -eq 0 ]; then
    echo "🎉 所有检查通过！可以启动服务了。"
    echo ""
    echo "📝 下一步:"
    echo "  1. 在 IDEA 中打开服务启动类"
    echo "  2. 添加环境变量（如果需要）:"
    echo "     SPRING_PROFILES_ACTIVE=dev"
    echo "     NACOS_USERNAME=nacos"
    echo "     NACOS_PASSWORD=nacos"
    echo "     NACOS_NAMESPACE=duda-dev"
    echo "  3. 点击 Run 按钮启动服务"
    echo ""
else
    echo "⚠️  发现 ${FAIL_COUNT} 个问题，请先解决后再启动服务。"
    echo ""
    echo "💡 常见问题解决:"
    echo "  • Nacos 配置不存在 → 请参考 docs/Nacos配置创建指南.md"
    echo "  • 数据库不存在 → 请运行: mysql -h ${MYSQL_HOST} -u ${MYSQL_USER} -p${MYSQL_PASS} -e 'CREATE DATABASE ...'"
    echo "  • 端口被占用 → 请关闭占用端口的进程"
    echo ""
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
