package com.example.vladyslav.repository;


import com.example.vladyslav.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DoctorRepository extends MongoRepository<Doctor, String> {
    Page<Doctor> findDoctorsBySpecialityId(String specialityId, Pageable pageable);
    Page<Doctor> findBySpeciality_TitleIgnoreCase(String title, Pageable pageable);
    Page<Doctor> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable);
}
