package com.ysgs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YsgsBackendApplication {

    public static void main(String[] args) {
        // 【核心強置修改】直接從 Render 後台讀取正確的環境變數
        String renderDbUrl = System.getenv("RENDER_DB_URL");
        String renderDbUser = System.getenv("RENDER_DB_USER");
        String renderDbPassword = System.getenv("RENDER_DB_PASSWORD");

        // 如果雲端環境變數存在，直接無視所有設定檔，強行用代碼蓋過去
        if (renderDbUrl != null && !renderDbUrl.trim().isEmpty()) {
            System.setProperty("spring.datasource.url", renderDbUrl);
            System.setProperty("spring.datasource.username", renderDbUser);
            System.setProperty("spring.datasource.password", renderDbPassword);
        }

        SpringApplication.run(YsgsBackendApplication.class, args);
    }
}
