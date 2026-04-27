package com.internship.tool.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.List;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        String base64Secret = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", base64Secret);
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpirationMs", 3_600_000L);
    }

    @Test
    void generateToken_shouldExtractUsernameAndRoles() {
        String token = jwtUtil.generateToken("alice", List.of("ROLE_ADMIN", "ROLE_MANAGER"));

        assertEquals("alice", jwtUtil.extractUsername(token));
        assertTrue(jwtUtil.extractRoles(token).contains("ROLE_ADMIN"));
        assertTrue(jwtUtil.extractRoles(token).contains("ROLE_MANAGER"));
    }

    @Test
    void validateToken_shouldReturnTrueForMatchingUserDetails() {
        String token = jwtUtil.generateToken("bob", List.of("ROLE_VIEWER"));
        UserDetails userDetails = User.withUsername("bob").password("pwd").authorities("ROLE_VIEWER").build();

        boolean valid = jwtUtil.validateToken(token, userDetails);

        assertTrue(valid);
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpirationMs", 1L);
        String token = jwtUtil.generateToken("charlie", List.of("ROLE_VIEWER"));
        Thread.sleep(10);

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.validateToken(token));
    }
}
