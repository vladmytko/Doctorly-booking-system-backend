package com.example.vladyslav.repository;

import com.example.vladyslav.model.Invitation;
import com.example.vladyslav.model.enums.InvitationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ClinicInvitationRepository extends MongoRepository<Invitation, String> {
    Optional<Invitation> findByIdAndDoctorId(String id, String doctorId);
    List<Invitation> findByDoctorIdAndStatus(String doctorId, InvitationStatus invitationStatus);
}
