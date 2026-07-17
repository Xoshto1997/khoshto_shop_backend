package com.example.ecommerce_project.controller;

import com.example.ecommerce_project.dto.OrderItemRequest;
import com.example.ecommerce_project.service.OrderService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestBody List<OrderItemRequest> cartItems,
            @RequestParam String userEmail) {

        try {
            String stripeUrl = orderService.createOrderAndGetStripeUrl(cartItems, userEmail);

            return ResponseEntity.ok(Map.of("url", stripeUrl));

        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Stripe-თან კავშირი ჩავარდა: " + e.getMessage()));
        }
    }
}