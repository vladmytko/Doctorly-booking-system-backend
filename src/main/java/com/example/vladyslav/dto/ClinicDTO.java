package com.example.vladyslav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClinicDTO {

    private String id;
    private String email;
    private String name;
    private String address;
    private String city;
    private String postCode;
    private String phoneNumber;
    private String description;
    private String imageUrl;
    private List<ReviewDTO> reviewDTOS = new ArrayList<>();
    private Float averageRating;
    private double latitude;
    private double longitude;
    private Instant createdAt;
    private Instant updatedAt;

}
