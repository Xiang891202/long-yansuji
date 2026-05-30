package com.ysgs.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class ImageUploadService {

    private final OkHttpClient httpClient = new OkHttpClient();

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    public String uploadImage(MultipartFile file, String fileName) throws IOException {
        // 建立檔案路徑 (可自行調整前綴)
        String filePath = String.format("products/%s", fileName);
        String uploadUrl = String.format("%s/storage/v1/object/%s/%s",
                supabaseUrl, bucketName, filePath);

        // 直接使用檔案位元組作為請求體，設定正確的 Content-Type
        MediaType mediaType = MediaType.parse(file.getContentType());
        RequestBody requestBody = RequestBody.create(file.getBytes(), mediaType);
        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBody)  // 或使用 put(requestBody)
                .addHeader("Authorization", "Bearer " + serviceKey)
                .addHeader("Content-Type", file.getContentType())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new RuntimeException("上傳失敗: " + response.code() + " - " + response.message() + " " + errorBody);
                }
        }

        // 回傳公開 URL (假設 bucket 是公開的)
        return String.format("%s/storage/v1/object/public/%s/%s",
                supabaseUrl, bucketName, filePath);
        }
}