package com.portfolio.banking_api.controller;

import com.portfolio.banking_api.dto.AuthResponse;
import com.portfolio.banking_api.service.AuthService;
import com.portfolio.banking_api.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JWTService jwtService;

    @Test
    void register_returnsToken() throws Exception {
        when(authService.register(any())).thenReturn(
            new AuthResponse("fake-token", "Bearer", 3600000L)
        );

        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "email": "alex@example.com",
                    "password": "password123",
                    "firstName": "Alex",
                    "middleName": "",
                    "lastName": "Morgan"
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("fake-token"));
    }

    @Test
    void login_returnsToken() throws Exception {
        when(authService.login(any())).thenReturn(
            new AuthResponse("login-token", "Bearer", 3600000L)
        );

        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                    {
                        "email": "alex@example.com",
                        "password": "password123"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("login-token"));
    }
}
