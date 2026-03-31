#!/bin/bash

echo "======================================"
echo "  重新部署 duda-tenant 测试新功能"
echo "======================================"
echo ""

# 1. 进入项目目录
cd /Volumes/DudaDate/DudaNexus/duda-tenant

# 2. 清理并编译
echo "📦 步骤1: 编译项目..."
mvn clean package -DskipTests

# 3. 停止旧容器
echo "🛑 步骤2: 停止旧容器..."
docker-compose down

# 4. 启动新容器
echo "🚀 步骤3: 启动新容器..."
docker-compose up -d --build

# 5. 等待服务启动
echo "⏳ 步骤4: 等待服务启动（30秒）..."
sleep 30

# 6. 检查服务状态
echo ""
echo "✅ 部署完成！检查服务状态..."
echo ""

# API服务健康检查
echo "📊 API服务 (8089):"
curl -s http://localhost:8089/actuator/health | jq '.status'
echo ""

# Provider服务健康检查
echo "📊 Provider服务 (8088):"
curl -s http://localhost:8088/actuator/health | jq '.status'
echo ""

# Dubbo服务注册检查
echo "📊 Dubbo服务注册:"
curl -s http://localhost:8089/actuator/services | jq '.keys'
echo ""

echo "======================================"
echo "  部署完成！准备测试..."
echo "======================================"
echo ""
echo "Swagger UI: http://localhost:8089/swagger-ui.html"
echo ""
