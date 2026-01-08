package com.company.usermanagement.dev;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class BCryptPrinter implements CommandLineRunner {

    private final PasswordEncoder encoder;

    public BCryptPrinter(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        System.out.println("BCrypt(Admin123!) = " + encoder.encode("Admin123!"));
    }
}
