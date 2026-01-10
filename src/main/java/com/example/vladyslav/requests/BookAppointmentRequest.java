package com.example.vladyslav.requests;

import lombok.Data;

@Data
public class BookAppointmentRequest {
    private String doctorId;
    private String patientId;
    private String concern;
    private String date;
    private String time;
}
