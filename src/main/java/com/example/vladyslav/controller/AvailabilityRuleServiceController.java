package com.example.vladyslav.controller;

import com.example.vladyslav.model.AvailabilityRule;
import com.example.vladyslav.service.AvailabilityRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availability-rules")
@RequiredArgsConstructor
public class AvailabilityRuleServiceController {

    private final AvailabilityRuleService service;

    // List all rules for doctor
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    @GetMapping("/list/{doctorId}")
    public ResponseEntity<List<AvailabilityRule>> list (@PathVariable String doctorId){
        return ResponseEntity.ok(service.list(doctorId));
    }

    // Update a rule (create or update by doctorId +  dayOfWeek)
    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    @PutMapping
    public ResponseEntity<AvailabilityRule> upsert(@Valid @RequestBody AvailabilityRule rule){
        return ResponseEntity.ok(service.upsert(rule));
    }
}
