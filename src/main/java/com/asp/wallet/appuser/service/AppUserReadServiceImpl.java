package com.asp.wallet.appuser.service;

import com.asp.wallet.appuser.dto.AppUserResponse;
import com.asp.wallet.appuser.mapper.AppUserMapper;
import com.asp.wallet.appuser.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AppUserReadServiceImpl implements AppUserReadService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;

    public AppUserReadServiceImpl(AppUserRepository appUserRepository,
                                  AppUserMapper appUserMapper) {
        this.appUserRepository = appUserRepository;
        this.appUserMapper = appUserMapper;
    }

    @Override
    public Page<AppUserResponse> getAllUsers(Pageable pageable) {
        return appUserRepository.findAll(pageable)
                .map(appUserMapper::toResponse);
    }
}
