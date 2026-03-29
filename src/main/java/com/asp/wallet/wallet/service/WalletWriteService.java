package com.asp.wallet.wallet.service;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.wallet.model.Wallet;

public interface WalletWriteService {

    void createDefaultWallet(AppUser appUser);

    Wallet activateWallet(Long walletId);

    Wallet deactivateWallet(Long walletId);

    Wallet suspendWallet(Long walletId);;

    Wallet closeWallet(Long walletId);

    Wallet rejectWallet(Long walletId);
}
