package com.example.vladyslav.service;

import com.example.vladyslav.dto.PatientDTO;
import com.example.vladyslav.exception.NotFoundException;
import com.example.vladyslav.model.Patient;
import com.example.vladyslav.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;


    public PatientDTO getPatientByPatientId(String patientId){

        Patient patient = patientRepository.findByUserId(patientId)
                .orElseThrow(()-> new NotFoundException("Patient not found for patientId: " + patientId));

        return toDto(patient);
    }



    public PatientDTO getPatientByUserId(String userId){

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(()-> new NotFoundException("Patient not found for userId: " + userId));

        return toDto(patient);
    }



    public Page<PatientDTO> getAllPatients(int page, int size){

        Pageable pageable = PageRequest.of(page,size);
        return patientRepository.findAll(pageable).map(this::toDto);
    }

    public Page<PatientDTO> getPatientByLastName(String lastName, int page, int size){

        Pageable pageable = PageRequest.of(page,size);
        return patientRepository.findByLastNameContainingIgnoreCase(lastName, pageable).map(this::toDto);
    }


    private PatientDTO toDto(Patient patient){
        return PatientDTO.builder()
                .id(patient.getId())
                .firstName(patient.getFirstName())
                .lastName(patient.getLastName())
                .phoneNumber(patient.getPhoneNumber())
                .email(patient.getEmail())
                .dateOfBirth(patient.getDateOfBirth())
                .userId(patient.getUser().getId())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .imageUrl(patient.getImageUrl())
                .role(patient.getUser().getRole())
                .build();
    }
}
