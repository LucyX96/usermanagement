package com.company.usermanagement.service;

import com.company.usermanagement.entity.Role;
import com.company.usermanagement.entity.User;
import com.company.usermanagement.entity.dtoIN.RegisterRequestDTO;
import com.company.usermanagement.entity.dtoOut.RegisterResponseDTO;
import com.company.usermanagement.exception.RegistrationException;
import com.company.usermanagement.exception.UserAlreadyExistsException;
import com.company.usermanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User registerUser(RegisterRequestDTO requestDTO) {
        String username = requestDTO.username();
        String rawPassword = requestDTO.password();
        String email = requestDTO.email();
        String name = requestDTO.name();
        long userCount = userRepository.count();
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username già esistente");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email già esistente");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setEnabled(true);
        Set<Role> roles = new HashSet<>();

        if (userCount == 0) {
            roles.add(Role.ADMIN);
        } else {
            roles.add(Role.USER);
        }
        newUser.setRoles(roles);

        User savedUser = userRepository.save(newUser);

        if (savedUser.getId() == null) {
            throw new RegistrationException("Errore durante la registrazione");
        }
        return savedUser;
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
