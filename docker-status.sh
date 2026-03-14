#!/bin/bash

# DudaNexus 服务状态检查脚本

echo "=========================================="
echo "  DudaNexus 服务状态"
echo "=========================================="
echo ""

# 检查 Docker 容器状态
echo "【Docker 容器状态】"
docker-compose ps
echo ""

# 检查服务健康状态
echo "【服务健康检查】"
services=(
    "duda-id-generator:9090"
    "duda-msg-provider:9091"
    "duda-user-provider:8082"
    "duda-file-provider:8084"
    "duda-gateway:8080"
)

for service in "${services[@]}"; do
    name=$(echo $service | cut -d: -f1)
    port=$(echo $service | cut -d: -f2)

    if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
        echo "  ✓ $name (端口 $port) - 运行正常"
    else
        echo "  ✗ $name (端口 $port) - 无法访问"
    fi
done

echo ""
echo "=========================================="
echo "  Swagger 文档访问地址"
echo "=========================================="
echo ""
echo "🔗 网关 Swagger 文档 (推荐):"
echo "   http://localhost:8080/swagger-ui.html"
echo ""
echo "🔗 各服务独立 Swagger:"
echo "   用户服务: http://localhost:8082/swagger-ui.html"
echo "   文件服务: http://localhost:8084/swagger-ui.html"
echo ""
echo "=========================================="
echo "  Nacos 控制台"
echo "=========================================="
echo ""
echo "🔗 Nacos: http://120.26.170.213:8848/nacos"
echo "   用户名: nacos"
echo "   密码:   nacos"
echo ""
