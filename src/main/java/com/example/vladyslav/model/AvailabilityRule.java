package com.example.vladyslav.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("availability_rules")
@CompoundIndex(name = "idx_doctor_dow", def = "{ 'doctorId': 1, 'dayOfWeek': 1 }")
public class AvailabilityRule {

    @Id
    private String id;

    @Indexed
    private String doctorId;

    /**
     * ISO day-of-week: 1=Monday ... 7=Sunday
     */
    private int dayOfWeek;

    /**
     * Working hours for this day (UK local time)
     */
    private LocalTime start;
    private LocalTime end;

    /**
     * Appointment slot configuration
     */
    private int slotMinutes;            // e.g 30
    private int bufferBeforeMinutes;    // optional
    private int bufferAfterMinutes;     // optional

   private List<Break> breaks;
}

