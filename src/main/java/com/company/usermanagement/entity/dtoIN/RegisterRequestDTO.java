package com.company.usermanagement.entity.dtoIN;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Lo username non può essere vuoto")
        @Size(min = 3, max = 50, message = "Lo username deve essere tra 3 e 50 caratteri")
        String username,

        @NotBlank(message = "La password non può essere vuota")
        @Size(min = 8, max = 72, message = "La password deve essere tra 8 e 72 caratteri")
        String password,

        @NotBlank(message = "La email non può essere vuota")
        @Email(message = "Formato email non valido")
        @Size(max = 254, message = "La email è troppo lunga")
        String email,

        @NotBlank(message = "Il nome non può essere vuoto")
        @Size(min = 2, max = 80, message = "Il nome deve essere tra 2 e 80 caratteri")
        String name
) {}
