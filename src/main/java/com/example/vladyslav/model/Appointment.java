package com.example.vladyslav.model;

import com.example.vladyslav.model.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "appointments")

@CompoundIndexes({
        @CompoundIndex(name = "uniq_doctor_start", def = "{ 'doctorId': 1, 'start': 1 }", unique = true),
        @CompoundIndex(name = "idx_doctor_range", def = "{ 'doctorId': 1, 'start': 1, 'end': 1 }")
})
public class Appointment {

    @Id
    private String id;

    @Indexed
    private String doctorId;

    @Indexed
    private String patientId;

    @Indexed
    private String clinicId;

    /**
     * Absolute UTC times for DST-safe scheduling
     */
    @Indexed
    private Instant start;

    private Instant end;

    private AppointmentStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;

    private String concern;



}
