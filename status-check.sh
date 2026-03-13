#!/bin/bash

# DudaNexus 状态检查脚本

PROJECT_DIR="/Volumes/DudaDate/DudaNexus"
cd "$PROJECT_DIR"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "========================================="
echo "📊 DudaNexus 服务状态检查"
echo "========================================="
echo ""

# Docker 状态
echo -e "${YELLOW}🐳 Docker 状态:${NC}"
if docker info > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Docker 运行正常${NC}"
else
    echo -e "${RED}❌ Docker 未运行${NC}"
    exit 1
fi
echo ""

# 容器状态
echo -e "${YELLOW}📦 容器状态:${NC}"
docker-compose ps
echo ""

# 端口检查
echo -e "${YELLOW}🔌 端口监听:${NC}"
PORTS=(
    "9090:duda-id-generator"
    "9091:duda-msg-provider"
    "8082:duda-user-provider"
    "8083:duda-user-api"
)

for port_info in "${PORTS[@]}"; do
    IFS=':' read -ra PARTS <<< "$port_info"
    PORT=${PARTS[0]}
    NAME=${PARTS[1]}

    if lsof -i :$PORT > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $NAME - 端口 $PORT${NC}"
    else
        echo -e "${RED}❌ $NAME - 端口 $PORT 未监听${NC}"
    fi
done
echo ""

# 健康检查
echo -e "${YELLOW}🏥 健康检查:${NC}"

check_health() {
    local url=$1
    local name=$2

    if curl -s "$url" > /dev/null 2>&1; then
        STATUS=$(curl -s "$url" | python3 -c "import sys,json; print(json.load(sys.stdin).get('status', 'UNKNOWN'))" 2>/dev/null || echo "UNKNOWN")
        if [ "$STATUS" = "UP" ]; then
            echo -e "${GREEN}✅ $name - 健康${NC}"
        else
            echo -e "${YELLOW}⚠️  $name - $STATUS${NC}"
        fi
    else
        echo -e "${RED}❌ $name - 无法访问${NC}"
    fi
}

check_health "http://localhost:9090/actuator/health" "ID生成器"
check_health "http://localhost:9091/actuator/health" "消息服务"
check_health "http://localhost:8082/actuator/health" "用户Provider"
check_health "http://localhost:8083/actuator/health" "用户API"
echo ""

# Swagger 接口统计
echo -e "${YELLOW}📚 Swagger 接口统计:${NC}"
API_COUNT=$(curl -s http://localhost:8083/v3/api-docs 2>/dev/null | python3 -c "import sys,json; print(len(json.load(sys.stdin).get('paths',{})))" 2>/dev/null || echo "0")
echo -e "${GREEN}✅ 用户API - $API_COUNT 个接口${NC}"
echo ""

# Nacos 服务注册
echo -e "${YELLOW}📡 Nacos 服务注册:${NC}"
echo "  访问: http://120.26.170.213:8848/nacos"
echo "  命名空间: duda-dev"
echo "  用户名/密码: nacos/nacos"
echo ""

# 最新日志
echo -e "${YELLOW}📋 最新日志 (最后20行):${NC}"
echo -e "${YELLOW}选择服务:${NC}"
echo "  1) duda-id-generator"
echo "  2) duda-msg-provider"
echo "  3) duda-user-provider"
echo "  4) duda-user-api"
echo "  5) 全部"
echo ""
read -p "输入选择 (1-5): " choice

case $choice in
    1) docker logs duda-id-generator --tail=20 ;;
    2) docker logs duda-msg-provider --tail=20 ;;
    3) docker logs duda-user-provider --tail=20 ;;
    4) docker logs duda-user-api --tail=20 ;;
    5) docker-compose logs --tail=20 ;;
    *) echo "无效选择" ;;
esac

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}✅ 状态检查完成！${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""
echo -e "💡 查看完整日志: ${GREEN}PROJECT_LOG.md${NC}"
