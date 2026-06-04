package com.ysgs.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LoginRequest {
    private Integer tenantId;
    private String identityNumber;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String password;  // 新增

    // 手動 getter/setter（由於 Lombok 可能失效，加入這些方法確保編譯通過）
    public Integer getTenantId() { return tenantId; }
    public void setTenantId(Integer tenantId) { this.tenantId = tenantId; }
    public String getIdentityNumber() { return identityNumber; }
    public void setIdentityNumber(String identityNumber) { this.identityNumber = identityNumber; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}