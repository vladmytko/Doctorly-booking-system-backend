package com.example.vladyslav.requests;

import lombok.Data;

import java.time.Instant;

@Data
public class RescheduleRequest {
    private Instant newStart;
    private Instant newEnd;
}
