package com.asp.wallet.wallet.transactions.service;

import com.asp.wallet.wallet.transactions.model.WalletTransaction;

import java.math.BigDecimal;

public interface WalletTransactionWriteService {

    WalletTransaction deposit(Long toWalletId, BigDecimal amount);

    WalletTransaction withdraw(Long fromWalletId, BigDecimal amount);

    WalletTransaction transfer(Long fromWalletId, Long toWalletId, BigDecimal amount);
}
