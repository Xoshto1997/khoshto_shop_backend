package com.example.ecommerce_project.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "პროდუქტის ID სავალდებულოა")
    private Long productId;

    @NotNull(message = "რეიტინგის მითითება სავალდებულოა")
    @Min(value = 1, message = "რეიტინგი უნდა იყოს მინიმუმ 1")
    @Max(value = 5, message = "რეიტინგი არ შეიძლება იყოს 5-ზე მეტი")
    private Integer rating;

    @NotBlank(message = "კომენტარის დაწერა სავალდებულოა")
    @Size(min = 3, max = 500, message = "კომენტარი უნდა იყოს 3-დან 500 სიმბოლომდე")
    private String comment;
}