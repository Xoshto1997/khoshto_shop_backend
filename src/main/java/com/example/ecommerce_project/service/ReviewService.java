package com.example.ecommerce_project.service;

import com.example.ecommerce_project.dto.CreateReviewRequest;
import com.example.ecommerce_project.dto.ReviewResponse; // 👈 დაემატა DTO-ს იმპორტი
import com.example.ecommerce_project.model.Review;
import com.example.ecommerce_project.model.User;
import com.example.ecommerce_project.repository.ReviewRepository;
import com.example.ecommerce_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public List<ReviewResponse> getReviewsByProductId(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);

        return reviews.stream().map(review -> {
            String displayName = "მომხმარებელი";

            if (review.getUser() != null) {
                if (review.getUser().getUsername() != null && !review.getUser().getUsername().isBlank()) {
                    displayName = review.getUser().getUsername();
                } else if (review.getUser().getEmail() != null) {
                    displayName = review.getUser().getEmail();
                }
            }

            return new ReviewResponse(
                    review.getId(),
                    review.getProductId(),
                    review.getRating(),
                    review.getComment(),
                    review.getCreatedAt(),
                    displayName
            );
        }).collect(Collectors.toList());
    }

    public Review addReview(CreateReviewRequest request, String currentUserName) {
        Review review = new Review();
        review.setProductId(request.getProductId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        if (currentUserName != null && !currentUserName.isBlank()) {
            User user = userRepository.findByEmail(currentUserName).orElse(null);
            review.setUser(user);
        }

        return reviewRepository.save(review);
    }
}