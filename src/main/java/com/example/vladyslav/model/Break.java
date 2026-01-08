package com.example.vladyslav.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Break {
    private LocalTime startBreak;
    private LocalTime endBreak;
}
