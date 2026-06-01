package com.ysgs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YsgsBackendApplication {

    public static void main(String[] args) {
        // 【無條件暴力清洗】不進行任何 if 判斷，只要程式一啟動，強制把資料庫網址校正為正確的連線池
        String targetUrl = "jdbc:postgresql://://supabase.com";
        String targetUser = "postgres.bsgmgwwogsrtageajzks";
        String targetPass = "2026Beautycrm";

        // 強制塞進 JVM 系統變數，徹底斷絕所有外部干擾
        System.setProperty("spring.datasource.url", targetUrl);
        System.setProperty("spring.datasource.username", targetUser);
        System.setProperty("spring.datasource.password", targetPass);

        // 列印日誌到控制台，讓我們在 Render 畫面上 100% 確定有換成功
        System.out.println("[CRITICAL_FIX] Database URL has been forcefully set to: " + targetUrl);

        SpringApplication.run(YsgsBackendApplication.class, args);
    }
}