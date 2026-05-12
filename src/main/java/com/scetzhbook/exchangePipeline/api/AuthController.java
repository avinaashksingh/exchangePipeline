package com.scetzhbook.exchangePipeline.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scetzhbook.exchangePipeline.security.TokenService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final TokenService tokenService;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    // Demo-only: mints tokens using the app's local private key.
    @PostMapping("/token")
    public ResponseEntity<String> token(
        @RequestParam(defaultValue = "trader") String user,
        @RequestParam(defaultValue = "TRADER") String role
    ) {
        String normalizedRole = role.trim().toUpperCase();
        if (!normalizedRole.equals("TRADER") && !normalizedRole.equals("ADMIN")) {
            return ResponseEntity.badRequest().body("role must be TRADER or ADMIN");
        }
        return ResponseEntity.ok(tokenService.mintToken(user, List.of(normalizedRole)));
    }
}

