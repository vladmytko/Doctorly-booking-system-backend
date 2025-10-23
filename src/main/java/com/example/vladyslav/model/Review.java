package com.example.vladyslav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    private String id;

    private String comment;

    private Float rating; // e.g., 4.5

    @DBRef
    private Patient patient; // who wrote the review

    @DBRef
    private Doctor doctor; // nullable of it's for clinic

    @DBRef
    private Clinic clinic; // nullable if it's for doctor

    @CreatedDate
    private Instant createdAt;


}
