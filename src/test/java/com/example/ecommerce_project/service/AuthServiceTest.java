package com.example.ecommerce_project.service;

import com.example.ecommerce_project.constants.Role;
import com.example.ecommerce_project.dto.AuthRequest;
import com.example.ecommerce_project.dto.AuthResponse;
import com.example.ecommerce_project.exception.UserAlreadyExistsException;
import com.example.ecommerce_project.model.User;
import com.example.ecommerce_project.repository.UserRepository;
import com.example.ecommerce_project.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setEmail("test@gtu.ge");
        authRequest.setPassword("password123");

        sampleUser = User.builder()
                .id(1L)
                .email("test@gtu.ge")
                .password("encoded_password")
                .role(Role.USER)
                .build();
    }


    @Test
    void should_Register_User_Successfully() {
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(authRequest.getPassword())).thenReturn("encoded_password");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt_mock_token");

        AuthResponse response = authService.register(authRequest);

        assertNotNull(response);
        assertEquals("jwt_mock_token", response.getToken());

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    void should_ThrowException_When_UserAlreadyExists_OnRegister() {
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(sampleUser));

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(authRequest)
        );

        assertEquals("მომხმარებელი ამ ელ. ფოსტით უკვე არსებობს!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void should_Login_Successfully() {
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(sampleUser));
        when(jwtService.generateToken(sampleUser)).thenReturn("jwt_mock_token");

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("jwt_mock_token", response.getToken());

        verify(authenticationManager, times(1)).authenticate(any());
    }


    @Test
    void should_ProcessForgotPassword_And_SendEmail() {
        when(userRepository.findByEmail("test@gtu.ge")).thenReturn(Optional.of(sampleUser));

        authService.processForgotPassword("test@gtu.ge");

        assertNotNull(sampleUser.getResetToken());
        assertNotNull(sampleUser.getResetTokenExpiry());

        verify(userRepository, times(1)).save(sampleUser);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void should_ThrowException_When_EmailNotFound_OnForgotPassword() {
        when(userRepository.findByEmail("unknown@gtu.ge")).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.processForgotPassword("unknown@gtu.ge")
        );

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }


    @Test
    void should_UpdatePassword_Successfully() {
        String token = "valid_token";
        sampleUser.setResetToken(token);
        sampleUser.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new_encoded_password");

        authService.updatePassword(token, "newPassword123");

        assertEquals("new_encoded_password", sampleUser.getPassword());
        assertNull(sampleUser.getResetToken());
        assertNull(sampleUser.getResetTokenExpiry());

        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void should_ThrowException_When_TokenIsExpired_OnUpdatePassword() {
        String token = "expired_token";
        sampleUser.setResetToken(token);
        sampleUser.setResetTokenExpiry(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(sampleUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.updatePassword(token, "newPassword123")
        );

        assertEquals("ტოკენს ვადა გაუვიდა!", exception.getMessage());
        verify(userRepository, never()).save(sampleUser);
    }
}