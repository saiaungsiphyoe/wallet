package com.asp.wallet.appuser.service;

import com.asp.wallet.appuser.dto.AppUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppUserReadService {

    Page<AppUserResponse> getAllUsers(Pageable pageable);
}
