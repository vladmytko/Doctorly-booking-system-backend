package com.example.vladyslav.controller;

import com.example.vladyslav.model.Speciality;
import com.example.vladyslav.service.SpecialityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialities")
public class SpecialityController {

    @Autowired
    private SpecialityService specialityService;

    @GetMapping
    public List<Speciality> getAll(){
        return specialityService.getAll();
    }

    @PostMapping
    public ResponseEntity<Speciality> createSpeciality(@Valid @RequestBody Speciality speciality){
        Speciality created = specialityService.createSpeciality(speciality);

        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/id/{specialityId}")
    public ResponseEntity<Speciality> getSpecialityById(@PathVariable String specialityId){
        Speciality speciality = specialityService.getSpecialityById(specialityId);
        return new ResponseEntity<>(speciality, HttpStatus.OK);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<Speciality> getSpecialityByTitle(@PathVariable String title){
        Speciality speciality = specialityService.getSpecialityByTitle(title);
        return new ResponseEntity<>(speciality, HttpStatus.OK);
    }

    /**
     * Type-ahead search endpoint.
     * Example: GET /api/specialities/search?q=der&limit=8
     * - Enforces 3+ chars in the service.
     * - Caps limit for performance.
     * - Sorted ascending by title for a stable suggestions list.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Speciality>> search(
            @RequestParam("q") String q,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(specialityService.searchByPrefix(q, limit));
    }
}
