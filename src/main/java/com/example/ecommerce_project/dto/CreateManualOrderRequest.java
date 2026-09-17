package com.example.ecommerce_project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CreateManualOrderRequest {

    @NotBlank(message = "მომხმარებლის ელფოსტის მითითება სავალდებულოა")
    @Email(message = "გთხოვთ მიუთითოთ ვალიდური ელფოსტა")
    private String userEmail;

    private String companyName;
    private String taxId;
    private String companyAddress;

    @NotEmpty(message = "შეკვეთა უნდა შეიცავდეს სულ მცირე ერთ პროდუქტს")
    @Valid
    private List<OrderItemRequest> items;
}