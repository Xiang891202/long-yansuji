package com.ysgs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysgs.config.TenantContext;
import com.ysgs.dto.InventoryReportRequest;
import com.ysgs.dto.ReplenishmentResult;
import com.ysgs.repository.InvProductRepository;
import com.ysgs.repository.InvSafeStockRepository;
import com.ysgs.security.JwtUtils;
import com.ysgs.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private InvSafeStockRepository safeStockRepository;

    @MockBean
    private InvProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(2);
    }

    @Test
    @WithMockUser(authorities = "inventory_access", username = "123e4567-e89b-12d3-a456-426614174000")
    void testCalculateReplenishment() throws Exception {
        InventoryReportRequest.InventoryItem item = new InventoryReportRequest.InventoryItem();
        item.setProductId(UUID.randomUUID());
        item.setCurrentQuantity(5);
        item.setSafeStockVersion(1);

        InventoryReportRequest request = new InventoryReportRequest();
        request.setDayOfWeek(1);
        request.setItems(List.of(item));
        request.setVegetables(List.of("高麗菜", "花椰菜"));

        ReplenishmentResult result = new ReplenishmentResult("雞排：補 7 片\n", "高麗菜\n花椰菜");
        when(inventoryService.calculateReplenishment(anyInt(), any(), anyString(), any(), anyString()))
                .thenReturn(result);

        mockMvc.perform(post("/inventory/calculate")
                        .header("Idempotency-Key", "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supplierText").value("雞排：補 7 片\n"))
                .andExpect(jsonPath("$.vegetableText").value("高麗菜\n花椰菜"));
    }
}