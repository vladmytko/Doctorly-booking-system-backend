package com.example.vladyslav.service;

import com.example.vladyslav.dto.UserDTO;
import com.example.vladyslav.exception.NotFoundException;
import com.example.vladyslav.model.User;
import com.example.vladyslav.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("User not found by email: " + email));
    }

    public UserDTO getUserById(String userId){
        return userRepository.findById(userId)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("User not found by ID: " + userId));
    }

    public Page<UserDTO> getAllUsers(int page, int size){
        Pageable pageable = PageRequest.of(page,size);

        return userRepository.findAll(pageable).map(this::toDto);
    }

    public User getCurrentUser(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("User not found with email: " + email));
    }

    private UserDTO toDto(User user){
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }


}
