package com.example.ecommerce_project.controller;

import com.example.ecommerce_project.constants.OrderStatus;
import com.example.ecommerce_project.dto.AdminOrderResponse;
import com.example.ecommerce_project.dto.CreateManualOrderRequest;
import com.example.ecommerce_project.dto.DirectOrderRequest;
import com.example.ecommerce_project.dto.OrderItemRequest;
import com.example.ecommerce_project.model.Order;
import com.example.ecommerce_project.service.OrderService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create-direct")
    public ResponseEntity<Order> createDirectOrder(@Valid @RequestBody DirectOrderRequest request) {
        Order createdOrder = orderService.createDirectOrder(request);
        return ResponseEntity.ok(createdOrder);
    }

    @PostMapping("/admin/create-manual")
    public ResponseEntity<Order> createManualOrder(@Valid @RequestBody CreateManualOrderRequest request) {
        Order newOrder = orderService.createManualOrder(request);
        return ResponseEntity.ok(newOrder);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<AdminOrderResponse>> getAllOrdersForAdmin() {
        List<AdminOrderResponse> orders = orderService.getAllOrdersForAdmin();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable @NotNull(message = "შეკვეთის ID სავალდებულოა") Long orderId,
            @RequestParam @NotNull(message = "სტატუსის მითითება სავალდებულოა") OrderStatus status) {
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestBody @NotEmpty(message = "კალათა არ უნდა იყოს ცარიელი") List<@Valid OrderItemRequest> cartItems,
            @RequestParam @NotBlank(message = "ელფოსტის მითითება სავალდებულოა") @Email(message = "არასწორი ელფოსტის ფორმატი") String userEmail) {

        try {
            String stripeUrl = orderService.createOrderAndGetStripeUrl(cartItems, userEmail);
            return ResponseEntity.ok(Map.of("url", stripeUrl));

        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Stripe-თან კავშირი ჩავარდა: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<Order>> getOrdersByUserEmail(
            @PathVariable @NotBlank(message = "ელფოსტა სავალდებულოა") @Email(message = "არასწორი ელფოსტის ფორმატი") String email) {
        List<Order> userOrders = orderService.getOrdersByUserEmail(email);
        return ResponseEntity.ok(userOrders);
    }
}