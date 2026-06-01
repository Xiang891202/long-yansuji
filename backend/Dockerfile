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

# 【關鍵修改】使用 shell 格式啟動，確保 Java 執行期能 100% 讀取到 Render 的環境變數
ENTRYPOINT ["sh", "-c", "java -jar app.jar --spring.datasource.url=\"$DB_URL\" --spring.datasource.username=\"$DB_USER\" --spring.datasource.password=\"$DB_PASSWORD\" --app.tenant-id=\"$TENANT_ID\" --supabase.url=\"$SUPABASE_URL\" --supabase.service-key=\"$SUPABASE_SERVICE_KEY\" --supabase.bucket=\"$SUPABASE_BUCKET\""]
