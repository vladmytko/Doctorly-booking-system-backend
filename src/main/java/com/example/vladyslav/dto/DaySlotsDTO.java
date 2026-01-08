package com.example.vladyslav.dto;

import java.time.LocalDate;
import java.util.List;

public record DaySlotsDTO(LocalDate date, List<String> slots) {}
