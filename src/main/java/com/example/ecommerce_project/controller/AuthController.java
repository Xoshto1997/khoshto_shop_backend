package com.example.ecommerce_project.controller;

import com.example.ecommerce_project.dto.AuthRequest;
import com.example.ecommerce_project.dto.AuthResponse;
import com.example.ecommerce_project.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.processForgotPassword(email);
        return ResponseEntity.ok().body(Map.of("message", "აღდგენის ლინკი გაიგზავნა მეილზე!"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authService.updatePassword(token, newPassword);
        return ResponseEntity.ok().body("{\"message\": \"პაროლი წარმატებით შეიცვალა!\"}");
    }
}