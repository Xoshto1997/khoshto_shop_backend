package com.example.ecommerce_project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Verify2faRequest {

    @NotBlank(message = "ელფოსტის მითითება სავალდებულოა")
    @Email(message = "გთხოვთ მიუთითოთ ვალიდური ელფოსტის მისამართი")
    private String email;

    @NotBlank(message = "დადასტურების კოდის მითითება სავალდებულოა")
    @Size(min = 6, max = 6, message = "დადასტურების კოდი უნდა შედგებოდეს 6 ციფრისგან")
    @Pattern(regexp = "^[0-9]+$", message = "კოდი უნდა შეიცავდეს მხოლოდ ციფრებს")
    private String code;
}