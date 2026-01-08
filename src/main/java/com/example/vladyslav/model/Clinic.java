package com.example.vladyslav.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "clinics")
public class Clinic {

    @Id
    private String id;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @TextIndexed(weight = 2f)
    private String name;

    @TextIndexed
    private String address;

    @Indexed
    private String city;

    @Indexed
    private String postCode;

    private String phoneNumber;

    private String imageUrl;

    @TextIndexed
    private String description;

//    @DBRef(lazy = true)
//    @Builder.Default
//    private List<Review> reviews = new ArrayList<>();

    private Float averageRating;

    @DBRef
    private User user;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location; // longitude, latitude

    public void setLocationFromCoordinates(double lon, double lat){
        this.location = new GeoJsonPoint(lon, lat);
    }
}
