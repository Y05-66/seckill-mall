# ==================== 构建阶段 ====================
FROM maven:3.8-openjdk-17 AS builder

WORKDIR /app
COPY pom.xml .
# 先下载依赖（利用Docker缓存层，依赖不变时不重新下载）
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ==================== 运行阶段 ====================
FROM openjdk:17-jdk-slim

WORKDIR /app

# 从构建阶段复制jar包
COPY --from=builder /app/target/seckill-mall-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# JVM参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
