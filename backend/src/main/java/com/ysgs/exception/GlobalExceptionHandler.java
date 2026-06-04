package com.ysgs.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        // 如果是鎖定異常，可以額外附加剩餘秒數
        if (ex.getMessage().contains("账号已锁定")) {
            // 可以從 ThreadLocal 或再次查詢取得剩餘時間，但簡單起見只返回訊息
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}