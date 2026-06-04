package org.example.bff.controller;

import org.example.bff.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final JwtService jwtService;
    private final RestClient userClient;

    public AuthController(JwtService jwtService,
                          @Value("${services.user-url}") String userUrl) {
        this.jwtService = jwtService;
        this.userClient = RestClient.create(userUrl);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password required"));
        }

        try {
            // Delegate credential verification to userservice
            @SuppressWarnings("unchecked")
            Map<String, Object> user = userClient.post()
                    .uri("/users/authenticate")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(Map.of("username", username, "password", password))
                    .retrieve()
                    .body(Map.class);

            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
            }

            Long userId   = Long.valueOf(user.get("id").toString());
            String role   = user.getOrDefault("role", "USER").toString();

            String token = jwtService.generateToken(userId, username, role);

            logger.info("Login successful for user: {} (role: {})", username, role);

            return ResponseEntity.ok(Map.of(
                    "token",    token,
                    "username", username,
                    "role",     role
            ));

        } catch (HttpClientErrorException.Unauthorized e) {
            logger.warn("Failed login attempt for user: {}", username);
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        } catch (Exception e) {
            logger.error("Auth error for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(503).body(Map.of("error", "Auth service unavailable"));
        }
    }
}
