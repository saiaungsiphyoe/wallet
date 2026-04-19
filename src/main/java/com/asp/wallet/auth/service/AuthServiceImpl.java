package com.asp.wallet.auth.service;

import com.asp.wallet.appuser.dto.AppUserResponse;
import com.asp.wallet.appuser.mapper.AppUserMapper;
import com.asp.wallet.auth.dto.request.LoginRequest;
import com.asp.wallet.auth.dto.request.RegisterRequest;
import com.asp.wallet.auth.dto.response.AuthResponse;
import com.asp.wallet.appuser.service.AppUserWriteService;
import com.asp.wallet.common.security.JwtService;
import com.asp.wallet.common.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AppUserWriteService appUserWriteService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserMapper appUserMapper;

    public AuthServiceImpl(AppUserWriteService appUserWriteService,
                           PasswordEncoder encoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService, AppUserMapper appUserMapper) {

        this.appUserWriteService = appUserWriteService;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.appUserMapper = appUserMapper;
    }

    @Override
    public AppUserResponse register(RegisterRequest request) {

        request.setPassword(encoder.encode(request.getPassword()));

        return appUserMapper.toResponse(appUserWriteService.createUser(request));
    }

    @Override
    public AuthResponse verify(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(user);

        return new AuthResponse(accessToken, "Bearer");
    }
}
