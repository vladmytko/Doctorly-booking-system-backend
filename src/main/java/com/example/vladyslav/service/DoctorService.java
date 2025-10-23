package com.example.vladyslav.service;

import com.example.vladyslav.awsS3.AwsS3Service;
import com.example.vladyslav.dto.DoctorDTO;
import com.example.vladyslav.exception.NotFoundException;
import com.example.vladyslav.model.Doctor;
import com.example.vladyslav.model.Speciality;
import com.example.vladyslav.model.User;
import com.example.vladyslav.model.enums.LanguageCode;
import com.example.vladyslav.model.enums.Role;
import com.example.vladyslav.repository.DoctorRepository;
import com.example.vladyslav.repository.SpecialityRepository;
import com.example.vladyslav.repository.UserRepository;
import com.example.vladyslav.requests.DoctorRegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SpecialityRepository specialityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AwsS3Service awsS3Service;

    public Doctor createDoctor(DoctorRegisterRequest request) {

        // 1) Resolve Speciality if provided
        Speciality resolvedSpeciality = null;
        if (request.getSpeciality() != null) {
            Speciality incoming = request.getSpeciality();

            // Prefer id if present
            if (incoming.getId() != null && !incoming.getId().isBlank()) {
                resolvedSpeciality = specialityRepository.findById(incoming.getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Speciality not found for id: " + incoming.getId()));
            }
            // Otherwise try by title (create if not exists)
            else if (incoming.getTitle() != null && !incoming.getTitle().isBlank()) {
                resolvedSpeciality = specialityRepository.findByTitle(incoming.getTitle())
                        .orElseGet(() -> specialityRepository.save(
                                Speciality.builder().title(incoming.getTitle()).build()
                        ));
            }
        }

        // 2) Normalise and validate languages
        List<LanguageCode> validLanguages = new ArrayList<>();
        if (request.getLanguages() != null && !request.getLanguages().isEmpty()) {
            for (LanguageCode lang : request.getLanguages()) {
                if (lang == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Language cannot be null");
                }
                validLanguages.add(lang);
            }
            validLanguages = validLanguages.stream().distinct().collect(Collectors.toList());
        }

        // 3) Create backing User entity

        String normalizedEmail = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        if (normalizedEmail.isEmpty() || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        if (userRepository.existsByEmail(normalizedEmail))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");

        User user = User.builder()
                .email(request.getEmail())
                .role(Role.DOCTOR)
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        userRepository.save(user);


        // 3) Upload image to S3 and generate URL
        String imageUrl = awsS3Service.saveImageToS3(request.getImage());


        // 4) Attach resolved speciality & user, then save doctor
        Doctor doctor = Doctor.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .speciality(resolvedSpeciality)
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .bio(request.getBio())
                .languages(request.getLanguages())
                .imageUrl(imageUrl)
                .consultationFee(request.getConsultationFee())
                .user(user)
                .languages(validLanguages)
                .build();



        if (resolvedSpeciality != null) {
            doctor.setSpeciality(resolvedSpeciality);
        }

        return doctorRepository.save(doctor);
    }

    public DoctorDTO getDoctorById(String doctorId){
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(()-> new NotFoundException("Doctor not found for doctorId " + doctorId));
        return toDTO(doctor);
    }

    public Page<DoctorDTO> findBySpecialityTitle(String title, Pageable p){
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "specialityTitle is required");
        }
        final String trimmed = title.trim();

        // Resolve the Speciality by title first (avoid querying DBRef fields directly in Mongo)
        Speciality speciality = specialityRepository.findByTitle(trimmed)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Speciality not found for title: " + trimmed));

        return doctorRepository
                .findDoctorsBySpecialityId(speciality.getId(), p)
                .map(this::toDTO);
    }

    public Page<DoctorDTO> findDoctorsBySpecialityId(String id, Pageable p){
        return doctorRepository.findDoctorsBySpecialityId(id, p)
                .map(this::toDTO);
    }

    public Page<DoctorDTO> getAllDoctors(int page, int size){

        Pageable pageable = PageRequest.of(page,size);
        return doctorRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<DoctorDTO> getDoctorByLastName(String lastName, int page, int size){

        Pageable pageable = PageRequest.of(page,size);
        return doctorRepository.findByLastNameContainingIgnoreCase(lastName, pageable).map(this::toDTO);
    }

    private DoctorDTO toDTO(Doctor doctor){
        return DoctorDTO.builder()
                .id(doctor.getId())
                .userId(doctor.getUser().getId())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .email(doctor.getEmail())
                .specialityId(doctor.getSpeciality().getId())
                .phoneNumber(doctor.getPhoneNumber())
                .dateOfBirth(doctor.getDateOfBirth())
                .bio(doctor.getBio())
                .reviews(doctor.getReviews())
                .averageRating(doctor.getAverageRating())
                .photoUrl(doctor.getImageUrl())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .consultationFee(doctor.getConsultationFee())
                .languages(doctor.getLanguages())
                .build();
    }




}
