package com.example.ecommerce_project.service;

import com.example.ecommerce_project.constants.OrderStatus;
import com.example.ecommerce_project.constants.PaymentStatus;
import com.example.ecommerce_project.dto.AdminOrderResponse;
import com.example.ecommerce_project.dto.CreateManualOrderRequest;
import com.example.ecommerce_project.dto.DirectOrderRequest;
import com.example.ecommerce_project.dto.OrderItemRequest;
import com.example.ecommerce_project.model.Order;
import com.example.ecommerce_project.model.OrderItem;
import com.example.ecommerce_project.model.Product;
import com.example.ecommerce_project.repository.OrderRepository;
import com.example.ecommerce_project.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public Page<AdminOrderResponse> getAdminOrdersPaged(String search, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findAllAdminOrdersPaged(search, status, pageable);

        return orderPage.map(this::mapToAdminOrderResponse);
    }

    private AdminOrderResponse mapToAdminOrderResponse(Order order) {
        List<AdminOrderResponse.AdminOrderItemDto> itemsDto = order.getOrderItems().stream().map(item -> {
            String name = "3D პროდუქტი";

            if (item.getProductName() != null && !item.getProductName().isBlank()) {
                name = item.getProductName();
            } else if (item.getProduct() != null && item.getProduct().getProductName() != null) {
                name = item.getProduct().getProductName();
            }

            return new AdminOrderResponse.AdminOrderItemDto(
                    name,
                    item.getQuantity(),
                    item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO
            );
        }).collect(Collectors.toList());

        return AdminOrderResponse.builder()
                .id(order.getId())
                .userEmail(order.getUserEmail())
                .customerName(order.getCustomerName())
                .phoneNumber(order.getPhoneNumber())
                .city(order.getCity())
                .address(order.getAddress())
                .notes(order.getNotes())
                .companyName(order.getCompanyName())
                .taxId(order.getTaxId())
                .companyAddress(order.getCompanyAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .orderItems(itemsDto)
                .build();
    }

    @Transactional
    public Order createDirectOrder(DirectOrderRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = Order.builder()
                .userEmail(request.getUserEmail())
                .customerName(request.getCustomerName())
                .phoneNumber(request.getPhoneNumber())
                .city(request.getCity())
                .address(request.getAddress())
                .notes(request.getNotes())
                .companyName(request.getCompanyName())
                .taxId(request.getTaxId())
                .companyAddress(request.getCompanyAddress())
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .orderItems(new ArrayList<>())
                .build();

        if (request.getItems() != null) {
            for (DirectOrderRequest.OrderItemDTO itemDto : request.getItems()) {
                BigDecimal itemPrice = BigDecimal.valueOf(itemDto.getPrice());
                BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProductName(itemDto.getProductName());
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setPrice(itemPrice);

                if (itemDto.getProductId() != null) {
                    Product product = productRepository.findById(itemDto.getProductId()).orElse(null);
                    orderItem.setProduct(product);
                }

                order.getOrderItems().add(orderItem);
            }
        }

        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    public List<AdminOrderResponse> getAllOrdersForAdmin() {
        return orderRepository.findAll().stream()
                .map(this::mapToAdminOrderResponse)
                .collect(Collectors.toList());
    }

    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("შეკვეთა ვერ მოიძებნა!"));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional
    public Order createManualOrder(CreateManualOrderRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (OrderItemRequest item : request.getItems()) {
                if (item.getProductId() != null) {
                    productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new IllegalArgumentException("პროდუქტი ID-ით: " + item.getProductId() + " ვერ მოიძებნა!"));
                }

                BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
        }

        Order order = Order.builder()
                .userEmail(request.getUserEmail())
                .companyName(request.getCompanyName())
                .taxId(request.getTaxId())
                .companyAddress(request.getCompanyAddress())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        if (request.getItems() != null) {
            List<OrderItem> orderItems = request.getItems().stream().map(itemDto -> {
                OrderItem orderItem = new OrderItem();
                orderItem.setProductName(itemDto.getProductName());
                orderItem.setQuantity(itemDto.getQuantity());
                orderItem.setPrice(itemDto.getPrice());

                if (itemDto.getProductId() != null) {
                    Product product = productRepository.findById(itemDto.getProductId()).orElse(null);
                    orderItem.setProduct(product);
                }

                orderItem.setOrder(order);
                return orderItem;
            }).collect(Collectors.toList());

            order.setOrderItems(orderItems);
        }

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUserEmail(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(email);
    }
}