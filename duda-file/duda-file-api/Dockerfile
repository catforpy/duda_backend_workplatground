FROM eclipse-temurin:17-jre

LABEL maintainer="duda-nexus"

# 设置时区
RUN apt-get update && \
    apt-get install -y tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 添加应用JAR
ADD target/duda-file-api-1.0.0-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8084

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动应用
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
