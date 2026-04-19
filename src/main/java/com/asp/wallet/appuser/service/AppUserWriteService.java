package com.asp.wallet.appuser.service;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.auth.dto.request.RegisterRequest;

public interface AppUserWriteService {

    AppUser createUser(RegisterRequest appUser);
}
