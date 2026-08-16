package com.decisionhub.security;

import com.decisionhub.dto.request.authentication.LoginRequest;
import com.decisionhub.dto.request.authentication.RegisterRequest;
import com.decisionhub.dto.response.authentication.LoginResponse;
import com.decisionhub.entity.authentication.User;
import com.decisionhub.enums.authentication.PlatformRole;
import com.decisionhub.enums.authentication.UserStatus;
import com.decisionhub.repository.authentication.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.findByEmail("user@test.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("admin@test.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("moderator@test.com").ifPresent(userRepository::delete);

        // 1. Register a standard user
        RegisterRequest userReg = new RegisterRequest("testuser", "user@test.com", "Password123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReg)))
                .andExpect(status().isOk());

        // 2. Create an admin user directly in the database
        User admin = new User();
        admin.setUsername("testadmin");
        admin.setEmail("admin@test.com");
        admin.setPasswordHash(passwordEncoder.encode("Password123!"));
        admin.setRole(PlatformRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);

        // 3. Create a moderator user directly in the database
        User moderator = new User();
        moderator.setUsername("testmoderator");
        moderator.setEmail("moderator@test.com");
        moderator.setPasswordHash(passwordEncoder.encode("Password123!"));
        moderator.setRole(PlatformRole.MODERATOR);
        moderator.setStatus(UserStatus.ACTIVE);
        userRepository.save(moderator);
    }

    @Test
    void testUserLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "Password123!", "USER");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        assertNotNull(loginResponse.token());
    }

    @Test
    void testAdminLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest("admin@test.com", "Password123!", "ADMIN");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        assertNotNull(loginResponse.token());
    }

    @Test
    void testModeratorLoginAsUser_Success() throws Exception {
        LoginRequest request = new LoginRequest("moderator@test.com", "Password123!", "USER");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        assertNotNull(loginResponse.token());
    }

    @Test
    void testUserLoginAsAdmin_MismatchedRole_Fails() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "Password123!", "ADMIN");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.error").value("Invalid email or password"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertFalse(responseContent.contains("token"));
    }

    @Test
    void testAdminLoginAsUser_MismatchedRole_Fails() throws Exception {
        LoginRequest request = new LoginRequest("admin@test.com", "Password123!", "USER");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.error").value("Invalid email or password"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertFalse(responseContent.contains("token"));
    }

    @Test
    void testModeratorLoginAsAdmin_MismatchedRole_Fails() throws Exception {
        LoginRequest request = new LoginRequest("moderator@test.com", "Password123!", "ADMIN");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.error").value("Invalid email or password"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertFalse(responseContent.contains("token"));
    }

    @Test
    void testLogin_InvalidRoleName_Fails() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "Password123!", "INVALID_ROLE");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.error").value("Invalid email or password"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertFalse(responseContent.contains("token"));
    }

    @Test
    void testUserLogin_WrongPassword_Fails() throws Exception {
        LoginRequest request = new LoginRequest("user@test.com", "WrongPassword!", "USER");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.error").value("Invalid email or password"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertFalse(responseContent.contains("token"));
    }

    @Test
    void testAdminLogin_WrongPassword_Fails() throws Exception {
        LoginRequest request = new LoginRequest("admin@test.com", "WrongPassword!", "ADMIN");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.error").value("Invalid email or password"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertFalse(responseContent.contains("token"));
    }
}
