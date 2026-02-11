package com.framedrop.upload_api.core.application.usecases;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.framedrop.upload_api.adapters.in.controller.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenUseCaseTest {

    private TokenUseCase tokenUseCase;
    private String validToken;

    @BeforeEach
    void setUp() {
        tokenUseCase = new TokenUseCase();

        // Cria um token JWT válido para testes
        validToken = JWT.create()
                .withClaim("client_id", "user123")
                .withClaim("sub", "John Doe")
                .sign(Algorithm.none());
    }

    @Test
    void shouldExtractUserFromValidToken() {
        UserDTO result = tokenUseCase.getUserFromToken(validToken);

        assertNotNull(result);
        assertEquals("user123", result.userId());
        assertEquals("John Doe", result.userName());
    }

    @Test
    void shouldExtractUserFromTokenWithBearerPrefix() {
        String tokenWithBearer = "Bearer " + validToken;

        UserDTO result = tokenUseCase.getUserFromToken(tokenWithBearer);

        assertNotNull(result);
        assertEquals("user123", result.userId());
        assertEquals("John Doe", result.userName());
    }

    @Test
    void shouldThrowExceptionWhenTokenIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                tokenUseCase.getUserFromToken(null)
        );
    }

    @Test
    void shouldThrowExceptionWhenTokenIsEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                tokenUseCase.getUserFromToken("")
        );
    }

    @Test
    void shouldThrowExceptionWhenTokenIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                tokenUseCase.getUserFromToken("   ")
        );
    }

    @Test
    void shouldHandleTokenWithDifferentClaims() {
        String token = JWT.create()
                .withClaim("client_id", "user456")
                .withClaim("sub", "Jane Smith")
                .sign(Algorithm.none());

        UserDTO result = tokenUseCase.getUserFromToken(token);

        assertEquals("user456", result.userId());
        assertEquals("Jane Smith", result.userName());
    }

    @Test
    void shouldRemoveBearerPrefixCorrectly() {
        String tokenWithBearer = "Bearer " + validToken;

        UserDTO result = tokenUseCase.getUserFromToken(tokenWithBearer);

        assertNotNull(result);
        assertEquals("user123", result.userId());
    }
}
