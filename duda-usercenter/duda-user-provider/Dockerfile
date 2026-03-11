# Dockerfile for duda-user-provider
# 镜像标签: duda/user-provider:1.0.0
FROM eclipse-temurin:17-jre

LABEL maintainer="DudaNexus"
LABEL description="用户Provider服务 - RPC服务实现"
LABEL version="1.0.0"

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建应用目录
WORKDIR /app

# 复制JAR文件（使用固定的文件名）
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 暴露端口
EXPOSE 8082 20882

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
