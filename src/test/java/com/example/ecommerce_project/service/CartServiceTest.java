package com.example.ecommerce_project.service;

import com.example.ecommerce_project.model.Cart;
import com.example.ecommerce_project.model.CartItem;
import com.example.ecommerce_project.model.Product;
import com.example.ecommerce_project.model.User;
import com.example.ecommerce_project.repository.CartRepository;
import com.example.ecommerce_project.repository.ProductRepository;
import com.example.ecommerce_project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private CartService cartService;

    private User sampleUser;
    private Product sampleProduct;
    private Cart sampleCart;

    @BeforeEach
    void setUp() {
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("test@gtu.ge");
        SecurityContextHolder.setContext(securityContext);

        sampleUser = User.builder()
                .id(1L)
                .email("test@gtu.ge")
                .build();

        sampleProduct = new Product();
        sampleProduct.setId(10L);
        sampleProduct.setProductName("Laptop");
        sampleProduct.setPrice(3000.0);

        sampleCart = Cart.builder()
                .id(100L)
                .user(sampleUser)
                .items(new ArrayList<>())
                .build();
    }


    @Test
    void should_ReturnExistingCart_WhenCartExists() {
        when(userRepository.findByEmail("test@gtu.ge")).thenReturn(Optional.of(sampleUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));

        Cart cart = cartService.getOrCreateCart();

        assertNotNull(cart);
        assertEquals(100L, cart.getId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void should_CreateNewCart_WhenCartDoesNotExist() {
        when(userRepository.findByEmail("test@gtu.ge")).thenReturn(Optional.of(sampleUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(sampleCart);

        Cart cart = cartService.getOrCreateCart();

        assertNotNull(cart);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void should_ThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("test@gtu.ge")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> cartService.getOrCreateCart());
    }


    @Test
    void should_AddNewProductToCart() {
        when(userRepository.findByEmail("test@gtu.ge")).thenReturn(Optional.of(sampleUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart updatedCart = cartService.addProductToCart(10L, 2);

        assertNotNull(updatedCart);
        assertEquals(1, updatedCart.getItems().size());
        assertEquals(2, updatedCart.getItems().get(0).getQuantity());
        assertEquals(10L, updatedCart.getItems().get(0).getProduct().getId());
    }

    @Test
    void should_IncreaseQuantity_WhenProductAlreadyInCart() {
        CartItem existingItem = CartItem.builder()
                .cart(sampleCart)
                .product(sampleProduct)
                .quantity(1)
                .build();
        sampleCart.getItems().add(existingItem);

        when(userRepository.findByEmail("test@gtu.ge")).thenReturn(Optional.of(sampleUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart updatedCart = cartService.addProductToCart(10L, 3);

        assertEquals(1, updatedCart.getItems().size());
        assertEquals(4, updatedCart.getItems().get(0).getQuantity());
    }


    @Test
    void should_RemoveProductFromCart() {
        CartItem item = CartItem.builder().cart(sampleCart).product(sampleProduct).quantity(2).build();
        sampleCart.getItems().add(item);

        when(userRepository.findByEmail("test@gtu.ge")).thenReturn(Optional.of(sampleUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart updatedCart = cartService.removeProductFromCart(10L);

        assertTrue(updatedCart.getItems().isEmpty());
    }


    @Test
    void should_UpdateQuantitySuccessfully() {
        CartItem item = CartItem.builder().cart(sampleCart).product(sampleProduct).quantity(2).build();
        sampleCart.getItems().add(item);

        when(userRepository.findByEmail("test@gtu.ge")).thenReturn(Optional.of(sampleUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart updatedCart = cartService.updateProductQuantity(10L, 5);

        assertEquals(5, updatedCart.getItems().get(0).getQuantity());
    }

    @Test
    void should_RemoveProduct_WhenQuantityIsZeroOrNegative() {
        CartItem item = CartItem.builder().cart(sampleCart).product(sampleProduct).quantity(2).build();
        sampleCart.getItems().add(item);

        when(userRepository.findByEmail("test@gtu.ge")).thenReturn(Optional.of(sampleUser));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart updatedCart = cartService.updateProductQuantity(10L, 0);

        assertTrue(updatedCart.getItems().isEmpty());
    }
}