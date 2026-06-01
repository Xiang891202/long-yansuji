package com.ysgs.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class EmployeeRequest {
    private String identityNumber;
    private LocalDate birthDate;
    private String name;
    private Boolean isActive;
    private List<String> permissions;
}