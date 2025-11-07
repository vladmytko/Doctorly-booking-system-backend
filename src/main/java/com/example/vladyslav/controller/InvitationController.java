package com.example.vladyslav.controller;

import com.example.vladyslav.dto.InvitationDTO;
import com.example.vladyslav.exception.NotFoundException;
import com.example.vladyslav.model.Doctor;
import com.example.vladyslav.model.Invitation;
import com.example.vladyslav.model.User;
import com.example.vladyslav.repository.DoctorRepository;
import com.example.vladyslav.service.InvitationService;
import com.example.vladyslav.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitation")
@RequiredArgsConstructor
@Validated
public class InvitationController {

    private final InvitationService service;
    private final UserService userService;
    private final DoctorRepository doctorRepository;

    @PostMapping("/{clinicId}/invite-doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('CLINIC','ADMIN')")
    public ResponseEntity<InvitationDTO> inviteDoctor(
            @PathVariable String clinicId,
            @PathVariable String doctorId ) {

        return new ResponseEntity<InvitationDTO>(service.createClinicInvitation(clinicId, doctorId), HttpStatus.CREATED);
    }

    @GetMapping("clinic-invitations/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<InvitationDTO>> getMyInvitations(Authentication auth) {
        User user = userService.getCurrentUser(auth);
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));


        return new ResponseEntity<>(service.getPendingInvitationsForDoctor(doctor.getId()), HttpStatus.FOUND);
    }

    @PostMapping("/api/clinic-invitations/{invitationId}/accept")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> acceptInvitation(@PathVariable String invitationId, Authentication auth) {
        User user = userService.getCurrentUser(auth);
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));

        service.respondToInvitation(invitationId, doctor.getId(), true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/clinic-invitations/{invitationId}/decline")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> declineInvitation(@PathVariable String invitationId, Authentication auth) {
        User user = userService.getCurrentUser(auth);
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Doctor profile not found"));

        service.respondToInvitation(invitationId, doctor.getId(), false);
        return ResponseEntity.noContent().build();
    }
}
