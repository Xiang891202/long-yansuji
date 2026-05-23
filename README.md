# 龍鹽酥雞點貨系統 (後端)

多租戶庫存管理後端系統，支援員工回報庫存、自動計算叫貨清單、安全庫存模板管理等功能。

## 技術棧

- Java 17
- Spring Boot 3.1.5
- Spring Data JPA
- PostgreSQL (Supabase)
- Flyway (資料庫遷移)
- JWT (身份驗證)
- Maven

## 功能模組

- 多租戶隔離 (tenant_id)
- 員工登入 (身分證 + 生日) 與權限管理
- 管理員登入 (Email + 手機)
- 商品分類與商品管理
- 安全庫存模板 (依星期設定)
- 員工點貨回報，自動計算補貨清單 (含樂觀鎖與冪等性)
- 叫貨統計報表

## 環境要求

- JDK 17
- Maven 3.9+
- PostgreSQL 資料庫 (建議使用 Supabase)

## 快速啟動

1. **複製專案**
   ```bash
   git clone https://github.com/Xiang891202/long-yansuji.git
   cd long-yansuji/backend
設定資料庫連線 (使用環境變數)

建立 application.properties 或使用環境變數：

properties
spring.datasource.url=jdbc:postgresql://your-db-host:5432/postgres?sslmode=require
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
app.tenant-id=2
執行 Flyway 遷移

Maven 會自動執行 (第一次啟動時)

啟動應用程式

bash
mvn spring-boot:run
或使用環境變數啟動 (PowerShell 範例)：

powershell
$env:DB_URL="jdbc:postgresql://your-host:5432/postgres?sslmode=require"
$env:DB_USER="postgres"
$env:DB_PASSWORD="your_password"
$env:TENANT_ID="2"
mvn spring-boot:run
測試 API

GET http://localhost:8080/health → OK

GET http://localhost:8080/test/db → Tenant: 2, Employee count: 1

資料庫結構
使用 Flyway 管理，腳本位於 src/main/resources/db/migration/V1__init_schema.sql

注意事項
敏感資訊：application.properties、application-personal.properties 等檔案請勿提交至 Git (已加入 .gitignore)。

租戶 ID 型態：資料庫中 tenant_id 為 INTEGER，對應 Java 中的 Integer。

多租戶：所有資料表都包含 tenant_id 欄位，透過 TenantInterceptor 自動注入當前租戶 ID。

後續開發計畫
員工登入 API (JWT)

管理員後台 (商品、安全庫存管理)

點貨計算核心邏輯 (樂觀鎖 + 冪等性)

前後端串接 (Vue 3)

授權
本專案為商業用途，未經授權不得任意散佈。