package com.example.vladyslav.controller;

import com.example.vladyslav.dto.ReviewDTO;
import com.example.vladyslav.requests.ReviewCreateRequest;
import com.example.vladyslav.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<Page<ReviewDTO>> list(@PathVariable String doctorId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(reviewService.listForDoctor(doctorId, page, size));
    }

    @PostMapping()
    public ResponseEntity<ReviewDTO> createReview(@RequestBody ReviewCreateRequest request){
        return new ResponseEntity<>(reviewService.createReviewForDoctor(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable String reviewId){
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
