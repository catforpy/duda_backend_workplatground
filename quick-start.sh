#!/bin/bash

# DudaNexus 快速启动脚本
# 作者: DudaNexus Team
# 创建时间: 2026-03-13

set -e

PROJECT_DIR="/Volumes/DudaDate/DudaNexus"
cd "$PROJECT_DIR"

echo "========================================="
echo "🚀 DudaNexus 快速启动脚本"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 检查 Docker
echo -e "${YELLOW}📋 检查 Docker 状态...${NC}"
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker 未运行，请先启动 Docker${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker 运行正常${NC}"
echo ""

# 检查端口占用
echo -e "${YELLOW}📋 检查端口占用...${NC}"
PORTS=(8082 8083 9090 9091)
OCCUPIED=0

for port in "${PORTS[@]}"; do
    if lsof -i :$port > /dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  端口 $port 已被占用${NC}"
        OCCUPIED=1
    fi
done

if [ $OCCUPIED -eq 1 ]; then
    echo -e "${YELLOW}是否继续启动？(y/n)${NC}"
    read -r response
    if [[ ! "$response" =~ ^[Yy]$ ]]; then
        echo "取消启动"
        exit 0
    fi
fi
echo ""

# 检查 Docker 网络
echo -e "${YELLOW}📋 检查 Docker 网络...${NC}"
if ! docker network ls | grep -q duda-network; then
    echo -e "${GREEN}✅ 创建 Docker 网络: duda-network${NC}"
    docker network create duda-network
else
    echo -e "${GREEN}✅ Docker 网络已存在${NC}"
fi
echo ""

# 启动服务
echo -e "${YELLOW}🚀 启动所有服务...${NC}"
docker-compose up -d

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}✅ 服务启动完成！${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""

# 显示服务状态
echo -e "${YELLOW}📊 服务状态:${NC}"
docker-compose ps
echo ""

# 等待健康检查
echo -e "${YELLOW}⏳ 等待服务健康检查...${NC}"
sleep 15

# 检查健康状态
echo -e "${YELLOW}📋 健康检查:${NC}"
HEALTH_CHECKS=(
    "9090:duda-id-generator"
    "9091:duda-msg-provider"
    "8082:duda-user-provider"
    "8083:duda-user-api"
)

for check in "${HEALTH_CHECKS[@]}"; do
    IFS=':' read -ra PARTS <<< "$check"
    PORT=${PARTS[0]}
    NAME=${PARTS[1]}

    if curl -s "http://localhost:$PORT/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $NAME${NC}"
    else
        echo -e "${YELLOW}⏳ $NAME (启动中...)${NC}"
    fi
done

echo ""
echo -e "${GREEN}=========================================${NC}"
echo -e "${GREEN}🎉 启动完成！${NC}"
echo -e "${GREEN}=========================================${NC}"
echo ""

echo -e "📖 完整日志文件: ${GREEN}PROJECT_LOG.md${NC}"
echo ""

echo -e "🌐 访问地址:"
echo -e "  - Swagger UI:   ${GREEN}http://localhost:8083/swagger-ui/index.html${NC}"
echo -e "  - 健康检查:     ${GREEN}http://localhost:8083/actuator/health${NC}"
echo -e "  - Nacos控制台:  ${GREEN}http://120.26.170.213:8848/nacos${NC}"
echo ""

echo -e "💡 常用命令:"
echo -e "  查看日志:   ${YELLOW}docker-compose logs -f${NC}"
echo -e "  停止服务:   ${YELLOW}docker-compose down${NC}"
echo -e "  重启服务:   ${YELLOW}docker-compose restart${NC}"
echo -e "  查看状态:   ${YELLOW}docker-compose ps${NC}"
echo ""

echo -e "📚 更多信息请查看: ${GREEN}PROJECT_LOG.md${NC}"
