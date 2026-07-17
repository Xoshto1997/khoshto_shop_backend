package com.example.ecommerce_project.service;

import com.example.ecommerce_project.dto.AdminOrderResponse;
import com.example.ecommerce_project.dto.OrderItemRequest;
import com.example.ecommerce_project.model.Order;
import com.example.ecommerce_project.model.OrderItem;
import com.example.ecommerce_project.constants.OrderStatus;
import com.example.ecommerce_project.model.Product;
import com.example.ecommerce_project.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
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

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
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

    public List<AdminOrderResponse> getAllOrdersForAdmin() {
        return orderRepository.findAll().stream().map(order -> {

            List<AdminOrderResponse.AdminOrderItemDto> itemsDto = order.getOrderItems().stream().map(item -> {
                String name = "უცნობი პროდუქტი";
                if (item.getProduct() != null) {
                    name = item.getProduct().getProductName(); 
                }
                return new AdminOrderResponse.AdminOrderItemDto(name, item.getQuantity());
            }).collect(Collectors.toList());

            return AdminOrderResponse.builder()
                    .id(order.getId())
                    .userEmail(order.getUserEmail())
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
}