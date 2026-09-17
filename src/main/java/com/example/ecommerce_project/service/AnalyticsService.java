package com.example.ecommerce_project.service;

import com.example.ecommerce_project.constants.OrderStatus;
import com.example.ecommerce_project.dto.AdminDashboardStatsDto;
import com.example.ecommerce_project.dto.MonthlyRevenueDto;
import com.example.ecommerce_project.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;

    public AdminDashboardStatsDto getDashboardStats() {
        BigDecimal totalRevenueBd = orderRepository.getTotalRevenue(OrderStatus.PAID);
        Double totalRevenue = (totalRevenueBd != null) ? totalRevenueBd.doubleValue() : 0.0;

        Long totalPaidOrders = orderRepository.countPaidOrders(OrderStatus.PAID);
        if (totalPaidOrders == null) {
            totalPaidOrders = 0L;
        }

        List<Object[]> rawMonthly = orderRepository.getMonthlyRevenueRaw();
        List<MonthlyRevenueDto> monthlyRevenues = new ArrayList<>();

        String currentMonthKey = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Double currentMonthRevenue = 0.0;

        if (rawMonthly != null) {
            for (Object[] row : rawMonthly) {
                String month = (String) row[0];
                Double revenue = (row[1] != null) ? ((Number) row[1]).doubleValue() : 0.0;
                Long count = (row[2] != null) ? ((Number) row[2]).longValue() : 0L;

                monthlyRevenues.add(new MonthlyRevenueDto(month, revenue, count));

                if (currentMonthKey.equals(month)) {
                    currentMonthRevenue = revenue;
                }
            }
        }

        return new AdminDashboardStatsDto(
                totalRevenue,
                totalPaidOrders,
                currentMonthRevenue,
                monthlyRevenues
        );
    }
}