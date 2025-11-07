package com.example.vladyslav.service;

import com.example.vladyslav.dto.InvitationDTO;
import com.example.vladyslav.exception.NotFoundException;
import com.example.vladyslav.exception.OurException;
import com.example.vladyslav.model.Clinic;
import com.example.vladyslav.model.Invitation;
import com.example.vladyslav.model.Doctor;
import com.example.vladyslav.model.enums.InvitationStatus;
import com.example.vladyslav.repository.ClinicInvitationRepository;
import com.example.vladyslav.repository.ClinicRepository;
import com.example.vladyslav.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final ClinicInvitationRepository invitationRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final ClinicService clinicService;

    public InvitationDTO createClinicInvitation(String clinicId, String doctorEmail){

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(()-> new NotFoundException("Clinic not found with id:" + clinicId));

        Doctor doctor = doctorRepository.findByEmail(doctorEmail)
                .orElseThrow(()-> new NotFoundException("Doctor not found with email: " + doctorEmail));

        if (doctor.getClinic() != null && clinicId.equals(doctor.getClinic().getId())){
            throw new OurException("Doctor already works in this clinic");
        }

        Invitation invitation = Invitation.builder()
                .clinicId(clinicId)
                .doctorId(doctor.getId())
                .doctorEmail(doctor.getEmail())
                .status(InvitationStatus.PENDING)
                .build();

        invitationRepository.save(invitation);

        return toDTO(invitation);
    }

    public List<InvitationDTO> getPendingInvitationsForDoctor(String doctorId){
        List<Invitation> invitations = invitationRepository.findByDoctorIdAndStatus(doctorId, InvitationStatus.PENDING);

        return invitations.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void respondToInvitation(String invitationId, String doctorId, boolean accept){
        Invitation invitation = invitationRepository.findByIdAndDoctorId(invitationId, doctorId)
                .orElseThrow(()-> new NotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new OurException("Invitation is no longer valid");
        }

        if (accept) {
            // Attach doctor to clinic
            clinicService.addDoctorToClinic(invitation.getClinicId(), doctorId);
            invitation.setStatus(InvitationStatus.ACCEPTED);
        } else {
            invitation.setStatus(InvitationStatus.DECLINED);
        }

        invitationRepository.save(invitation);
    }

    private InvitationDTO toDTO(Invitation invitation){
        return InvitationDTO.builder()
                .id(invitation.getId())
                .clinicId(invitation.getClinicId())
                .doctorId(invitation.getDoctorId())
                .doctorEmail(invitation.getDoctorEmail())
                .status(invitation.getStatus())
                .createdAt(invitation.getCreatedAt())
                .updatedAt(invitation.getUpdatedAt())
                .build();
    }
}
