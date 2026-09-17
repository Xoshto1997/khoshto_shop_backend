package com.example.ecommerce_project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRequest {

    private Long productId;

    @NotBlank(message = "პროდუქტის დასახელება სავალდებულოა")
    private String productName;

    @Min(value = 1, message = "რაოდენობა უნდა იყოს მინიმუმ 1")
    private int quantity;

    @NotNull(message = "ფასი სავალდებულოა")
    @Positive(message = "ფასი უნდა იყოს დადებითი რიცხვი")
    private BigDecimal price;
}