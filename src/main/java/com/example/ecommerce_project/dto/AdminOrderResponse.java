package com.example.ecommerce_project.dto;

import com.example.ecommerce_project.constants.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class AdminOrderResponse {
    private Long id;
    private String userEmail;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<AdminOrderItemDto> orderItems;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class AdminOrderItemDto {
        private String productName;
        private Integer quantity;
    }
}