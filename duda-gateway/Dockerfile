# 多阶段构建 Dockerfile - 网关服务
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

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
