package com.ysgs.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class EmployeeRequest {
    private String identityNumber;
    // private LocalDate birthDate;   // 移除生日
    private String password;          // 新增密碼（明文）
    private String name;
    private Boolean isActive;
    private List<String> permissions;
}