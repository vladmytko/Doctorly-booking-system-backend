package com.example.vladyslav.service;

import com.example.vladyslav.awsS3.AwsS3Service;
import com.example.vladyslav.dto.ClinicDTO;
import com.example.vladyslav.dto.ReviewDTO;
import com.example.vladyslav.exception.NotFoundException;
import com.example.vladyslav.exception.OurException;
import com.example.vladyslav.model.Clinic;
import com.example.vladyslav.model.Doctor;
import com.example.vladyslav.model.Review;
import com.example.vladyslav.model.User;
import com.example.vladyslav.model.enums.Role;
import com.example.vladyslav.repository.ClinicRepository;
import com.example.vladyslav.repository.DoctorRepository;
import com.example.vladyslav.repository.UserRepository;
import com.example.vladyslav.requests.ClinicRegisterRequest;
import org.springframework.data.geo.Point;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;

    private final UserRepository userRepository;

    private final DoctorRepository doctorRepository;

    private final PasswordEncoder passwordEncoder;

    private final AwsS3Service awsS3Service;

    private final GeocodingService geocodingService;


    /**
     * Register new Clinic
     * @param request
     * @return
     */
    public ClinicDTO registerClinic(ClinicRegisterRequest request){

        // 1) Create User entity

        String normalizedEmail = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        if(normalizedEmail.isEmpty() || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        if(userRepository.existsByEmail(normalizedEmail))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");

        User user = User.builder()
                .email(normalizedEmail)
                .role(Role.CLINIC)
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        userRepository.save(user);

        // 2) Upload image to S3 and generate URL
        String imageUrl = awsS3Service.saveImageToS3(request.getImage());

        // 3) Create clinic

        Clinic clinic = Clinic.builder()
                .email(request.getEmail())
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .postCode(request.getPostCode())
                .phoneNumber(request.getPhoneNumber())
                .imageUrl(imageUrl)
                .description(request.getDescription())
                .build();

        // 3) Geocode address -> latitude/longitude
        GeoJsonPoint location = geocodingService.geocodeAddress(request.getAddress(), request.getCity(), request.getPostCode());
        if(location != null){
            clinic.setLocation(location);
        }

        clinicRepository.save(clinic);

        return toDTO(clinic);

    }


    /**
     * Finds clinic by name
     * @param name
     * @return Optional ClinicDTO object
     */
    public Optional<ClinicDTO> findClinicByName(String name){
        Clinic clinic = clinicRepository.findByName(name).orElseThrow(()-> new OurException("Clinic with name " + name + " not found"));
        return Optional.ofNullable(toDTO(clinic));
    }


    /**
     * Finds all clinics whose name contains the given keyword (case-insensitive).
     * Useful for search or autocomplete functionality.
     *
     * @param keyword part of the clinic name to search for
     * @return list of matching ClinicDTO objects
     */
    public List<ClinicDTO> findByNameContainingIgnoreCase(String keyword){
        List<Clinic> clinicList = clinicRepository.findByNameContainingIgnoreCase(keyword);
        return clinicList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     * Find clinic by doctor id that works there
     * @param doctorId
     * @return list of matching Clinic DTO objects
     */
    public List<ClinicDTO> findByDoctorId(String doctorId){
        List<Clinic> clinicList = clinicRepository.findByDoctors_Id(doctorId);
        return clinicList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find clinic by doctor's find name and last name
     * @param firstName
     * @param lastName
     * @return list of matching Clinic DTO objects
     */
    public List<ClinicDTO> findByDoctorFirstNameAndLastName(String firstName, String lastName){
        List<Clinic> clinicList = clinicRepository.findByDoctorFirstNameAndLastName(firstName,lastName);
        return clinicList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     *
     * @param text
     * @return
     */
    public Page<ClinicDTO> searchByText(int page, int size, String text) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Clinic> clinicList = clinicRepository.searchByText(pageable, text);
        return clinicList.map(this::toDTO);
    }

    /**
     *
     * @param rating
     * @return
     */
    public Page<ClinicDTO> findByAverageRatingGreaterThanEqual(int page, int size, Float rating){
        Pageable pageable = PageRequest.of(page, size);
        Page<Clinic> clinicList = clinicRepository.findByAverageRatingGreaterThanEqual(pageable, rating);
        return clinicList.map(this::toDTO);
    }


    /**
     *
     * @param clinicId
     * @return
     */
    public ClinicDTO getClinicById(String clinicId){
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(()-> new NotFoundException("Clinic not found with id: " + clinicId));

        return toDTO(clinic);
    }


    /**
     *
     * @param size
     * @param page
     * @return
     */
    public Page<ClinicDTO> getAllClinics(int size, int page){
        Pageable pageable = PageRequest.of(page,size);
        return clinicRepository.findAll(pageable).map(this::toDTO);
    }


    /**
     *
     * @param clinicId
     */
    public void deleteClinic(String clinicId){
        clinicRepository.deleteById(clinicId);
    }

    /**
     * Find clinic near a given geographic location within the given radius (in kilometers).
     *
     * @param latitude of the center point
     * @param longitude of the center point
     * @param radiusKm search radius in kilometers
     * @return list of nearby ClinicDTO objects
     */
    public Page<ClinicDTO> findClinicsNear(int page, int size, double latitude, double longitude, double radiusKm){
        Pageable pageable = PageRequest.of(page, size);
        Point point = new Point(longitude, latitude); // (x, y) = (lon, lat)
        Distance distance = new Distance(radiusKm, Metrics.KILOMETERS);

        Page<Clinic> clinicList = clinicRepository.findByLocationNear(pageable, point, distance);
        return clinicList.map(this::toDTO);
    }

    public void addDoctorToClinic(String clinicId, String doctorId){
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(()-> new NotFoundException("Clinic not found with id:" + clinicId));

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(()-> new NotFoundException("Doctor not found with id: " + doctorId));

        doctor.setClinic(clinic);
        doctorRepository.save(doctor);
    }





    private ClinicDTO toDTO(Clinic clinic){
        return ClinicDTO.builder()
                .id(clinic.getId())
                .email(clinic.getEmail())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .city(clinic.getCity())
                .postCode(clinic.getPostCode())
                .phoneNumber(clinic.getPhoneNumber())
                .imageUrl(clinic.getImageUrl())
                .description(clinic.getDescription())
                .reviewDTOS(clinic.getReviews() == null ? null :
                        clinic.getReviews().stream()
                                .map(this::toReviewDTO).collect(Collectors.toList()))
                .averageRating(clinic.getAverageRating())
                .createdAt(clinic.getCreatedAt())
                .updatedAt(clinic.getUpdatedAt())
                .latitude(clinic.getLocation() != null ? clinic.getLocation().getY() : null)
                .longitude(clinic.getLocation() != null ? clinic.getLocation().getX() : null)
                .build();
    }



    private ReviewDTO toReviewDTO(Review review){
        if(review == null) return null;

        return ReviewDTO.builder()
                .id(review.getId())
                .comment(review.getComment())
                .rating(review.getRating())
                .patientId(review.getPatient().getId())
                .doctorId(review.getDoctor().getId())
                .clinicId(review.getClinic().getId())
                .createdAt(review.getCreatedAt())
                .build();
    }



}
