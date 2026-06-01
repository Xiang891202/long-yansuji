# 第一階段：編譯（保持不變）
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# 第二階段：執行（【修改這行】換成啟動更快速的 slim 鏡像）
FROM eclipse-temurin:17-jre-slim
WORKDIR /app
COPY --from=builder /app/target/ysgs-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
