package com.example.vladyslav.service;

import com.example.vladyslav.dto.DaySlotsDTO;
import com.example.vladyslav.exception.OurException;
import com.example.vladyslav.model.Appointment;
import com.example.vladyslav.model.AvailabilityRule;
import com.example.vladyslav.model.enums.AppointmentStatus;
import com.example.vladyslav.repository.AppointmentRepository;
import com.example.vladyslav.repository.AvailabilityRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityRuleService {

    private final AvailabilityRuleRepository repository;

    private final AppointmentRepository appointmentRepository;

    public List<AvailabilityRule> findByDoctorId(String doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    public AvailabilityRule upsert(AvailabilityRule rule) {
        validate(rule);

        return repository.findByDoctorIdAndDayOfWeek(rule.getDoctorId(), rule.getDayOfWeek())
                .map(existing -> {
                    existing.setStart(rule.getStart());
                    existing.setEnd(rule.getEnd());
                    existing.setSlotMinutes(rule.getSlotMinutes());
                    existing.setBufferBeforeMinutes(rule.getBufferBeforeMinutes());
                    existing.setBufferAfterMinutes(rule.getBufferAfterMinutes());
                    return repository.save(existing);
                })
                .orElseGet(()-> repository.save(rule));
    }

    private void validate(AvailabilityRule rule){
        if(rule.getDoctorId() == null) throw new OurException("Doctor is required.");
        if(rule.getDayOfWeek() < 1 || rule.getDayOfWeek() > 7) throw new OurException("dayOfWeek must be between 1..7");

        LocalTime s = rule.getStart(), e = rule.getEnd();
        if(s == null || e == null || !e.isAfter(s)) throw new OurException("Invalid start/end for availability.");
        if(rule.getSlotMinutes() < 0) throw new OurException("slotMinutes cannot be negative.");
    }

    public List<DaySlotsDTO> getAvailability(String doctorId, LocalDate from, LocalDate to) {

        // Load doctor's weekly schedule rules from DB
        List<AvailabilityRule> rules = findByDoctorId(doctorId);

        // e.g. 1 -> Monday rule
        // e.g. 2 -> Tuesday rule
        // e.g  3 -> "id": "6904cc4ec8248ceb824c2e40",
        //        "doctorId": "690321b8a20b7634c71ef405",
        //        "dayOfWeek": 1,
        //        "start": "09:00:00",
        //        "end": "17:00:00",
        //        "slotMinutes": 30,
        //        "bufferBeforeMinutes": 5,
        //        "bufferAfterMinutes": 5,
        //        "breaks": [
        //            {
        //                "startBreak": "12:30:00",
        //                "endBreak": "13:30:00"
        //            },
        //            {
        //                "startBreak": "15:00:00",
        //                "endBreak": "15:15:00"
        //            }
        //        ]
        Map<Integer, AvailabilityRule> ruleByDow = rules.stream()
                .collect(Collectors.toMap(AvailabilityRule::getDayOfWeek, r -> r));
        // AvailabilityRule::getDayOfWeek (KEY) this is method reference, means rule -> rule.getDayOfWeek()
        // r -> r (VALUE) means use object itself as a value.

        List<DaySlotsDTO> result = new ArrayList<>();


        // Loop date by date between from and to, so if from = Jan 6 and to = Jan 12, it iterates: Jav6, Jan 7, Jan 8,Jan9, Jan 10
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            int dow = date.getDayOfWeek().getValue(); // ISO 1..7
            AvailabilityRule rule = ruleByDow.get(dow);  // Get rule for specific date

            // If doctor doesn't work that day, meaning rule is null;
            if (rule == null) {
                result.add(new DaySlotsDTO(date, List.of())); // return empty slots
                continue;
            }

            // Load existing appointments for that doctor & date
            List<TimeInterval> booked = findBookedIntervals(doctorId, date);

            // Convert breaks to intervals
            List<TimeInterval> breaks = (rule.getBreaks() == null) ? List.of() :
                    rule.getBreaks().stream()
                            .map(b -> new TimeInterval(b.getStartBreak(), b.getEndBreak()))
                            .toList();

            List<String> slots = generateSlots(rule, breaks, booked);
            result.add(new DaySlotsDTO(date, slots));
        }

        return result;
    }

    private List<String> generateSlots(AvailabilityRule rule,
                                       List<TimeInterval> breaks,
                                       List<TimeInterval> booked) {

        int slotMin = rule.getSlotMinutes(); // 30 min
        int bufBefore = Math.max(0, rule.getBufferBeforeMinutes()); // 5 min
        int bufAfter = Math.max(0, rule.getBufferAfterMinutes()); // 5 min

        LocalTime workStart = rule.getStart(); // 10:00
        LocalTime workEnd = rule.getEnd();     // 17:00

        List<String> out = new ArrayList<>();

        for (LocalTime t = workStart; !t.plusMinutes(slotMin).isAfter(workEnd); t = t.plusMinutes(slotMin)) {
            // Generates slots, starts from date start e.g. 10:00 and adds slotMin e.g. 30 min. Generate 10:00, 10:30, 11:00, 11:00 until time is after workEnd e.g. 17:00

            LocalTime appontmentStartTime = t; // e.g. 10:00
            LocalTime appointmentEndTime = t.plusMinutes(slotMin); // e.g 10:30

            // blocked interval includes buffers
            // Meaning: if someone books 10:00-10:30, doctor if busy from 09:55 to 10:35, depends on buffer times.
            LocalTime blockedStart = appontmentStartTime.minusMinutes(bufBefore);
            LocalTime blockedEnd = appointmentEndTime.plusMinutes(bufAfter);

            // If blocked interval touches break, skip that slot
            boolean overlapsBreak = breaks.stream().anyMatch(b -> overlaps(blockedStart, blockedEnd, b.start(), b.end()));
            if (overlapsBreak) continue;

            // If blocked interval touches booked appointment, skip that slot
            boolean overlapsBooked = booked.stream().anyMatch(b -> overlaps(blockedStart, blockedEnd, b.start(), b.end()));
            if (overlapsBooked) continue;

            // Add slots to list as ["09:00", "09:30", "10:00"]
            out.add(appontmentStartTime.toString().substring(0,5)); // "HH:mm"
        }

        return out;
    }

    public List<TimeInterval> findBookedIntervals(String doctorId, LocalDate date) {

        ZoneId clinicZone = ZoneId.of("Europe/London"); // IMPORTANT

        Instant dayStart = date.atStartOfDay(clinicZone).toInstant(); //e.g. dayStart = 2026-01-06 00:00 London time -> Instant
        Instant dayEnd   = date.plusDays(1).atStartOfDay(clinicZone).toInstant();  //e.g. dayEnd = 2026-01-07 00:00 London time -> Instant


        // appointment starts before the day ends
        // appointment ends after the day starts
        List<Appointment> appointments =
                appointmentRepository.findByDoctorIdAndStartLessThanAndEndGreaterThan(
                        doctorId,
                        dayEnd,
                        dayStart
                );

        // Only appointments with status SCHEDULED are considered
        return appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                .map(a -> {
                    LocalTime start = LocalDateTime.ofInstant(a.getStart(), clinicZone).toLocalTime(); // Get appointment start time
                    LocalTime end   = LocalDateTime.ofInstant(a.getEnd(), clinicZone).toLocalTime();   // Get appointment end time
                    return new TimeInterval(start, end);
                })
                .toList();

        /** return:
         * [
         *   TimeInterval(start=10:30, end=11:00),
         *   TimeInterval(start=14:00, end=14:30)
         * ]
         */
    }

    private boolean overlaps(LocalTime appointmentStart, LocalTime appointmentEnd, LocalTime breakStart, LocalTime breakEnd) {
        return appointmentStart.isBefore(breakEnd) && breakStart.isBefore(appointmentEnd);
    }


    public record TimeInterval(LocalTime start, LocalTime end) {}
}
