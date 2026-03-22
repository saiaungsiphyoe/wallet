package com.asp.wallet.appuser.service;

import com.asp.wallet.appuser.dto.AppUserResponse;
import com.asp.wallet.auth.dto.request.RegisterRequest;

public interface AppUserWriteService {

    AppUserResponse createUser(RegisterRequest appUser);
}
