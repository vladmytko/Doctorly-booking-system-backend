package com.example.vladyslav.controller;

import com.example.vladyslav.dto.ClinicDTO;
import com.example.vladyslav.dto.DoctorDTO;
import com.example.vladyslav.requests.ClinicRegisterRequest;
import com.example.vladyslav.service.ClinicService;
import com.example.vladyslav.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
@Validated
public class ClinicController {

    private final ClinicService service;

    private final DoctorService doctorService;

    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ClinicDTO> registerClinic(@Valid @ModelAttribute ClinicRegisterRequest request){
        return new ResponseEntity<>(service.registerClinic(request), HttpStatus.CREATED);
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<Optional<ClinicDTO>> findClinicByName(@PathVariable String name){
        return new ResponseEntity<>(service.findClinicByName(name), HttpStatus.FOUND);
    }

    @GetMapping("/by-keyword/{keyword}")
    public ResponseEntity<List<ClinicDTO>> findByNameContainingIgnoreCase(@PathVariable String keyword){
        return new ResponseEntity<>(service.findByNameContainingIgnoreCase(keyword), HttpStatus.FOUND);
    }

    @GetMapping("/by-doctor-id/{doctorId}")
    public ResponseEntity<List<ClinicDTO>> findByDoctorId(@PathVariable String doctorId){
        return new ResponseEntity<>(service.findByDoctorId(doctorId), HttpStatus.FOUND);
    }

    @GetMapping("/by-doctor-name/{firstName}/{lastName}")
    public ResponseEntity<List<ClinicDTO>> findByDoctorFirstNameAndLastName(@PathVariable String firstName, @PathVariable String lastName){
        return new ResponseEntity<>(service.findByDoctorFirstNameAndLastName(firstName,lastName), HttpStatus.FOUND);
    }

    @GetMapping("/by-text/{text}")
    public ResponseEntity<Page<ClinicDTO>> searchByText(@PathVariable String text,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size){
        return new ResponseEntity<>(service.searchByText(page, size, text), HttpStatus.FOUND);
    }

    @GetMapping("/by-average-rating/{rating}")
    public ResponseEntity<Page<ClinicDTO>> findByAverageRatingGreaterThanEqual(@PathVariable Float rating,
                                                                               @RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "20") int size){
        return new ResponseEntity<>(service.findByAverageRatingGreaterThanEqual(page, size, rating), HttpStatus.FOUND);
    }

    @GetMapping("/by-id/{clinicId}")
    public ResponseEntity<ClinicDTO> getClinicById(@PathVariable String clinicId){
        return new ResponseEntity<>(service.getClinicById(clinicId), HttpStatus.FOUND);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ClinicDTO>> getAllClinics( @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size){
        return new ResponseEntity<>(service.getAllClinics(page, size), HttpStatus.FOUND);
    }

    @DeleteMapping("/{clinicId}")
    public ResponseEntity<Void> deleteClinic(@RequestParam String clinicId){
       service.deleteClinic(clinicId);
       return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/doctors/search")
    @PreAuthorize("hasRole('CLINIC')")
    public ResponseEntity<List<DoctorDTO>> searchDoctors(
            @RequestParam String query) {
        // e.g. search by name or email in DoctorRepository
        return ResponseEntity.ok(doctorService.searchByNameOrEmail(query));
    }




    @GetMapping("/near")
    public ResponseEntity<Page<ClinicDTO>> getClinicsNear(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size

    ) {
        return new ResponseEntity<>(service.findClinicsNear(page, size, lat, lon, radiusKm), HttpStatus.FOUND);
    }
}
