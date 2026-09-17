package com.example.ecommerce_project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = "ელფოსტის მითითება სავალდებულოა")
    @Email(message = "გთხოვთ მიუთითოთ ვალიდური ელფოსტის მისამართი")
    private String email;

    @NotBlank(message = "პაროლის მითითება სავალდებულოა")
    private String password;

    private String twoFactorCode;
}