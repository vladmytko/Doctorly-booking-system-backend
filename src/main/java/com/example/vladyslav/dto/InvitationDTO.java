package com.example.vladyslav.dto;

import com.example.vladyslav.model.enums.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvitationDTO {

    private String id;
    private String clinicId;
    private String doctorId;
    private String doctorEmail;
    private InvitationStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
