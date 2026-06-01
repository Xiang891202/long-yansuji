package com.ysgs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YsgsBackendApplication {

    public static void main(String[] args) {
        String renderDbUrl = System.getenv("RENDER_DB_URL");
        String renderDbUser = System.getenv("RENDER_DB_USER");
        String renderDbPassword = System.getenv("RENDER_DB_PASSWORD");

        // 【終極清洗機制】如果抓到的網址是錯的，或者包含 supabase.com 的髒資料，直接強行修正
        if (renderDbUrl == null || renderDbUrl.contains("://://supabase.com") || renderDbUrl.trim().isEmpty()) {
            // 直接硬性強制指向你正確的 Supabase 17-jre 連線池
            renderDbUrl = "jdbc:postgresql://://supabase.com";
            renderDbUser = "postgres.bsgmgwwogsrtageajzks";
            renderDbPassword = "2026Beautycrm";
        }

        // 強制寫入 JVM 系統屬性，覆蓋所有設定檔與卡住的變數
        System.setProperty("spring.datasource.url", renderDbUrl);
        System.setProperty("spring.datasource.username", renderDbUser);
        System.setProperty("spring.datasource.password", renderDbPassword);

        SpringApplication.run(YsgsBackendApplication.class, args);
    }
}
