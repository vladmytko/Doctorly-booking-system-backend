package com.example.vladyslav.controller;

import com.example.vladyslav.model.TimeOff;
import com.example.vladyslav.service.TimeOffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/time-offs")
@RequiredArgsConstructor
public class TimeOffController {

    private final TimeOffService service;

    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TimeOff> create(@Valid @RequestBody TimeOff timeOff){
        return ResponseEntity.ok(service.create(timeOff));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<TimeOff> get(@PathVariable String id){
        return ResponseEntity.ok(service.get(id));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id){
        service.delete(id);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','DOCTOR')")
    @GetMapping("by-doctor/{doctorId}")
    public ResponseEntity<Page<TimeOff>> getTimeOffByDoctorId(@PathVariable String doctorId,
                                                              @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size){


        Page<TimeOff> timeOffs = service.getTimeOffByDoctorId(doctorId, start, page, size);
        return ResponseEntity.ok(timeOffs);
    }





}
