package com.portfolio.banking_api.config;

import com.portfolio.banking_api.controller.AccountController;
import com.portfolio.banking_api.controller.AuthController;
import com.portfolio.banking_api.controller.HealthController;
import com.portfolio.banking_api.dto.AuthResponse;
import com.portfolio.banking_api.service.AccountService;
import com.portfolio.banking_api.service.AuthService;
import com.portfolio.banking_api.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
    HealthController.class,
    AuthController.class,
    AccountController.class
})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JWTService jwtService;

    @Test
    void passwordEncoder_hashesAndMatches() {
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);

        String password = "password123";
        String hashedPassword = passwordEncoder.encode(password);

        assertTrue(passwordEncoder.matches(password, hashedPassword));
        assertFalse(passwordEncoder.matches("wrong_password", hashedPassword));
    }

    @Test
    void health_isPublic() throws Exception {
        mockMvc.perform(get("/health"))
            .andExpect(status().isOk());
    }

    @Test
    void authLogin_isPublic() throws Exception {
        when(authService.login(any())).thenReturn(
            new AuthResponse("fake-token", "Bearer", 3600000L)
        );

        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "email": "alex@example.com",
                    "password": "password123"
                }
                """))
            .andExpect(status().isOk());
    }

    @Test
    void accounts_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
            .andExpect(status().isForbidden());
    }
}
