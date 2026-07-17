package com.example.ecommerce_project.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRequest {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
}