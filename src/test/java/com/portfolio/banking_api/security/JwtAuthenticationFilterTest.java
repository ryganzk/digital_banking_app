package com.portfolio.banking_api.security;

import com.portfolio.banking_api.service.JWTService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JWTService jwtService;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthHeader_doesNotSetAuthentication() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidBearerToken_doesNotSetAuthentication() throws Exception {
        request.addHeader("Authorization", "Bearer invalid-token");
        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService).isTokenValid("invalid-token");
    }

    @Test
    void validBearerToken_setsAuthentication() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractUserId("valid-token")).thenReturn("user-123");

        filter.doFilter(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("user-123", auth.getPrincipal());
        verify(jwtService).isTokenValid("valid-token");
        verify(jwtService).extractUserId("valid-token");
    }

    @Test
    void nonBearerHeader_isIgnored() throws Exception {
        request.addHeader("Authorization", "Basic abc123");

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
