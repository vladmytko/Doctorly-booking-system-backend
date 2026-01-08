package com.example.vladyslav.repository;

import com.example.vladyslav.model.Appointment;
import com.example.vladyslav.model.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    Page<Appointment> findByDoctorIdOrderByStartDesc(String doctorId, Pageable pageable);

    Page<Appointment> findByClinicIdOrderByStartDesc(String clinicId, Pageable pageable);

    Page<Appointment> findByDoctorIdAndStartBetween(String doctorId, Instant from, Instant to, Pageable pageable);

    Page<Appointment> findByPatientIdOrderByStartDesc(String patientId, Pageable pageable);

    Page<Appointment> findByClinicIdAndStartBetween(String clinicId, Instant from, Instant to, Pageable pageable);

    Optional<Appointment> findByDoctorIdAndStart(String doctorId, Instant start);

    List<Appointment> findByDoctorIdAndStartLessThanAndEndGreaterThan(String doctorId, Instant endExclusive, Instant startExclusive);

    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

    @Query("{ 'doctorId': ?0, 'status': ?1, 'start': { $gte: ?2 }, 'end': { $lte: ?3 } }")
    Page<Appointment> findByDoctorIdAndStatusBetween(String doctorId, AppointmentStatus status, Instant from, Instant to, Pageable pageable);

    boolean existsByDoctorIdAndPatientIdAndStatusAndEndBefore(String doctorId, String patientId, AppointmentStatus status, Instant end);

}
