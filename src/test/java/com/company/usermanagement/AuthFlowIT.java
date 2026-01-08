package com.company.usermanagement;

import com.company.usermanagement.entity.dtoIN.LoginRequestDTO;
import com.company.usermanagement.entity.dtoIN.RefreshRequestDTO;
import com.company.usermanagement.entity.dtoIN.RegisterRequestDTO;
import com.company.usermanagement.repository.RefreshTokenRepository;
import com.company.usermanagement.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static com.company.usermanagement.TestJson.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Autowired UserRepository userRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void cleanDb() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_shouldReturn201_andPersistUser() throws Exception {
        RegisterRequestDTO req = new RegisterRequestDTO("luciano", "Password123!", "luciano@test.com", "Luciano");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mapper, req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("luciano"))
                .andExpect(jsonPath("$.name").value("Luciano"));

        assertThat(userRepository.existsByUsername("luciano")).isTrue();
    }

    @Test
    void register_validationError_shouldReturn400_withDetails() throws Exception {
        // email invalida + password corta
        RegisterRequestDTO req = new RegisterRequestDTO("lu", "123", "not-an-email", "L");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mapper, req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void login_shouldReturnTokenPair() throws Exception {
        // register
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mapper, new RegisterRequestDTO("luciano", "Password123!", "luciano@test.com", "Luciano"))))
                .andExpect(status().isCreated());

        // login
        var res = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mapper, new LoginRequestDTO("luciano", "Password123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn();

        JsonNode json = mapper.readTree(res.getResponse().getContentAsString());
        assertThat(json.get("accessToken").asText()).isNotBlank();
        assertThat(json.get("refreshToken").asText()).isNotBlank();
    }

    @Test
    void refresh_shouldRotateRefreshToken_andReturnNewPair() throws Exception {
        // register
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mapper, new RegisterRequestDTO("luciano", "Password123!", "luciano@test.com", "Luciano"))))
                .andExpect(status().isCreated());

        // login
        var loginRes = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mapper, new LoginRequestDTO("luciano", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = mapper.readTree(loginRes.getResponse().getContentAsString());
        String refresh1 = loginJson.get("refreshToken").asText();
        String access1 = loginJson.get("accessToken").asText();

        // refresh
        var refreshRes = mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mapper, new RefreshRequestDTO(refresh1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andReturn();

        JsonNode refreshJson = mapper.readTree(refreshRes.getResponse().getContentAsString());
        String refresh2 = refreshJson.get("refreshToken").asText();
        String access2 = refreshJson.get("accessToken").asText();

        assertThat(refresh2).isNotEqualTo(refresh1);
        assertThat(access2).isNotEqualTo(access1);

        // refresh con token vecchio -> 401 (one-time use)
        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mapper, new RefreshRequestDTO(refresh1))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void protectedEndpoint_withoutToken_shouldReturn401Json() throws Exception {
        // Scegli un endpoint protetto del tuo progetto (esempio: /api/users/me)
        // Se non esiste, usa un endpoint reale protetto. Qui uso un placeholder: /api/users/me
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void logout_shouldRevokeRefreshTokens() throws Exception {
        // register + login
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mapper, new RegisterRequestDTO("luciano", "Password123!", "luciano@test.com", "Luciano"))))
                .andExpect(status().isCreated());

        var loginRes = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mapper, new LoginRequestDTO("luciano", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = mapper.readTree(loginRes.getResponse().getContentAsString());
        String access = loginJson.get("accessToken").asText();

        // logout (protetto)
        mvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + access))
                .andExpect(status().isNoContent());

        // Dopo logout: refresh emesso in login non deve più funzionare.
        // Per recuperarlo, prendi il refresh token dal login:
        String refresh = loginJson.get("refreshToken").asText();

        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mapper, new RefreshRequestDTO(refresh))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }
}
