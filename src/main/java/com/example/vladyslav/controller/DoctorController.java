package com.example.vladyslav.controller;

import com.example.vladyslav.dto.DoctorDTO;
import com.example.vladyslav.requests.DoctorRegisterRequest;
import com.example.vladyslav.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Validated
public class DoctorController {


    private final DoctorService doctorService;


    @GetMapping("/all")
    public ResponseEntity<Page<DoctorDTO>> getAllDoctors(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size){
        Page<DoctorDTO> dto = doctorService.getAllDoctors(page, size);
        return ResponseEntity.ok(dto);
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
            )
    public ResponseEntity<DoctorDTO> createDoctor(@Valid @ModelAttribute DoctorRegisterRequest request){
        return new ResponseEntity<>(doctorService.createDoctor(request), HttpStatus.CREATED);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable String id){
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

//    @GetMapping
//    public ResponseEntity<Page<DoctorDTO>> getDoctorsByClinicAndRating(@RequestParam(required = false) String clinicId,
//                                                                       @RequestParam(required = false) @Min(0) Integer minRating,
//                                                                       @PageableDefault(size = 20, sort = "lastName") Pageable pageable){
//        return ResponseEntity.ok(doctorService.search(clinicId, minRating, pageable));
//    }

    @GetMapping("/last-name/{lastName}")
    public ResponseEntity<Page<DoctorDTO>> getDoctorByLastName(@PathVariable String lastName,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size){
        Page<DoctorDTO> dto = doctorService.getDoctorByLastName(lastName, page, size);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/speciality")
    public Page<DoctorDTO> findDoctorsBySpeciality(
            @RequestParam(required = false) String specialityId,
            @RequestParam(required = false) String specialityTitle,
            @PageableDefault(size = 20) Pageable pageable
            )   {
        if(specialityId != null) return doctorService.findDoctorsBySpecialityId(specialityId, pageable);
        if(specialityTitle != null && !specialityTitle.isBlank()) return doctorService.findBySpecialityTitle(specialityTitle, pageable);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide specialityId or speciality title");
    }
}
