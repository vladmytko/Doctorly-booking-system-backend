package com.example.vladyslav.controller;

import com.example.vladyslav.dto.PatientDTO;
import com.example.vladyslav.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {


    private final PatientService patientService;

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<PatientDTO> getPatientByUserId(@PathVariable String userId){
        PatientDTO dto = patientService.getPatientByUserId(userId); // throws NotFoundException
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/by-patient/{patientId}")
    public ResponseEntity<PatientDTO> getPatientByPatientId(@PathVariable String patientId){
        PatientDTO dto = patientService.getPatientByPatientId(patientId); // throws NotFoundException
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<PatientDTO>> getAllPatients(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size){
        Page<PatientDTO> dto = patientService.getAllPatients(page, size);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/name/{lastName}")
    public ResponseEntity<Page<PatientDTO>> getAllPatients(@PathVariable String lastName,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size){
        Page<PatientDTO> dto = patientService.getPatientByLastName(lastName, page, size);
        return ResponseEntity.ok(dto);
    }
}
