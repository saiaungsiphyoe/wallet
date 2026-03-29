package com.asp.wallet.wallet.service;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.common.util.SecurityUtil;
import com.asp.wallet.wallet.model.Wallet;
import com.asp.wallet.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletReadServiceImpl implements WalletReadService{

    private final WalletRepository walletRepository;

    public WalletReadServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public List<Wallet> retrieveCurrentUserWallets() {
        AppUser appUser = SecurityUtil.getCurrentUser();

        return walletRepository.findByAppUserId(appUser.getId());
    }

    @Override
    public Wallet retrieveWallet(Long appUserId) {
        return null;
    }
}
