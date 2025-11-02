package by.bsuir.growpathserver.trainee.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.common.util.JwtUtils;

@RestController
@RequestMapping("/trainee")
public class TraineeController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "trainee-service"));
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("username", JwtUtils.getUsername(jwt));
        profile.put("email", JwtUtils.getEmail(jwt));
        profile.put("roles", JwtUtils.extractRoles(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminEndpoint() {
        return ResponseEntity.ok(Map.of("message", "Admin access granted"));
    }

    @GetMapping("/hr")
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> hrEndpoint() {
        return ResponseEntity.ok(Map.of("message", "HR Manager access granted"));
    }

    @GetMapping("/trainee-info")
    @PreAuthorize("hasAnyRole('INTERN', 'MENTOR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> traineeInfoEndpoint() {
        return ResponseEntity.ok(Map.of("message", "Trainee information access granted"));
    }
}
