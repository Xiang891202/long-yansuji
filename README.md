# 龍鹽酥雞點貨系統 (後端)

多租戶庫存管理後端系統，支援員工回報庫存、自動計算叫貨清單、安全庫存模板管理等功能。

## ✅ 已完成功能

- ✅ 多租戶隔離（`tenant_id` 整數型態）
- ✅ 員工登入（身分證 + 生日）→ JWT（權限：`inventory_access`）
- ✅ 管理員登入（Email + 手機）→ JWT（權限：`admin`）
- ✅ 商品分類 CRUD（僅管理員可操作）
- ✅ Spring Security + JWT 過濾器
- ✅ Flyway 資料庫遷移（Schema 版本管理）
- ✅ 健康檢查與測試 API

## 🚧 進行中 / 規劃中

- 商品管理（含圖片上傳、價格設定）
- 安全庫存模板（依星期設定，樂觀鎖）
- 員工點貨回報與叫貨計算（冪等性）
- 前後端串接（Vue 3）

## 技術棧

- Java 17
- Spring Boot 3.1.5
- Spring Data JPA
- PostgreSQL (Supabase)
- Flyway
- JWT (JJWT)
- Maven
- Spring Security

## 環境要求

- JDK 17
- Maven 3.9+
- PostgreSQL 資料庫（建議 Supabase）

## 快速啟動

1. **複製專案**
   ```bash
   git clone https://github.com/Xiang891202/long-yansuji.git
   cd long-yansuji/backend
   C:\maven\apache-maven-3.9.16\bin\mvn.cmd clean compile

2.設定資料庫連線（使用環境變數）
複製 application.properties.example（若無，請參考下方範例）並設定環境變數：

properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/postgres}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:}
app.tenant-id=${TENANT_ID:2}
app.jwt.secret=${JWT_SECRET:your_jwt_secret_here}


3.啟動前設定環境變數（PowerShell 範例）

powershell
$env:DB_URL="jdbc:postgresql://your-db-host:5432/postgres?sslmode=require"
$env:DB_USER="postgres"
$env:DB_PASSWORD="your_password"
$env:TENANT_ID="2"
mvn spring-boot:run


4.測試 API

健康檢查：GET http://localhost:8080/health → OK

測試資料庫連線：GET http://localhost:8080/test/db → Tenant: 2, Employee count: 1

員工登入：POST http://localhost:8080/auth/employee/login

json
{ "tenantId": 2, "identityNumber": "B987654321", "birthDate": "1990-01-02" }
管理員登入：POST http://localhost:8080/auth/admin/login

json
{ "tenantId": 2, "email": "boss@ysgs.com", "phone": "0987654321" }

資料庫結構
Flyway 腳本：src/main/resources/db/migration/V1__init_schema.sql

租戶範例：id=1 美容專案，id=2 鹽酥雞客戶

測試員工：身分證 B987654321，生日 1990-01-02，權限 ["inventory_access"]

測試管理員：boss@ysgs.com / 0987654321，權限 ["admin"]

注意事項
敏感設定檔（含真實密碼）請勿提交至 Git，已加入 .gitignore。

租戶 ID 在資料庫中為 INTEGER，對應 Java 中的 Integer。

多租戶隔離透過 TenantInterceptor 自動注入當前租戶 ID。

授權
本專案為商業用途，未經授權不得任意散佈。