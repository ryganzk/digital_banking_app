package com.portfolio.banking_api.exception;

import com.portfolio.banking_api.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFound_mapsTo404() {
        ErrorResponse body = handler.notFound(new AccountNotFoundException("Account not found"));

        assertEquals(404, body.status());
        assertEquals("NOT_FOUND", body.error());
        assertEquals("Account not found", body.message());
        assertNotNull(body.timestamp());
    }

    @Test
    void badRequest_mapsTo400() {
        ErrorResponse body = handler.badRequest(new BadRequestException("Bad request"));

        assertEquals(400, body.status());
        assertEquals("BAD_REQUEST", body.error());
        assertEquals("Bad request", body.message());
        assertNotNull(body.timestamp());
    }

    @Test
    void conflict_mapsTo409() {
        ErrorResponse body = handler.conflict(new ConflictException("Conflict"));

        assertEquals(409, body.status());
        assertEquals("CONFLICT", body.error());
        assertEquals("Conflict", body.message());
        assertNotNull(body.timestamp());
    }

    @Test
    void insufficientFunds_mapsTo400() {
        ErrorResponse body = handler.insufficientFunds(new InsufficientFundsException("Insufficient funds"));

        assertEquals(400, body.status());
        assertEquals("INSUFFICIENT_FUNDS", body.error());
        assertEquals("Insufficient funds", body.message());
        assertNotNull(body.timestamp());
    }

    @Test
    void invalidCredentials_mapsTo401() {
        ErrorResponse body = handler.invalidCredentials(new InvalidCredentialsException("Invalid credentials"));

        assertEquals(401, body.status());
        assertEquals("INVALID_CREDENTIALS", body.error());
        assertEquals("Invalid credentials", body.message());
        assertNotNull(body.timestamp());
    }

    @Test
    void handleUnexpected_hideInternalDetails() {
        ErrorResponse body = handler.handleUnexpected(new RuntimeException("Unexpected error"));

        assertEquals(500, body.status());
        assertEquals("INTERNAL_ERROR", body.error());
        assertEquals("An unexpected error occurred", body.message());
        assertNotNull(body.timestamp());
    }
}