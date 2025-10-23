package com.example.vladyslav.service;

import com.example.vladyslav.model.User;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JWTService {

    public final SecretKey key;
    public final long expMinutes;
    public final String issuer;

    public JWTService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.exp-minutes}") long expMinutes,
            @Value("${app.jwt.issuer}") String issuer
    ) {
        byte[] bytes = secret.length() % 4 == 0 ? Decoders.BASE64.decode(secret) : secret.getBytes();
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expMinutes = expMinutes;
        this.issuer = issuer;
    }

    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(expMinutes, ChronoUnit.MINUTES);


        // Put a single role into JWT (enum -> String)
        var roles = user.getRole() == null ? java.util.List.<String>of()
                : java.util.List.of(user.getRole());

        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(user.getId())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles
                ))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
