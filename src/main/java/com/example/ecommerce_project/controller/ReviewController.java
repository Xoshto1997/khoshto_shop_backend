package com.example.ecommerce_project.controller;

import com.example.ecommerce_project.dto.CreateReviewRequest;
import com.example.ecommerce_project.dto.ReviewResponse;
import com.example.ecommerce_project.model.Review;
import com.example.ecommerce_project.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> getProductReviews(@PathVariable Long productId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@Valid @RequestBody CreateReviewRequest request, Principal principal) {
        String currentUserIdentifier = (principal != null) ? principal.getName() : null;
        Review createdReview = reviewService.addReview(request, currentUserIdentifier);
        return ResponseEntity.ok(createdReview);
    }
}