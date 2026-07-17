package com.example.ecommerce_project.repository;

import com.example.ecommerce_project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
