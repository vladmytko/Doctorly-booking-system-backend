package com.example.vladyslav.controller;

import com.example.vladyslav.dto.DaySlotsDTO;
import com.example.vladyslav.model.AvailabilityRule;
import com.example.vladyslav.service.AvailabilityRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability-rules")
@RequiredArgsConstructor
public class AvailabilityRuleServiceController {

    private final AvailabilityRuleService service;

    // List all rules for doctor
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    @GetMapping("/list/{doctorId}")
    public ResponseEntity<List<AvailabilityRule>> findByDoctorId (@PathVariable String doctorId){
        return ResponseEntity.ok(service.findByDoctorId(doctorId));
    }

    // Update a rule (create or update by doctorId +  dayOfWeek)
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    @PutMapping
    public ResponseEntity<AvailabilityRule> upsert(@Valid @RequestBody AvailabilityRule rule){
        return ResponseEntity.ok(service.upsert(rule));
    }

    @GetMapping("/doctors/{doctorId}/availability")
    public List<DaySlotsDTO> availability(@PathVariable String doctorId,
                                          @RequestParam LocalDate from,
                                          @RequestParam LocalDate to) {
        return service.getAvailability(doctorId, from, to);
    }
}
