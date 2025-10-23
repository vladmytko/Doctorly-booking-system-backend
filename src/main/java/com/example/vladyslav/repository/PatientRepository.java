package com.example.vladyslav.repository;

import com.example.vladyslav.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PatientRepository extends MongoRepository<Patient, String> {
    Optional<Patient> findByUserId(String userId);
    Page<Patient> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
}
