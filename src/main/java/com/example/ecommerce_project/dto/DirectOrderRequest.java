package com.example.ecommerce_project.dto;

import com.example.ecommerce_project.constants.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class DirectOrderRequest {

    @NotBlank(message = "ელფოსტის მითითება სავალდებულოა")
    @Email(message = "გთხოვთ მიუთითოთ ვალიდური ელფოსტის მისამართი")
    private String userEmail;

    @NotBlank(message = "სახელი და გვარი სავალდებულოა")
    private String customerName;

    @NotBlank(message = "ტელეფონის ნომერი სავალდებულოა")
    private String phoneNumber;

    @NotBlank(message = "ქალაქი სავალდებულოა")
    private String city;

    @NotBlank(message = "მისამართი სავალდებულოა")
    private String address;

    private String notes;

    private String companyName;
    private String taxId;
    private String companyAddress;

    @NotNull(message = "გადახდის მეთოდის არჩევა სავალდებულოა")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "შეკვეთა უნდა შეიცავდეს სულ მცირე ერთ პროდუქტს")
    private List<@Valid OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {

        private Long productId;

        @NotBlank(message = "პროდუქტის დასახელება სავალდებულოა")
        private String productName;

        @NotNull(message = "რაოდენობის მითითება სავალდებულოა")
        @Min(value = 1, message = "რაოდენობა უნდა იყოს მინიმუმ 1")
        private Integer quantity;

        @NotNull(message = "ფასი სავალდებულოა")
        @Positive(message = "ფასი უნდა იყოს დადებითი რიცხვი")
        private Double price;
    }
}