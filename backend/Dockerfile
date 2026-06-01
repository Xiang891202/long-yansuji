# 第一階段：編譯（保持不變）
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

# 【最終制裁指令】利用 Java 命令列參數 (-D) 暴力強行注入！
# 這種寫法是 Java 最高的配置權限，會直接碾壓並無視 JAR 包內部任何損壞的 properties 檔案
ENTRYPOINT ["java", \
            "-Dspring.datasource.url=jdbc:postgresql://://supabase.com", \
            "-Dspring.datasource.username=postgres.bsgmgwwogsrtageajzks", \
            "-Dspring.datasource.password=2026Beautycrm", \
            "-Dspring.datasource.properties.sslfactory=org.postgresql.ssl.NonValidatingFactory", \
            "-jar", "app.jar"]
