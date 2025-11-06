package com.example.edu_platform.web;

import com.example.edu_platform.service.CourseReviewService;
import com.example.edu_platform.service.dto.review_dtos.CourseRatingDto;
import com.example.edu_platform.service.dto.review_dtos.ReviewDto;
import com.example.edu_platform.service.dto.review_dtos.UpsertReviewCmd;
import com.example.edu_platform.web.dto.UpsertReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final CourseReviewService reviewService;

    @PostMapping("/courses/{courseId}/reviews")
    public ResponseEntity<ReviewDto> upsert(@PathVariable Long courseId,
                                            @RequestParam Long studentId,
                                            @RequestBody @Valid UpsertReviewRequest req) {
        ReviewDto res = reviewService.upsertReview(
                courseId, studentId, new UpsertReviewCmd(req.rating(), req.comment())
        );
        return ResponseEntity.created(URI.create("/api/reviews/" + res.id())).body(res);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> delete(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/reviews")
    public ResponseEntity<List<ReviewDto>> list(@PathVariable Long courseId) {
        return ResponseEntity.ok(reviewService.getReviewsForCourse(courseId));
    }

    @GetMapping("/courses/{courseId}/rating")
    public ResponseEntity<CourseRatingDto> rating(@PathVariable Long courseId) {
        return ResponseEntity.ok(reviewService.getCourseRating(courseId));
    }
}
