package com.asp.wallet.appuser.service;

import com.asp.wallet.auth.dto.request.RegisterRequest;
import com.asp.wallet.appuser.mapper.AppUserMapper;
import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.appuser.repository.AppUserRepository;
import com.asp.wallet.wallet.service.WalletWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserWriteServiceImpl implements AppUserWriteService {

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;
    private final WalletWriteService walletWriteService;

    public AppUserWriteServiceImpl(AppUserRepository appUserRepository,
                                   AppUserMapper appUserMapper,
                                   WalletWriteService walletWriteService) {
        this.appUserRepository = appUserRepository;
        this.appUserMapper = appUserMapper;
        this.walletWriteService = walletWriteService;
    }

    @Override
    @Transactional
    public AppUser createUser(RegisterRequest request) {
        AppUser appUser = appUserMapper.toEntity(request);
        AppUser savedUser = appUserRepository.save(appUser);

        walletWriteService.createDefaultWallet(savedUser);

        return savedUser;
    }
}
