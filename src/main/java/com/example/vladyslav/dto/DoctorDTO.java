package com.example.vladyslav.dto;

import com.example.vladyslav.model.Review;
import com.example.vladyslav.model.enums.LanguageCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDTO {

    private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String specialityId;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String bio;
    private Float averageRating;
    private List<Review> reviews;
    private String photoUrl;
    private List<LanguageCode> languages;
    private Instant createdAt;
    private Instant updatedAt;
    private String imageUrl;

    @Builder.Default
    private int consultationFee = 0;

}
