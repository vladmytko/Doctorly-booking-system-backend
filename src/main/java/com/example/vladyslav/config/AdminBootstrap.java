package com.example.vladyslav.config;

import com.example.vladyslav.model.User;
import com.example.vladyslav.model.enums.Role;
import com.example.vladyslav.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AdminBootstrap implements ApplicationRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return;
        }
        users.findByEmail(adminEmail.trim().toLowerCase()).ifPresentOrElse(
                u -> { /* already exist, do nothing */},
                () -> users.save(User.builder()
                        .email(adminEmail.trim().toLowerCase())
                        .password(encoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .build())
        );
    }
}
