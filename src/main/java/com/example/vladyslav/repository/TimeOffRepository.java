package com.example.vladyslav.repository;

import com.example.vladyslav.model.TimeOff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface TimeOffRepository extends MongoRepository<TimeOff, String > {

    List<TimeOff> findByDoctorIdAndStartLessThanEqualAndEndGreaterThanEqual(String doctorId, Instant end, Instant start);

    Page<TimeOff> findByDoctorIdAndStartAfter(String doctorId, LocalDate start, Pageable pageable);
}
