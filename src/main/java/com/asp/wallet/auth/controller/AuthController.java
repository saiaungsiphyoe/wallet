package com.asp.wallet.auth.controller;

import com.asp.wallet.appuser.dto.AppUserResponse;
import com.asp.wallet.auth.dto.request.LoginRequest;
import com.asp.wallet.auth.dto.request.RegisterRequest;
import com.asp.wallet.auth.dto.response.AuthResponse;
import com.asp.wallet.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AppUserResponse> register(@RequestBody RegisterRequest appUser) {

        AppUserResponse AuthResponse = authService.register(appUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(AuthResponse.getId())
                .toUri();

        return ResponseEntity.created(location).body(AuthResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse AuthResponse = authService.verify(loginRequest);

        return ResponseEntity.ok(AuthResponse);
    }
}
