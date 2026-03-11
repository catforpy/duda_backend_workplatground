#!/bin/bash

# DudaNexus 项目打包和Docker构建脚本
# 使用方法: ./build.sh

set -e

echo "========================================="
echo "  DudaNexus 打包和构建"
echo "========================================="

# 设置Java 17
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
echo "✓ Java版本:"
java -version

echo ""
echo "========================================="
echo "  第一步: 清理并打包项目"
echo "========================================="
mvn clean package -DskipTests

echo ""
echo "========================================="
echo "  第二步: 构建Docker镜像"
echo "========================================="

# 构建Provider镜像
echo "→ 构建 duda/user-provider:1.0.0"
docker build -t duda/user-provider:1.0.0 ./duda-usercenter/duda-user-provider

# 构建API镜像
echo "→ 构建 duda/user-api:1.0.0"
docker build -t duda/user-api:1.0.0 ./duda-usercenter/duda-user-api

echo ""
echo "========================================="
echo "  构建完成！"
echo "========================================="
echo ""
echo "查看镜像:"
docker images | grep duda

echo ""
echo "启动服务:"
echo "  docker-compose up -d"
echo ""
echo "查看日志:"
echo "  docker-compose logs -f"
