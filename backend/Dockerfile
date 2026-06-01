# 第一階段：編譯
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src

# 【關鍵修改】宣告接收 Render 傳入的 Build Args
ARG JWT_SECRET
ARG DB_URL
ARG DB_USER
ARG DB_PASSWORD

# 【關鍵修改】將 Args 轉換為編譯期間可用的環境變數
ENV JWT_SECRET=$JWT_SECRET
ENV DB_URL=$DB_URL
ENV DB_USER=$DB_USER
ENV DB_PASSWORD=$DB_PASSWORD

# 帶入環境變數進行打包
RUN mvn clean package -DskipTests

# 第二階段：執行
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/ysgs-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
