package com.example.vladyslav.service;

import com.example.vladyslav.exception.OurException;
import com.example.vladyslav.model.AvailabilityRule;
import com.example.vladyslav.repository.AvailabilityRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityRuleService {

    private final AvailabilityRuleRepository repository;

    public List<AvailabilityRule> list (String doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    public AvailabilityRule upsert(AvailabilityRule rule) {
        validate(rule);

        return repository.findByDoctorIdAndDayOfWeek(rule.getDoctorId(), rule.getDayOfWeek())
                .map(existing -> {
                    existing.setStart(rule.getStart());
                    existing.setEnd(rule.getEnd());
                    existing.setSlotMinutes(rule.getSlotMinutes());
                    existing.setBufferBeforeMinutes(rule.getBufferBeforeMinutes());
                    existing.setBufferAfterMinutes(rule.getBufferAfterMinutes());
                    return repository.save(existing);
                })
                .orElseGet(()-> repository.save(rule));
    }

    private void validate(AvailabilityRule rule){
        if(rule.getDoctorId() == null) throw new OurException("Doctor is required.");
        if(rule.getDayOfWeek() < 1 || rule.getDayOfWeek() > 7) throw new OurException("dayOfWeek must be between 1..7");

        LocalTime s = rule.getStart(), e = rule.getEnd();
        if(s == null || e == null || !e.isAfter(s)) throw new OurException("Invalid start/end for availability.");
        if(rule.getSlotMinutes() < 0) throw new OurException("slotMinutes cannot be negative.");
    }
}
