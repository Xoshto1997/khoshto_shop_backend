package com.example.ecommerce_project.service;

import com.example.ecommerce_project.model.Cart;
import com.example.ecommerce_project.model.CartItem;
import com.example.ecommerce_project.model.Product;
import com.example.ecommerce_project.model.User;
import com.example.ecommerce_project.repository.CartRepository;
import com.example.ecommerce_project.repository.ProductRepository;
import com.example.ecommerce_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("მომხმარებელი ვერ მოიძებნა მეილით: " + email));
    }

    @Transactional
    public Cart getOrCreateCart() {
        User currentUser = getCurrentUser();

        return cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(currentUser)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    @Transactional
    public Cart addProductToCart(Long productId, Integer quantity) {
        Cart cart = getOrCreateCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("პროდუქტი ვერ მოიძებნა ID-ით: " + productId));

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeProductFromCart(Long productId) {
        Cart cart = getOrCreateCart();

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateProductQuantity(Long productId, Integer quantity) {
        Cart cart = getOrCreateCart();

        if (quantity <= 0) {
            return removeProductFromCart(productId);
        }

        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        return cartRepository.save(cart);
    }
}