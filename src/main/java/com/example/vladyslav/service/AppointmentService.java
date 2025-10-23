package com.example.vladyslav.service;

import com.example.vladyslav.dto.AppointmentDTO;
import com.example.vladyslav.exception.NotFoundException;
import com.example.vladyslav.exception.OurException;
import com.example.vladyslav.model.Appointment;
import com.example.vladyslav.model.AvailabilityRule;
import com.example.vladyslav.model.TimeOff;
import com.example.vladyslav.model.enums.AppointmentStatus;
import com.example.vladyslav.repository.*;
import com.example.vladyslav.requests.RescheduleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final ZoneId UK_TZ = ZoneId.of("Europe/London");

    private AppointmentRepository appointmentRepository;
    private AvailabilityRuleRepository availabilityRuleRepository;
    private TimeOffRepository timeOffRepository;
    private UserRepository userRepository;
    private DoctorRepository doctorRepository;
    private PatientRepository patientRepository;

    public AppointmentDTO toDto(Appointment a){
        return AppointmentDTO.builder()
                .id(a.getId())
                .patientId(a.getPatientId())
                .doctorId(a.getDoctorId())
                .clinicId(a.getClinicId())
                .start(a.getStart())
                .end(a.getEnd())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .version(a.getVersion())
                .build();
    }

    public Page<AppointmentDTO> getAllAppointments(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentRepository.findAll(pageable);
        return appointments.map(this::toDto);
    }

    public AppointmentDTO getAppointmentById(String appointmentId){
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(()-> new NotFoundException("Appointment not found with Appointment ID " + appointmentId));
        return toDto(appointment);
    }

    public Page<Appointment> listForDoctor(String doctorId, Instant from, Instant to, Pageable pageable) {
        return appointmentRepository.findByDoctorIdAndStartBetween(doctorId,
                from != null ? from : Instant.EPOCH,
                to != null ? to : Instant.ofEpochMilli(Long.MAX_VALUE),
                pageable);
    }

    public AppointmentDTO createAppointment(Appointment draft) {
        validateDraft(draft);
        ensureWithinAvailability(draft);
        ensureNotDuringTimeOff(draft);
        ensureNoOverlap(draft);
        draft.setStatus(draft.getStatus() == null ? AppointmentStatus.SCHEDULED : draft.getStatus());

        appointmentRepository.save(draft);

        return toDto(draft);
    }

    public AppointmentDTO reschedule(String id, RescheduleRequest request) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(()-> new NotFoundException("Appointment not found with Appointment ID " + id));

        if(appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new OurException("Cannot reschedule a cancelled appointment.");
        }

        Appointment appointmentCheck = Appointment.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .clinicId(appointment.getClinicId())
                .start(request.getNewStart())
                .end(request.getNewEnd())
                .status(appointment.getStatus())
                .version(appointment.getVersion())
                .build();

        validateDraft(appointmentCheck);
        ensureWithinAvailability(appointmentCheck);
        ensureNotDuringTimeOff(appointmentCheck);
        ensureNoOverlapForRescheduling(appointmentCheck);

        appointment.setStart(request.getNewStart());
        appointment.setEnd(request.getNewEnd());
        appointmentRepository.save(appointment);

        return toDto(appointment);

    }

    public void cancel(String id) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow(()-> new NotFoundException("Appointment not found with id " + id));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    public  Page<AppointmentDTO> getAppointmentsByPatientId(String patientId, int page, int size){

        Pageable pageable = PageRequest.of(page,size);
        Page<Appointment> results = appointmentRepository.findByPatientIdOrderByStartDesc(patientId, pageable);
        return results.map(this::toDto);
    }

    public Page<AppointmentDTO> getAppointmentsByDoctorId(String doctorId, int page, int size){

        Pageable pageable = PageRequest.of(page,size);
        Page<Appointment> appointmentPage = appointmentRepository.findByDoctorIdOrderByStartDesc(doctorId, pageable);

        return appointmentPage.map(this::toDto);
    }

    public Page<AppointmentDTO> getAppointmentsByStatus(AppointmentStatus status, int page, int size){

        Pageable pageable = PageRequest.of(page,size);
        Page<Appointment> appointmentList = appointmentRepository.findByStatus(status, pageable);

        return appointmentList.map(this::toDto);
    }

    public Optional<AppointmentDTO> getAppointmentByDoctorIdAndStartDate(String doctorId, Instant start){
        return appointmentRepository
                .findByDoctorIdAndStart(doctorId,start)
                .map(this::toDto);
    }

    public Page<AppointmentDTO> getAppointmentByClinicIdFromStartToEndDates(String clinicId, Instant start, Instant end, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointmentPage = appointmentRepository.findByClinicIdAndStartBetween(clinicId, start, end, pageable);

        return appointmentPage.map(this::toDto);
    }

    public Page<AppointmentDTO> getAppointmentsByClinicId(String clinicId, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointmentPage = appointmentRepository.findByClinicIdOrderByStartDesc(clinicId, pageable);

        return appointmentPage.map(this::toDto);
    }

    public Page<AppointmentDTO> findByDoctorIdAndStatusBetween(String doctorId, AppointmentStatus status, Instant from, Instant to, int page, int size){
        Pageable pageable = PageRequest.of(page,size);
        Page<Appointment> appointmentPage = appointmentRepository.findByDoctorIdAndStatusBetween(doctorId, status, from, to, pageable);

        return appointmentPage.map(this::toDto);
    }

    /* --------------------------------------- Validators --------------------------------------------------------------- */

    public void validateDraft(Appointment appointment) {
        if(appointment.getDoctorId() == null || appointment.getPatientId() == null) {
            throw new OurException("doctorId and patientId are required.");
        }
        if (appointment.getStart() == null || appointment.getEnd() == null || !appointment.getEnd().isAfter(appointment.getStart()) || !appointment.getStart().isBefore(appointment.getEnd())) {
            throw new OurException("Invalid start/end.");
        }
    }

    public void ensureWithinAvailability(Appointment appointment) {
        // Convert start to UK local date/time
        ZonedDateTime startUK = appointment.getStart().atZone(UK_TZ);
        int isoDow = startUK.getDayOfWeek().getValue(); // 1..7

        AvailabilityRule rule = availabilityRuleRepository
                .findByDoctorIdAndDayOfWeek(appointment.getDoctorId(), isoDow)
                .orElseThrow(()-> new OurException("No availability for this day (doctorId=" + appointment.getDoctorId() +
                        ", day=" + isoDow + ")."));

        LocalTime startLocal = startUK.toLocalTime();
        LocalTime endLocal = appointment.getEnd().atZone(UK_TZ).toLocalTime();

        // Slot-size enforcement (optional, only if set)
        if (rule.getSlotMinutes() > 0) {
            long minutes = Duration.between(appointment.getStart(), appointment.getEnd()).toMinutes();
            if(minutes % rule.getSlotMinutes() != 0) {
                throw new OurException("Duration must be multiple of slotMinutes=" + rule.getSlotMinutes());
            }
        }
    }

    private void ensureNotDuringTimeOff(Appointment appointment){
        List<TimeOff> offs = timeOffRepository
                .findByDoctorIdAndStartLessThanEqualAndEndGreaterThanEqual(
                        appointment.getDoctorId(), appointment.getEnd(), appointment.getStart());

        if(!offs.isEmpty()) {
            throw new OurException("Doctor is unavailable (time-off) during the requested period.");
        }
    }

    public void ensureNoOverlap(Appointment appointment) {
        // Any appointment that starts before new end AND ends after new start is an overlap
        List<Appointment> overlaps = appointmentRepository
                .findByDoctorIdAndStartLessThanAndEndGreaterThan(appointment.getDoctorId(), appointment.getEnd(), appointment.getStart());
        // Allow same doc/time only if system updates the exact same record
        boolean conflict = overlaps.stream()
                .anyMatch(existing -> !existing.getId().equals(appointment.getId()) &&
                        existing.getStatus() != AppointmentStatus.CANCELLED);

        if(conflict) {
            throw new OurException("Overlapping appointment for this doctor with ID " + appointment.getDoctorId());
        }
    }

    public void ensureNoOverlapForRescheduling(Appointment appointment) {
        ensureNoOverlap(appointment);
    }

}
