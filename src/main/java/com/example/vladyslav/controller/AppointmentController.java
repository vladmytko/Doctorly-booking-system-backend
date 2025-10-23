package com.example.vladyslav.controller;

import com.example.vladyslav.dto.AppointmentDTO;
import com.example.vladyslav.model.Appointment;
import com.example.vladyslav.model.enums.AppointmentStatus;
import com.example.vladyslav.requests.RescheduleRequest;
import com.example.vladyslav.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;


@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<Page<AppointmentDTO>> getAppointments( @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(appointmentService.getAllAppointments(page, size));
    }

    @GetMapping("/id/{appointmentId}")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable String appointmentId){
        return ResponseEntity.ok(appointmentService.getAppointmentById(appointmentId));
    }


    @GetMapping("/patient-id/{patientId}")
    public ResponseEntity<Page<AppointmentDTO>> getAppointmentsByPatientId(@PathVariable String patientId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(patientId, page, size));
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Page<AppointmentDTO>> getAppointmentsByDoctorId(@PathVariable String doctorId,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorId(doctorId, page, size));
    }

    @PostMapping
    public ResponseEntity<AppointmentDTO> createAppointment(@RequestBody Appointment appointment) {
            return new ResponseEntity<>(appointmentService.createAppointment(appointment), HttpStatus.CREATED);
    }

    @PostMapping("/cancel/{appointmentId}")
    public ResponseEntity<Void> cancel(@PathVariable String appointmentId){
        appointmentService.cancel(appointmentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reschedule/{appointmentId}")
    public ResponseEntity<AppointmentDTO> reschedule(@PathVariable String appointmentId, @RequestBody RescheduleRequest request){
        return ResponseEntity.ok(appointmentService.reschedule(appointmentId, request));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<AppointmentDTO>> getAppointmentByStatus(@PathVariable AppointmentStatus status,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status, page, size));
    }

    @GetMapping("/doctor-id/{doctorId}/by-start")
    public ResponseEntity<Optional<AppointmentDTO>> getAppointmentByDoctorIdAndStartDate(@PathVariable String doctorId, @RequestParam Instant startDate){
        return ResponseEntity.ok(appointmentService.getAppointmentByDoctorIdAndStartDate(doctorId, startDate));
    }

    @GetMapping("/clinic-id/{clinicId}/by-start-end")
    public ResponseEntity<Page<AppointmentDTO>> getAppointmentByClinicIdFromStartToEndDates(@PathVariable String clinicId,
                                                                                            @RequestParam Instant start,
                                                                                            @RequestParam Instant end,
                                                                                            @RequestParam(defaultValue = "0") int page,
                                                                                            @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(appointmentService.getAppointmentByClinicIdFromStartToEndDates(clinicId, start, end, page, size));
    }

    @GetMapping("/by-clinic/{clinicId}")
    public ResponseEntity<Page<AppointmentDTO>> getAppointmentsByClinicId(@PathVariable String clinicId,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(appointmentService.getAppointmentsByClinicId(clinicId, page, size));
    }

    @GetMapping("/doctor-id/{doctorId}/by-status")
    public ResponseEntity<Page<AppointmentDTO>> findByDoctorIdAndStatusBetween(@PathVariable String doctorId,
                                                                               @RequestParam AppointmentStatus appointmentStatus,
                                                                               @RequestParam Instant from,
                                                                               @RequestParam Instant to,
                                                                               @RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(appointmentService.findByDoctorIdAndStatusBetween(doctorId, appointmentStatus, from, to, page, size));
    }



}
