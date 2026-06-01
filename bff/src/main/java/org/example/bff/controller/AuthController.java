package org.example.bff.controller;

import org.example.bff.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final JwtService jwtService;

    // Demo users — replace with DB lookup in production
    private static final Map<String, long[]> USERS = Map.of(
            "demo",  new long[]{1},
            "admin", new long[]{2}
    );
    private static final Map<String, String> PASSWORDS = Map.of(
            "demo",  "demo",
            "admin", "admin"
    );
    private static final Map<String, String> ROLES = Map.of(
            "demo",  "USER",
            "admin", "ADMIN"
    );

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || !PASSWORDS.getOrDefault(username, "").equals(password)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        long userId = USERS.get(username)[0];
        String role = ROLES.get(username);
        String token = jwtService.generateToken(userId, username, role);

        return ResponseEntity.ok(Map.of(
                "token",    token,
                "username", username,
                "role",     role
        ));
    }
}
