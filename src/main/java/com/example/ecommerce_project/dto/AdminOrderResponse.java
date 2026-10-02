package com.example.ecommerce_project.dto;

import com.example.ecommerce_project.constants.OrderStatus;
import com.example.ecommerce_project.constants.PaymentMethod;
import com.example.ecommerce_project.constants.PaymentStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminOrderResponse {
    private Long id;
    private String userEmail;

    private String customerName;
    private String phoneNumber;
    private String city;
    private String address;
    private String notes;

    private String companyName;
    private String taxId;
    private String companyAddress;

    private BigDecimal totalAmount;
    private OrderStatus status;

    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;
    private List<AdminOrderItemDto> orderItems;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdminOrderItemDto {
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}