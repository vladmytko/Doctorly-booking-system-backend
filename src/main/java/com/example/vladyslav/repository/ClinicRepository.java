package com.example.vladyslav.repository;

import com.example.vladyslav.model.Clinic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClinicRepository extends MongoRepository<Clinic, String> {

    // Find by name
    Optional<Clinic> findByName(String name);
    List<Clinic> findByNameContainingIgnoreCase(String keyword);

    // Find by doctor
    List<Clinic> findByDoctors_Id(String doctorId);
    List<Clinic> findByDoctorFirstNameAndLastName(String firstName, String lastName);

    // Geo search
    Page<Clinic> findByLocationNear(Pageable pageable, Point point, Distance distance);

    // Text search (after creating Mongo text index)
    @Query("{ $text: { $search: ?0 } }")
    Page<Clinic> searchByText(Pageable pageable, String text);

    // Find by minimum rating
    Page<Clinic> findByAverageRatingGreaterThanEqual(Pageable pageable, Float rating);
}
