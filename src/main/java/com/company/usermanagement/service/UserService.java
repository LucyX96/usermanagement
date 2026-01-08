package com.company.usermanagement.service;

import com.company.usermanagement.entity.Role;
import com.company.usermanagement.entity.User;
import com.company.usermanagement.entity.dtoIN.RegisterRequestDTO;
import com.company.usermanagement.exception.RegistrationException;
import com.company.usermanagement.exception.UserAlreadyExistsException;
import com.company.usermanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegisterRequestDTO request) {
        String username = normalizeUsername(request.username());
        String email = normalizeEmail(request.email());

        // check “fast”
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username già esistente");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email già esistente");
        }

        try {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(request.password()))
                    .email(email)
                    .name(request.name().trim())
                    .enabled(true)
                    .roles(Set.of(Role.USER))
                    .build();

            User saved = userRepository.save(user);

            if (saved.getId() == null) {
                throw new RegistrationException("Errore durante la registrazione");
            }
            return saved;
        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            // se scatta il vincolo unique DB per race condition, finisci qui
            throw new RegistrationException("Errore durante la registrazione");
        }
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalized = normalizeUsername(username);
        return userRepository.findByUsername(normalized)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + normalized)
                );
    }

    private String normalizeUsername(String username) {
        if (username == null) return null;
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
