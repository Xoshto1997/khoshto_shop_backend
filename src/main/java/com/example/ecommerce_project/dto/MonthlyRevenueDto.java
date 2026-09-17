package com.example.ecommerce_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyRevenueDto {
    private String yearMonth;
    private Double totalRevenue;
    private Long orderCount;
}