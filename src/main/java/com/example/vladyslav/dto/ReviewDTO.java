package com.example.vladyslav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private String id;
    private String comment;
    private Float rating;
    private String patientId;
    private String doctorId;
    private String clinicId;
    private Instant createdAt;
}
