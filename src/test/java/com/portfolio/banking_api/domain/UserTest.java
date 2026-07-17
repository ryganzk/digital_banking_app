package com.portfolio.banking_api.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {
    @Test
    void constructor_setAllFields() {
        User user = new User(
            "alex@example.com",
            "password123",
            "Alex",
            "",
            "Morgan",
            "CUSTOMER"
        );
        assertNotNull(user.getId());
        assertEquals("alex@example.com", user.getEmail());
        assertEquals("password123", user.getPasswordHash());
        assertEquals("Alex", user.getFirstName());
        assertEquals("", user.getMiddleName());
        assertEquals("Morgan", user.getLastName());
        assertEquals("CUSTOMER", user.getRole());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void noArgConstructor_canBeInvoked() {
        User user = new User();
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getPasswordHash());
        assertNull(user.getFirstName());
        assertNull(user.getMiddleName());
        assertNull(user.getLastName());
        assertNull(user.getRole());
        assertNull(user.getCreatedAt());
    }
}