package com.asp.wallet.wallet.service;

import com.asp.wallet.wallet.model.Wallet;

import java.util.List;

public interface WalletReadService {
    List<Wallet> retrieveCurrentUserWallets();

    Wallet retrieveWallet(Long appUserId);

}
