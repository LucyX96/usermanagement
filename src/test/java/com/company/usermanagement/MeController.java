package com.company.usermanagement;

import com.company.usermanagement.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class MeController {

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        User u = (User) authentication.getPrincipal();
        return ResponseEntity.ok(new MeResponse(u.getUsername(), u.getEmail(), u.getName()));
    }

    public record MeResponse(String username, String email, String name) {}
}
