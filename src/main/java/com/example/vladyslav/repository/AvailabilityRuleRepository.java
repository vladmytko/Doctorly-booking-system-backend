package com.example.vladyslav.repository;

import com.example.vladyslav.model.AvailabilityRule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AvailabilityRuleRepository extends MongoRepository<AvailabilityRule, String> {

    List<AvailabilityRule> findByDoctorId(String doctorId);

    Optional<AvailabilityRule> findByDoctorIdAndDayOfWeek(String doctorId, int dayOfWeek);
}
