package com.asp.wallet.wallet.transactions.service;

import com.asp.wallet.wallet.transactions.model.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WalletTransactionReadService {

    Page<WalletTransaction> retrieveWalletTransactions(Long walletId, Pageable pageable);

    WalletTransaction retrieveWalletTransaction(Long walletId, Long transactionId);
}
