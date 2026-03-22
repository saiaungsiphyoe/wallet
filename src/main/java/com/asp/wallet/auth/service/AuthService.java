package com.asp.wallet.auth.service;

import com.asp.wallet.appuser.dto.AppUserResponse;
import com.asp.wallet.auth.dto.request.RegisterRequest;
import com.asp.wallet.auth.dto.request.LoginRequest;
import com.asp.wallet.auth.dto.response.AuthResponse;

public interface AuthService {

    AppUserResponse register(RegisterRequest appUser);

    AuthResponse verify(LoginRequest loginRequest);
}
