package com.example.ecommerce_project.service;

import com.example.ecommerce_project.constants.PaymentStatus;
import com.example.ecommerce_project.dto.AdminOrderResponse;
import com.example.ecommerce_project.dto.CreateManualOrderRequest;
import com.example.ecommerce_project.dto.DirectOrderRequest;
import com.example.ecommerce_project.dto.OrderItemRequest;
import com.example.ecommerce_project.model.Order;
import com.example.ecommerce_project.model.OrderItem;
import com.example.ecommerce_project.constants.OrderStatus;
import com.example.ecommerce_project.model.Product;
import com.example.ecommerce_project.repository.OrderRepository;
import com.example.ecommerce_project.repository.ProductRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Transactional
    public Order createDirectOrder(DirectOrderRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 🆕 აქ დაემატა companyName, taxId, companyAddress
        Order order = Order.builder()
                .userEmail(request.getUserEmail())
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
        return orderRepository.findAll().stream().map(order -> {

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
                    .companyName(order.getCompanyName())
                    .taxId(order.getTaxId())
                    .companyAddress(order.getCompanyAddress())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus())
                    .createdAt(order.getCreatedAt())
                    .orderItems(itemsDto)
                    .build();
        }).collect(Collectors.toList());
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

    public String createOrderAndGetStripeUrl(List<OrderItemRequest> itemsRequest, String userEmail) throws StripeException {
        Order order = Order.builder()
                .userEmail(userEmail)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SessionCreateParams.LineItem> stripeLineItems = new ArrayList<>();

        for (OrderItemRequest req : itemsRequest) {
            BigDecimal itemPrice = req.getPrice();
            BigDecimal itemTotal = itemPrice.multiply(new BigDecimal(req.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            Product dummyProduct = new Product();
            dummyProduct.setId(req.getProductId());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(dummyProduct)
                    .productName(req.getProductName())
                    .quantity(req.getQuantity())
                    .price(itemPrice)
                    .build();
            order.getOrderItems().add(orderItem);

            SessionCreateParams.LineItem stripeItem = SessionCreateParams.LineItem.builder()
                    .setQuantity((long) req.getQuantity())
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("gel")
                                    .setUnitAmount(itemPrice.multiply(new BigDecimal(100)).longValue())
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(req.getProductName())
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();
            stripeLineItems.add(stripeItem);
        }

        order.setTotalAmount(totalAmount);

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:4200/order-success")
                .setCancelUrl("http://localhost:4200/cart")
                .addAllLineItem(stripeLineItems)
                .build();

        Session session = Session.create(params);

        order.setStripeSessionId(session.getId());
        orderRepository.save(order);

        return session.getUrl();
    }

    public List<Order> getOrdersByUserEmail(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(email);
    }

}