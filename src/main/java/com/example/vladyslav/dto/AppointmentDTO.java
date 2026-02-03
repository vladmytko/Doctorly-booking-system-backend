package com.example.vladyslav.dto;

import com.example.vladyslav.model.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDTO {
    private String id;
    private String patientId;
    private String doctorId;
    private String clinicId;
    private Instant start;
    private Instant end;
    private AppointmentStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;
    private String concern;

}
