package com.example.ecommerce_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private boolean mfaRequired;
    private boolean isSetup;
    private String qrCodeUri;
    private String secret;
    private String message;

    public AuthResponse(String token) {
        this.token = token;
        this.mfaRequired = false;
        this.isSetup = false;
        this.message = "წარმატებული ავტორიზაცია";
    }

    public AuthResponse(String token, boolean mfaRequired, String message) {
        this.token = token;
        this.mfaRequired = mfaRequired;
        this.isSetup = false;
        this.message = message;
    }
}