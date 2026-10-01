package com.example.ecommerce_project.repository;

import com.example.ecommerce_project.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();

    // თუ Pageable-ს იყენებ პაგინაციისთვის:
    Page<Product> findByActiveTrue(Pageable pageable);
}
