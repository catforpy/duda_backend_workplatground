# Dockerfile for duda-auth-provider
# 镜像标签: duda/auth-provider:1.0.0
FROM eclipse-temurin:17-jre

LABEL maintainer="DudaNexus"
LABEL description="认证服务 - Auth Provider"
LABEL version="1.0.0"

# 设置时区
RUN apt-get update && \
    apt-get install -y tzdata wget curl && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

VOLUME ["/tmp"]

# 复制已经编译好的 JAR 文件
ADD duda-auth-provider-1.0.0-SNAPSHOT.jar app.jar

# 设置JVM参数
ENV JAVA_OPTS="\
-server \
-Xmx1g \
-Xms1g \
-Xmn256m \
-Duser.timezone=Asia/Shanghai"

# 暴露端口
EXPOSE 8081

# 启动应用
ENTRYPOINT java ${JAVA_OPTS} \
-Djava.security.egd=file:/dev/./urandom \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.io=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED \
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.math=ALL-UNNAMED \
-jar app.jar
