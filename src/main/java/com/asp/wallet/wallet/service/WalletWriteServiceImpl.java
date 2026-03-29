package com.asp.wallet.wallet.service;

import com.asp.wallet.appuser.model.AppUser;
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
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        wallet.activate();
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet deactivateWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        wallet.deactivate();
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet suspendWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        wallet.suspend();
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet closeWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        wallet.close();
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet rejectWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        wallet.reject();
        return walletRepository.save(wallet);
    }
}
