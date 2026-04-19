package com.asp.wallet.appuser.service;

import com.asp.wallet.appuser.model.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppUserReadService {

    Page<AppUser> getAllUsers(Pageable pageable);

    AppUser getUserById(Long id);
}
