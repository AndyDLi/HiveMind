package com.andydli.hivemind.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private JwtService jwtService;
    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hs256-algorithm-minimum-256-bits";
    private static final long EXPIRATION = 3600000; // 1 hour in milliseconds
    private static final Long USER_ID = 123L;
    private static final String EMAIL = "test@example.com";
    private static final String INVALID_TOKEN = "invalid.token";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    @Test
    @DisplayName("generateToken Should Return a Valid JWT")
    void generateToken_shouldReturnValidJWT() {
        String token = jwtService.generateToken(USER_ID, EMAIL);

        assertNotNull(token, "Token Should Not Be Null");
        assertFalse(token.isEmpty(), "Token Should Not Be Empty");
    }

    @Test
    @DisplayName("JWT Should Include Correct UserId, Email, and Expiration Claims")
    void jwtContainsCorrect_userId_email_expiration() {
        String token = jwtService.generateToken(USER_ID, EMAIL);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userIdClaim = claims.getSubject();
        String emailClaim = claims.get("email", String.class);
        Date expirationClaim = claims.getExpiration();

        assertNotNull(userIdClaim, "User Id Should Not Be Null");
        assertFalse(userIdClaim.isEmpty(), "User Id Should Not Be Empty");
        assertEquals(USER_ID.toString(), userIdClaim, "User Id Should Match");

        assertNotNull(emailClaim, "Email Should Not Be Null");
        assertFalse(emailClaim.isEmpty(), "Email Should Not Be Empty");
        assertEquals(EMAIL, emailClaim, "Email Should Match");

        assertNotNull(expirationClaim, "Expiration Should Not Be Null");
        assertTrue(expirationClaim.after(new Date()), "Expiration Should Be In The Future");
    }

    @Test
    @DisplayName("validateToken With Valid Token Should Return True")
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtService.generateToken(USER_ID, EMAIL);

        assertTrue(jwtService.validateToken(token), "Valid Token Should Be Accepted");
    }

    @Test
    @DisplayName("validateToken With Invalid Token Should Return False")
    void validateToken_withInvalidToken_shouldReturnFalse() {
        assertFalse(jwtService.validateToken(null), "Null Token Should Be Rejected");
        assertFalse(jwtService.validateToken(""), "Empty Token Should Be Rejected");
        assertFalse(jwtService.validateToken("     "), "Blank Token Should Be Rejected");
        assertFalse(jwtService.validateToken(INVALID_TOKEN), "Invalid Token Should Be Rejected");
    }

    @Test
    @DisplayName("validateToken With Expired Token Should Return False")
    void validateToken_withExpiredToken_shouldReturnFalse() throws InterruptedException {
        JwtService shortLivedJwtService = new JwtService(SECRET, -1000);
        String token = shortLivedJwtService.generateToken(USER_ID, EMAIL);

        assertFalse(shortLivedJwtService.validateToken(token), "Expired Token Should Be Rejected");
    }

    @Test
    @DisplayName("validateToken With Tampered Token Should Return False")
    void validateToken_withTamperedToken_shouldReturnFalse() {
        String token = jwtService.generateToken(USER_ID, EMAIL);
        String tamperedToken = token.substring(0, token.length() - 8) + "TAMPERED";

        assertFalse(jwtService.validateToken(tamperedToken), "Tampered Token Should Be Rejected");
    }

    @Test
    @DisplayName("extractUserId With Valid Token Should Return Correct UserId")
    void extractUserId_withValidToken_shouldReturnCorrectUserId() {
        String token = jwtService.generateToken(USER_ID, EMAIL);
        Long extractedUserId = jwtService.extractUserId(token);

        assertEquals(USER_ID, extractedUserId, "Extracted UserId Should Match");
    }

    @Test
    @DisplayName("extractUserId With Invalid Token Should Throw Exception")
    void extractUserId_withInvalidToken_shouldThrowException() {
        assertThrows(Exception.class, () -> jwtService.extractUserId(null), "Null Token Should Throw Exception");
        assertThrows(Exception.class, () -> jwtService.extractUserId(""), "Empty Token Should Throw Exception");
        assertThrows(Exception.class, () -> jwtService.extractUserId("     "), "Blank Token Should Throw Exception");
        assertThrows(Exception.class, () -> jwtService.extractUserId(INVALID_TOKEN), "Invalid Token Should Throw Exception");
    }
}