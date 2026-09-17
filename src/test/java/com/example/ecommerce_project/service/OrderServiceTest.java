package com.example.ecommerce_project.service;

import com.example.ecommerce_project.constants.OrderStatus;
import com.example.ecommerce_project.dto.AdminOrderResponse;
import com.example.ecommerce_project.dto.OrderItemRequest;
import com.example.ecommerce_project.model.Order;
import com.example.ecommerce_project.model.OrderItem;
import com.example.ecommerce_project.model.Product;
import com.example.ecommerce_project.repository.OrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "stripeSecretKey", "sk_test_mock_key");
        orderService.init();

        Product product = new Product();
        product.setId(1L);
        product.setProductName("iPhone 15");

        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(1)
                .price(new BigDecimal("2500.00"))
                .build();

        sampleOrder = Order.builder()
                .id(100L)
                .userEmail("test@gtu.ge")
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .totalAmount(new BigDecimal("2500.00"))
                .orderItems(List.of(orderItem))
                .build();
    }


    @Test
    void should_CreateOrderAndReturnStripeUrl_Successfully() throws StripeException {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setProductName("iPhone 15");
        itemRequest.setPrice(new BigDecimal("2500.00"));
        itemRequest.setQuantity(1);

        List<OrderItemRequest> requests = List.of(itemRequest);

        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("cs_test_session_id_123");
        when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_session_id_123");

        try (MockedStatic<Session> mockedStripeSession = mockStatic(Session.class)) {
            mockedStripeSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

            String stripeUrl = orderService.createOrderAndGetStripeUrl(requests, "test@gtu.ge");

            assertNotNull(stripeUrl);
            assertEquals("https://checkout.stripe.com/pay/cs_test_session_id_123", stripeUrl);

            verify(orderRepository, times(1)).save(any(Order.class));
        }
    }


    @Test
    void should_GetAllOrdersForAdmin() {
        when(orderRepository.findAll()).thenReturn(List.of(sampleOrder));

        List<AdminOrderResponse> adminOrders = orderService.getAllOrdersForAdmin();

        assertNotNull(adminOrders);
        assertEquals(1, adminOrders.size());
        assertEquals("test@gtu.ge", adminOrders.get(0).getUserEmail());
        assertEquals(OrderStatus.PENDING, adminOrders.get(0).getStatus());
        assertEquals("iPhone 15", adminOrders.get(0).getOrderItems().get(0).getProductName());
    }


    @Test
    void should_UpdateOrderStatus_Successfully() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order updatedOrder = orderService.updateOrderStatus(100L, OrderStatus.PAID);

        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.PAID, updatedOrder.getStatus());
        verify(orderRepository, times(1)).save(sampleOrder);
    }

    @Test
    void should_ThrowException_WhenOrderNotFound_OnUpdateStatus() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.updateOrderStatus(999L, OrderStatus.PAID)
        );

        assertEquals("შეკვეთა ვერ მოიძებნა!", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }
}