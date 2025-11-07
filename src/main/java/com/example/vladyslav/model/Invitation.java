package com.example.vladyslav.model;

import com.example.vladyslav.model.enums.InvitationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("clinic_invitations")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Invitation {

    @Id
    private String id;

    @Indexed
    private String clinicId;

    @Indexed
    private String doctorId;

    // Store email for extra safety
    @NotBlank
    @Email
    private String doctorEmail;

    private InvitationStatus status;  // PENDING, ACCEPTED, DECLINED, EXPIRED

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
