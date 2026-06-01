# 第一階段：編譯（保持不變）
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# 第二階段：執行
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/ysgs-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

# 【修改回最乾淨的指令】移除所有硬編碼引用的變數，交給 Spring Boot 運行期原生讀取 Linux 環境變數
ENTRYPOINT ["java", "-jar", "app.jar"]
