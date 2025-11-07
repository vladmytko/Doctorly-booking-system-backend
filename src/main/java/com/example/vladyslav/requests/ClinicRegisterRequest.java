package com.example.vladyslav.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ClinicRegisterRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;

    @NotBlank(message = "Name is required")
    @Size(min = 2, message = "Name must be at least 2 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(min = 2, message = "Address must be at least 2 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(min = 2, message = "City must be at least 2 characters")
    private String city;

    @Pattern(
            regexp = "^([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9]?[A-Za-z]?))))\\s?[0-9][A-Za-z]{2})$",
            message = "Invalid UK postcode format"
    )
    private String postCode;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, message = "Phone number must start with 0, example 0754567890")
    private String phoneNumber;

    @NotBlank(message = "Bio is required")
    private String description;

    @NotNull(message = "Image is required")
    private MultipartFile image;


}
