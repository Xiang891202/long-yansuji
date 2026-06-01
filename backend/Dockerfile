# 第一階段：編譯
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src

# 【核心補刀】在編譯前，強制在硬碟上物理性清空個人設定檔，徹底杜絕錯字碎片干擾編譯器
RUN echo "spring.application.name=ysgs-backend" > src/main/resources/application-personal.properties

# 執行乾淨打包
RUN mvn clean package -DskipTests

# 第二階段：執行
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/ysgs-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
