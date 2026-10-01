package com.example.ecommerce_project.repository;

import com.example.ecommerce_project.constants.OrderStatus;
import com.example.ecommerce_project.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    Optional<Order> findByStripeSessionId(String stripeSessionId);



    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product")
    List<Order> findAllWithItemsAndProducts();

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    @Query("SELECT o FROM Order o WHERE " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:search IS NULL OR :search = '' OR " +
            " LOWER(o.userEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            " CAST(o.id AS string) LIKE CONCAT('%', :search, '%'))")
    Page<Order> findAllAdminOrdersPaged(
            @Param("search") String search,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal getTotalRevenue(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countPaidOrders(@Param("status") OrderStatus status);

    @Query(value = "SELECT DATE_FORMAT(o.created_at, '%Y-%m') AS yearMonth, " +
            "SUM(o.total_amount) AS totalRevenue, " +
            "COUNT(o.id) AS orderCount " +
            "FROM orders o " +
            "WHERE o.status = 'PAID' " +
            "GROUP BY DATE_FORMAT(o.created_at, '%Y-%m') " +
            "ORDER BY yearMonth DESC", nativeQuery = true)
    List<Object[]> getMonthlyRevenueRaw();
}