package com.scetzhbook.exchangePipeline.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

class TokenServiceTest {

    private JwtEncoder jwtEncoder;
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        jwtEncoder = mock(JwtEncoder.class);
        tokenService = new TokenService(jwtEncoder);
    }

    @Test
    void mintTokenReturnsEncodedToken() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mocked-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = tokenService.mintToken("user1", List.of("TRADER"));
        
        assertEquals("mocked-token", token);
    }
}
