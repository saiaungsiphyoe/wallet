package com.asp.wallet.wallet.service;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.common.util.SecurityUtil;
import com.asp.wallet.wallet.exception.WalletNotFoundException;
import com.asp.wallet.wallet.model.Currency;
import com.asp.wallet.wallet.model.Wallet;
import com.asp.wallet.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

@Service
public class WalletWriteServiceImpl implements WalletWriteService{

    private final WalletRepository walletRepository;

    public WalletWriteServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public void createDefaultWallet(AppUser appUser) {
        Wallet wallet = new Wallet(appUser, Currency.MMK);
        walletRepository.save(wallet);
    }

    @Override
    public Wallet activateWallet(Long walletId) {
        Wallet wallet = retrieveCurrentUserWallet(walletId);
        wallet.activate();
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet suspendWallet(Long walletId) {
        Wallet wallet = retrieveCurrentUserWallet(walletId);
        wallet.suspend();
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet closeWallet(Long walletId) {
        Wallet wallet = retrieveCurrentUserWallet(walletId);
        wallet.close();

        return walletRepository.save(wallet);
    }

    @Override
    public Wallet rejectWallet(Long walletId) {
        Wallet wallet = retrieveCurrentUserWallet(walletId);
        wallet.reject();
        return walletRepository.save(wallet);
    }

    private Wallet retrieveCurrentUserWallet(Long walletId) {
        AppUser appUser = SecurityUtil.getCurrentUser();

        return walletRepository.findByIdAndAppUser_Id(walletId, appUser.getId())
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }
}
