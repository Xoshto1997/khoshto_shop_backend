package com.example.ecommerce_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardStatsDto {
    private Double totalRevenue;
    private Long totalPaidOrders;
    private Double currentMonthRevenue;
    private List<MonthlyRevenueDto> monthlyRevenues;
}