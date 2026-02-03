package com.example.vladyslav.service;

import com.example.vladyslav.awsS3.AwsS3Service;
import com.example.vladyslav.dto.DoctorDTO;
import com.example.vladyslav.dto.ReviewDTO;
import com.example.vladyslav.exception.NotFoundException;
import com.example.vladyslav.model.Doctor;
import com.example.vladyslav.model.Speciality;
import com.example.vladyslav.model.User;
import com.example.vladyslav.model.enums.AppointmentType;
import com.example.vladyslav.model.enums.LanguageCode;
import com.example.vladyslav.model.enums.Role;
import com.example.vladyslav.repository.DoctorRepository;
import com.example.vladyslav.repository.ReviewRepository;
import com.example.vladyslav.repository.SpecialityRepository;
import com.example.vladyslav.repository.UserRepository;
import com.example.vladyslav.requests.DoctorRegisterRequest;
import com.example.vladyslav.search.DoctorSearchCriteria;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
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
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AwsS3Service awsS3Service;

    @Autowired
    private MongoTemplate mongoTemplate;

    public DoctorDTO createDoctor(DoctorRegisterRequest request) {

        // 1) Resolve Speciality if provided


        if (request.getSpecialityTitle() != null && !request.getSpecialityTitle().isBlank()) {
                Speciality speciality = specialityRepository.findByTitle(request.getSpecialityTitle())
                        .orElseThrow(()-> new NotFoundException("Speciality not found with title " + request.getSpecialityTitle()));}


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
                .email(normalizedEmail)
                .role(Role.DOCTOR)
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        userRepository.save(user);


        // 3) Upload image to S3 and generate URL
        String imageUrl = awsS3Service.saveImageToS3(request.getImage());

        Speciality speciality = specialityRepository.findByTitle(request.getSpecialityTitle()).orElseThrow(() -> new NotFoundException("Speciality not found by title " + request.getSpecialityTitle()));

        // 4) Attach resolved speciality & user, then save doctor
        Doctor doctor = Doctor.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .speciality(speciality)
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .bio(request.getBio())
                .languages(request.getLanguages())
                .imageUrl(imageUrl)
                .consultationFee(request.getConsultationFee())
                .user(user)
                .languages(validLanguages)
                .build();

        doctorRepository.save(doctor);

        return toDTO(doctor);
    }

    public DoctorDTO getDoctorById(String doctorId){
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(()-> new NotFoundException("Doctor not found for doctorId " + doctorId));
        return toDTO(doctor);
    }

    public DoctorDTO findByEmail(String email){
        Doctor doctor = doctorRepository.findByEmail(email).orElseThrow(()-> new NotFoundException("Doctor not found with email: " + email));
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

    public List<DoctorDTO> searchByNameOrEmail(String query) {
        String keyword = query.trim().toLowerCase();

        List<Doctor> doctors = doctorRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                keyword, keyword, keyword
        );

        return doctors.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    /**
     *
     * @param doctorId
     * @param firstName
     * @param lastName
     * @param specialityId
     * @param phoneNumber
     * @param dateOfBirth
     * @param bio
     * @param languages
     * @param consultationFee
     * @param appointmentTypes
     * @return
     */
    public DoctorDTO updateDoctor(String doctorId, String firstName, String lastName, String specialityId, String phoneNumber, LocalDate dateOfBirth, String bio, List<LanguageCode> languages, Integer consultationFee, List<AppointmentType> appointmentTypes){
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(()-> new NotFoundException("Doctor not found with ID: " + doctorId));

        if(firstName != null && !firstName.isBlank()){
            String formatted = firstName.trim();
            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1).toLowerCase();
            doctor.setFirstName(formatted);
        }

        if(lastName != null && !lastName.isBlank()){
            String formatted = lastName.trim();
            formatted = formatted.substring(0, 1).toUpperCase() + formatted.substring(1).toLowerCase();
            doctor.setLastName(formatted);
        }

        if(specialityId != null && !specialityId.isBlank()){
            Speciality speciality = specialityRepository.findById(specialityId)
                    .orElseThrow(() -> new NotFoundException("Speciality not found with ID + " + specialityId));
            doctor.setSpeciality(speciality);
        }

        if(phoneNumber != null && !phoneNumber.isBlank()){
            String formatted = phoneNumber.trim();
            doctor.setPhoneNumber(formatted);
        }

        if(dateOfBirth != null){
            LocalDate today = LocalDate.now();
            LocalDate adultDate = today.minusYears(18);

            if(dateOfBirth.isAfter(adultDate)){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor must be at least 18 years old");
            }
            doctor.setDateOfBirth(dateOfBirth);
        }

        if(bio != null && !bio.isBlank()){
            doctor.setBio(bio);
        }

        if(languages != null){
            doctor.setLanguages(languages.stream().distinct().toList());
        }


        if(consultationFee != null){

            if (consultationFee <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Consultation fee cannot be negative");
            }
            doctor.setConsultationFee(consultationFee);
        }

        if(appointmentTypes != null){
            doctor.setAppointmentTypes(appointmentTypes.stream().distinct().toList());
        }


        doctorRepository.save(doctor);
        return toDTO(doctor);
    }


    /**
     *
     * @param c
     * @param pageable
     * @return
     */
    public Page<DoctorDTO> search(DoctorSearchCriteria c, Pageable pageable) {
        List<Criteria> criteria = new ArrayList<>();

        if(c.getSpecialityId() != null) {
            criteria.add(
                    Criteria.where("speciality.$id").is(new ObjectId(c.getSpecialityId()))
            );
        }

        if(c.getLanguage() != null && !c.getLanguage().isBlank()){
            String langSrt = c.getLanguage().trim().toLowerCase();
            LanguageCode lang;

            try{
                lang = LanguageCode.valueOf(langSrt);
            } catch (IllegalArgumentException ex){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown language: " + c.getLanguage());
            }
            criteria.add(
                    Criteria.where("languages").in(c.getLanguage())
            );
        }

        if(c.getMinFee() != null || c.getMaxFee() != null){
            Criteria feeCriteria = Criteria.where("consultationFee");
            if(c.getMinFee() != null) feeCriteria = feeCriteria.gte(c.getMinFee());
            if(c.getMaxFee() != null) feeCriteria = feeCriteria.lte(c.getMaxFee());
            criteria.add(feeCriteria);
        }

        if(c.getClinicId() != null) {
            criteria.add(
                    Criteria.where("clinic.$id").is(new ObjectId(c.getClinicId()))
            );
        }

//        if (c.getLat() != null && c.getLng() != null && c.getRadiusKm() != null) {
//            Point point = new Point(c.getLng(), c.getLat());
//            Distance distance = new Distance(c.getRadiusKm(), Metrics.KILOMETERS);
//            criteria.add(
//                    Criteria.where("location")
//                            .nearSphere(point)
//                            .maxDistance(distance.getNormalizedValue())
//            );
//        }

        // simple text search by name/bio
        if (c.getQ() != null && !c.getQ().isBlank()) {
            String regex = ".*" + Pattern.quote(c.getQ()) + ".*";
            Criteria text = new Criteria().orOperator(
                    Criteria.where("firstName").regex(regex, "i"),
                    Criteria.where("lastName").regex(regex, "i"),
                    Criteria.where("bio").regex(regex, "i")
            );
            criteria.add(text);
        }



        Query query = new Query();

        if(!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        query.with(pageable);

        List<Doctor> content = mongoTemplate.find(query, Doctor.class);
        List<DoctorDTO> dtoList = content.stream().map(this::toDTO).toList();

        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Doctor.class);

        return new PageImpl<>(dtoList, pageable, total);

    }


    private DoctorDTO toDTO(Doctor doctor){

        List<ReviewDTO> latestReviews = reviewRepository
                .findTop3ByDoctorIdOrderByCreatedAtDesc(doctor.getId())
                .stream()
                .map(r -> ReviewDTO.builder()
                        .id(r.getId())
                        .comment(r.getComment())
                        .rating(r.getRating())
                        .patientId(r.getPatient().getId())
                        .doctorId(r.getDoctor().getId())
                        .createdAt(r.getCreatedAt())
                        .build()
                ).toList();


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
                .latestReviews(latestReviews)
                .averageRating(doctor.getAverageRating())
                .imageUrl(doctor.getImageUrl())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .consultationFee(doctor.getConsultationFee())
                .languages(doctor.getLanguages())
                .clinicId(doctor.getClinic().getId())
                .build();
    }






}
