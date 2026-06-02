# 第一階段：編譯
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# 第二階段：執行
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/ysgs-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

# 使用環境變數（不再硬編碼任何配置）
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]