package com.example.ecommerce_project.controller;

import com.example.ecommerce_project.dto.AuthRequest;
import com.example.ecommerce_project.dto.AuthResponse;
import com.example.ecommerce_project.dto.ChangePasswordRequest;
import com.example.ecommerce_project.dto.Verify2faRequest;
import com.example.ecommerce_project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<AuthResponse> verify2fa(@Valid @RequestBody Verify2faRequest request) {
        return ResponseEntity.ok(authService.verify2fa(request.getEmail(), request.getCode()));
    }

    @PostMapping("/setup-2fa")
    public ResponseEntity<Map<String, String>> setup2fa(@RequestParam String email) {
        return ResponseEntity.ok(authService.setup2fa(email));
    }

    @PostMapping("/enable-2fa")
    public ResponseEntity<?> enable2fa(@Valid @RequestBody Verify2faRequest request) {
        authService.verifyAndEnable2fa(request.getEmail(), request.getCode());
        return ResponseEntity.ok().body(Map.of("message", "2FA წარმატებით გააქტიურდა!"));
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

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "მომხმარებელი არ არის ავტორიზებული!"));
        }

        try {
            authService.changePassword(principal.getName(), request);
            return ResponseEntity.ok(Map.of("message", "პაროლი წარმატებით შეიცვალა"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "შეცდომა პაროლის შეცვლისას: " + e.getMessage()));
        }
    }
}