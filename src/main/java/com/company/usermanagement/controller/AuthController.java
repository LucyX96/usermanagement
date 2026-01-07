package com.company.usermanagement.controller;

import com.company.usermanagement.entity.User;
import com.company.usermanagement.entity.dtoIN.LoginRequestDTO;
import com.company.usermanagement.entity.dtoIN.RegisterRequestDTO;
import com.company.usermanagement.entity.dtoOut.LoginResponseDTO;
import com.company.usermanagement.security.service.JwtService;
import com.company.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoint per Login e Logout")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO requestDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDTO.username(), requestDTO.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtService.generateToken(authentication);
        User user = userService.loadUserByUsername(requestDTO.username());
        LoginResponseDTO response = new LoginResponseDTO(token, user.getName());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO requestDTO) {
        userService.registerUser(requestDTO);
        return ResponseEntity.ok("Registrazione OK");
    }
}
