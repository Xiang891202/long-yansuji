package com.ysgs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class EmployeeResponse {
    private UUID id;
    private Integer tenantId;
    private String identityNumber;
    private LocalDate birthDate;
    private String name;
    private Boolean isActive;
    private List<String> permissions;
    private Instant createdAt;
}