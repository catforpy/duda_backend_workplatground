#!/bin/bash

# DudaNexus Dockerfile 生成脚本
# 为所有服务生成标准的 Dockerfile

echo "╔══════════════════════════════════════════════════════════╗"
echo "║     DudaNexus Dockerfile 生成工具                          ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# 项目根目录
PROJECT_ROOT="/Volumes/DudaDate/DudaNexus"
cd "$PROJECT_ROOT" || exit 1

# 服务列表（格式："模块名:端口:服务描述"）
declare -A SERVICES
SERVICES["duda-search/duda-search-provider"]="8083:搜索服务"
SERVICES["duda-content/duda-content-provider"]="8084:内容服务"
SERVICES["duda-order/duda-order-provider"]="8085:订单服务"
SERVICES["duda-notification/duda-notification-provider"]="8086:通知服务"
SERVICES["duda-monitor/duda-monitor-provider"]="8087:监控服务"

# 生成 Dockerfile 的函数
generate_dockerfile() {
    local service_path=$1
    local port=$2
    local description=$3

    # 检查服务目录是否存在
    if [ ! -d "$service_path" ]; then
        echo "⚠️  跳过不存在的服务: $service_path"
        return
    fi

    local dockerfile_path="$service_path/Dockerfile"

    # 如果 Dockerfile 已存在，跳过
    if [ -f "$dockerfile_path" ]; then
        echo "✅ 已存在: $dockerfile_path"
        return
    fi

    # 创建 Dockerfile
    cat > "$dockerfile_path" << EOF
# 多阶段构建 Dockerfile - ${description}
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl tzdata
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar
RUN mkdir -p /var/log/duda && chown -R app:app /var/log/duda

USER app

EXPOSE ${port}

ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \\
    CMD curl -f http://localhost:${port}/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java \$JAVA_OPTS -jar app.jar"]
EOF

    echo "✅ 生成完成: $dockerfile_path"
}

# 遍历所有服务
echo "开始生成 Dockerfile..."
echo ""

for service_info in "${!SERVICES[@]}"; do
    IFS=':' read -r path port desc <<< "$service_info:${SERVICES[$service_info]}"
    generate_dockerfile "$path" "$port" "$desc"
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "生成完成！"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📊 已生成的 Dockerfile:"
echo ""

# 列出所有 Dockerfile
find . -name "Dockerfile" -type f | grep -E "(duda-.*-provider|duda-gateway)" | sort

echo ""
echo "💡 下一步:"
echo "  • 使用 'docker-compose build' 构建镜像"
echo "  • 使用 'docker-compose up -d' 启动服务"
echo ""
