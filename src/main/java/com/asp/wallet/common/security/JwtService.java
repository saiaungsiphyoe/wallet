package com.asp.wallet.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(UserDetails user);

    String extractUsername(String token);

    boolean validateToken(String token, UserDetails user);
}
