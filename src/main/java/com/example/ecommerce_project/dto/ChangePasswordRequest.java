package com.example.ecommerce_project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "მიმდინარე პაროლი სავალდებულოა")
    private String currentPassword;

    @NotBlank(message = "ახალი პაროლი სავალდებულოა")
    @Size(min = 6, message = "ახალი პაროლი უნდა იყოს მინიმუმ 6 სიმბოლო")
    private String newPassword;
}