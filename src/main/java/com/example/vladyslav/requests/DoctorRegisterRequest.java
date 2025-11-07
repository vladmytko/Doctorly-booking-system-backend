package com.example.vladyslav.requests;



import com.example.vladyslav.model.enums.LanguageCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class DoctorRegisterRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, message = "First name must be at least 2 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, message = "Last name must be at least 2 characters")
    private String lastName;

    @NotBlank(message = "Speciality title is required")
    private String specialityTitle;

    @NotNull(message = "Date of birth is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, message = "Phone number must start with 0, example 0754567890")
    private String phoneNumber;

    @NotNull(message = "Image is required")
    private MultipartFile image;

    @NotBlank(message = "Bio is required")
    private String bio;

    @NotNull(message = "At least one language is required")
    List<LanguageCode> languages;

    @NotNull(message = "Consultation price is required")
    private int consultationFee;



}
