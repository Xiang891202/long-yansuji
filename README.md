# 龍鹽酥雞點貨系統

多租戶庫存管理系統，包含後端 Spring Boot API 與前端 Vue 3 管理介面，支援員工回報庫存、自動計算叫貨清單、安全庫存模板管理等功能。

## ✅ 已完成功能

### 後端
- ✅ 多租戶隔離（`tenant_id` 整數型態）
- ✅ 員工登入（身分證 + 生日）→ JWT（權限：`inventory_access`）
- ✅ 管理員登入（Email + 手機）→ JWT（權限：`admin`）
- ✅ 商品分類 CRUD（僅管理員可操作）
- ✅ 商品管理 CRUD（含圖片欄位、多規格價格 JSONB）
- ✅ 安全庫存模板（依星期設定，樂觀鎖 `version`）
- ✅ 員工點貨回報與叫貨計算（冪等性 `Idempotency-Key`）
- ✅ 叫貨統計報表 API（依日期區間查詢補貨總量）
- ✅ 商品圖片上傳（整合 Supabase Storage）
- ✅ Spring Security + JWT 過濾器
- ✅ Flyway 資料庫遷移
- ✅ 單元測試（Service 層與 Controller 層，覆蓋核心邏輯）
- ✅ Docker 容器化（支援 Render 部署）

### 前端 (Vue 3)
- ✅ 管理員登入頁面（RWD 優化）
- ✅ 員工登入頁面（RWD 優化）
- ✅ 管理員儀表板（手機版抽屜式導航）
- ✅ 商品管理頁面（分類分組、CRUD、圖片上傳、RWD）
- ✅ 安全庫存管理頁面（依星期管理、樂觀鎖、RWD）
- ✅ 分類管理頁面（CRUD、RWD）
- ✅ 人資管理頁面（員工 CRUD、啟用/停用）
- ✅ 叫貨統計報表（日期區間、表格、ECharts 圖表）
- ✅ 員工點貨頁面（動態載入蔬菜清單、庫存回報、冪等性）
- ✅ 公開首頁（商品展示、分類篩選、RWD）
- ✅ API 串接與 JWT 自動附加
- ✅ 響應式設計（手機、平板、桌機）

## 🚧 規劃中
- 線上點餐模組（購物車、訂單）
- 人資進階功能（出勤、薪資）
- 更多統計圖表與匯出功能

## 技術棧

### 後端
- Java 17
- Spring Boot 3.1.5
- Spring Data JPA
- PostgreSQL (Supabase)
- Flyway
- JWT (JJWT)
- Maven
- Spring Security
- JUnit 5 / Mockito

### 前端
- Vue 3
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios
- ECharts

## 環境要求

- JDK 17
- Maven 3.9+
- Node.js 18+
- PostgreSQL 資料庫（建議 Supabase）

## 快速啟動

### 後端

1. **複製專案並進入後端目錄**：
   ```bash
   git clone https://github.com/Xiang891202/long-yansuji.git
   cd long-yansuji/backend

2.編譯專案（使用 Maven 完整路徑，或將 Maven 加入 PATH）：

powershell
C:\maven\apache-maven-3.9.16\bin\mvn.cmd clean compile

3.設定環境變數 (PowerShell 範例)：

powershell
$env:DB_URL="jdbc:postgresql://your-db-host:5432/postgres?sslmode=require"
$env:DB_USER="postgres"
$env:DB_PASSWORD="your_password"
$env:TENANT_ID="2"
$env:SUPABASE_URL="https://your-project.supabase.co"
$env:SUPABASE_SERVICE_KEY="your_service_role_key"
$env:SUPABASE_BUCKET="long-yansuji-products"

4.啟動後端：

powershell
C:\maven\apache-maven-3.9.16\bin\mvn.cmd spring-boot:run

### 前端

1.進入前端目錄並安裝依賴：

bash
cd ../frontend
npm install

2.設定環境變數 (建立 .env.development)：

env
VITE_API_BASE_URL=http://localhost:8080

3.啟動前端開發伺服器：

bash
npm run dev

Docker 部署
後端已提供 Dockerfile，可建置映像並部署至 Render 等平台。前端建議使用 Vercel 或靜態託管。

後端映像建置
bash
docker build -t ysgs-backend ./backend
docker run -p 8080:8080 --env-file .env ysgs-backend

GitHub Actions CI/CD
推送到 master/main 分支時，會自動執行後端單元測試並建置 Docker 映像，推送到 GitHub Container Registry (ghcr.io)。

API 測試
健康檢查：GET http://localhost:8080/health

員工登入：POST http://localhost:8080/auth/employee/login

管理員登入：POST http://localhost:8080/auth/admin/login

商品管理：GET /admin/products (需管理員 token)

安全庫存：GET /admin/safe-stocks?dayOfWeek=1 (需管理員 token)

統計報表：GET /admin/statistics/replenishment?startDate=2026-01-01&endDate=2026-01-31 (需管理員 token)

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