package com.company.usermanagement.entity.dtoIN;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank(message = "Lo username non può essere vuoto")
        String username,

        @NotBlank(message = "La password non può essere vuota")
        @Size(min = 8, message = "La password deve essere di almeno 8 caratteri")
        String password
) {}
