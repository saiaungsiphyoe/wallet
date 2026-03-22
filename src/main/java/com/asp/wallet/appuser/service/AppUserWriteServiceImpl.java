package com.asp.wallet.appuser.service;

import com.asp.wallet.appuser.dto.AppUserResponse;
import com.asp.wallet.auth.dto.request.RegisterRequest;
import com.asp.wallet.appuser.mapper.AppUserMapper;
import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.appuser.repository.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
public class AppUserWriteServiceImpl implements AppUserWriteService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;

    public AppUserWriteServiceImpl(AppUserRepository appUserRepository,
                                   AppUserMapper appUserMapper) {

        this.appUserRepository = appUserRepository;
        this.appUserMapper = appUserMapper;
    }

    @Override
    public AppUserResponse createUser(RegisterRequest request) {

        AppUser createdUser = appUserRepository.save(appUserMapper.toEntity(request));

        return appUserMapper.toResponse(createdUser);
    }
}
