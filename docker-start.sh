#!/bin/bash

# DudaNexus 微服务构建和启动脚本
# 依次构建并启动所有服务

set -e

echo "=========================================="
echo "  DudaNexus 微服务 Docker 启动脚本"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 步骤1: 清理旧的容器和镜像
echo -e "${YELLOW}[步骤 1/6] 清理旧的 Docker 容器和镜像...${NC}"
docker-compose down
echo ""

# 步骤2: 构建所有服务的 jar 包
echo -e "${YELLOW}[步骤 2/6] Maven 打包所有服务...${NC}"

echo "  → 构建 duda-id-generator..."
cd /Volumes/DudaDate/DudaNexus/duda-id-generator/duda-id-generator-provider
mvn clean package -DskipTests

echo "  → 构建 duda-msg-provider..."
cd /Volumes/DudaDate/DudaNexus/duda-msg/duda-msg-provider
mvn clean package -DskipTests

echo "  → 构建 duda-user-provider..."
cd /Volumes/DudaDate/DudaNexus/duda-usercenter/duda-user-provider
mvn clean package -DskipTests

echo "  → 构建 duda-file-provider..."
cd /Volumes/DudaDate/DudaNexus/duda-file/duda-file-provider
mvn clean package -DskipTests

echo "  → 构建 duda-gateway..."
cd /Volumes/DudaDate/DudaNexus/duda-gateway
mvn clean package -DskipTests

echo -e "${GREEN}✓ Maven 打包完成${NC}"
echo ""

# 步骤3: 复制 jar 包到服务目录（gateway 需要复制）
echo -e "${YELLOW}[步骤 3/6] 复制 jar 包...${NC}"
if [ -f /Volumes/DudaDate/DudaNexus/duda-gateway/target/duda-gateway-1.0.0-SNAPSHOT.jar ]; then
    cp /Volumes/DudaDate/DudaNexus/duda-gateway/target/duda-gateway-1.0.0-SNAPSHOT.jar \
       /Volumes/DudaDate/DudaNexus/duda-gateway/duda-gateway-1.0.0-SNAPSHOT.jar
    echo "  ✓ gateway jar 包已复制"
else
    echo -e "${RED}✗ gateway jar 包不存在，请先构建${NC}"
    exit 1
fi
echo ""

# 步骤4: 构建 Docker 镜像
echo -e "${YELLOW}[步骤 4/6] 构建 Docker 镜像...${NC}"
cd /Volumes/DudaDate/DudaNexus
docker-compose build --no-cache
echo -e "${GREEN}✓ Docker 镜像构建完成${NC}"
echo ""

# 步骤5: 启动服务
echo -e "${YELLOW}[步骤 5/6] 启动所有服务...${NC}"
docker-compose up -d
echo ""

# 步骤6: 等待服务启动并检查状态
echo -e "${YELLOW}[步骤 6/6] 等待服务启动...${NC}"
sleep 30

echo ""
echo -e "${GREEN}=========================================="
echo "  服务启动完成！"
echo "==========================================${NC}"
echo ""
echo "服务访问地址："
echo "  - 网关 (Swagger):    http://localhost:8080"
echo "  - ID生成器:          http://localhost:9090"
echo "  - 消息服务:          http://localhost:9091"
echo "  - 用户服务:          http://localhost:8082"
echo "  - 文件服务:          http://localhost:8084"
echo ""
echo "查看服务状态:"
echo "  docker-compose ps"
echo ""
echo "查看服务日志:"
echo "  docker-compose logs -f [服务名]"
echo ""
echo "停止所有服务:"
echo "  docker-compose down"
echo ""
