# 使用本地 OpenJDK 镜像
FROM openjdk:17-jdk-slim

LABEL maintainer="Claude Code <duda@example.com>"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用目录
WORKDIR /app

# 复制已编译的JAR文件
COPY duda-tenant-provider/target/duda-tenant-provider-1.0.0-SNAPSHOT.jar /app.jar

# 暴露端口
EXPOSE 20885 8088

# JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
