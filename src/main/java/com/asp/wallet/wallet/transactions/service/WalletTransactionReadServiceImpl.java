package com.asp.wallet.wallet.transactions.service;

import com.asp.wallet.wallet.service.WalletReadService;
import com.asp.wallet.wallet.transactions.exception.WalletTransactionNotFoundException;
import com.asp.wallet.wallet.transactions.model.WalletTransaction;
import com.asp.wallet.wallet.transactions.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletTransactionReadServiceImpl implements WalletTransactionReadService{

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletReadService walletReadService;

    @Override
    @Transactional(readOnly = true)
    public Page<WalletTransaction> retrieveWalletTransactions(Long walletId, Pageable pageable) {
        walletReadService.retrieveWalletById(walletId);

        return walletTransactionRepository.findAllByWalletId(walletId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletTransaction retrieveWalletTransaction(Long walletId, Long transactionId) {
        walletReadService.retrieveWalletById(walletId);

        return walletTransactionRepository.findByIdAndWalletId(transactionId, walletId)
                .orElseThrow(() -> new WalletTransactionNotFoundException(
                        "Transaction not found for wallet: " + walletId
                ));
    }
}
