package com.asp.wallet.wallet.transactions.service;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.common.util.SecurityUtil;
import com.asp.wallet.wallet.model.Wallet;
import com.asp.wallet.wallet.model.WalletStatus;
import com.asp.wallet.wallet.service.WalletReadService;
import com.asp.wallet.wallet.transactions.exception.WalletTransactionException;
import com.asp.wallet.wallet.transactions.model.WalletTransaction;
import com.asp.wallet.wallet.transactions.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class WalletTransactionWriteServiceImpl implements WalletTransactionWriteService{

    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletReadService walletReadService;

    public WalletTransactionWriteServiceImpl(WalletTransactionRepository walletTransactionRepository,
                                             WalletReadService walletReadService) {
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletReadService = walletReadService;
    }

    @Transactional
    @Override
    public WalletTransaction deposit(Long toWalletId, BigDecimal amount) {

        LocalDate transactionDate = LocalDate.now();

        AppUser currentUser = SecurityUtil.getCurrentUser();

        Wallet toWallet = walletReadService.retrieveWalletById(toWalletId);
        toWallet.deposit(amount);

        WalletTransaction walletTransaction = WalletTransaction.depositTransaction(currentUser, toWallet, transactionDate, amount);

        return walletTransactionRepository.save(walletTransaction);
    }

    @Transactional
    @Override
    public WalletTransaction withdraw(Long fromWalletId, BigDecimal amount) {

        LocalDate transactionDate = LocalDate.now();

        AppUser currentUser = SecurityUtil.getCurrentUser();

        Wallet fromWallet = walletReadService.retrieveWalletById(fromWalletId);
        fromWallet.withdraw(amount);

        WalletTransaction walletTransaction = WalletTransaction.withdrawTransaction(currentUser, fromWallet, transactionDate, amount);

        return walletTransactionRepository.save(walletTransaction);
    }

    @Transactional
    @Override
    public WalletTransaction transfer(Long fromWalletId, Long toWalletId, BigDecimal amount) {

        LocalDate transactionDate = LocalDate.now();

        AppUser currentUser = SecurityUtil.getCurrentUser();

        Wallet fromWallet = walletReadService.retrieveWalletById(fromWalletId);
        Wallet toWallet = walletReadService.retrieveWalletById(toWalletId);

        validateForBalanceTransfer(fromWallet, toWallet, amount);

        fromWallet.withdraw(amount);
        toWallet.deposit(amount);

        WalletTransaction walletTransaction = WalletTransaction.transferTransaction(currentUser, fromWallet, toWallet, transactionDate, amount);

        return walletTransactionRepository.save(walletTransaction);
    }

    private void validateForBalanceTransfer(Wallet fromWallet, Wallet toWallet, BigDecimal amount) {
        if (fromWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletTransactionException("Source wallet is inactive.");
        }

        if (toWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletTransactionException("Target wallet is inactive.");
        }

        if (fromWallet.getId().equals(toWallet.getId())) {
            throw new WalletTransactionException("Cannot transfer to same wallet.");
        }

        if (fromWallet.getCurrency() != toWallet.getCurrency()) {
            throw new WalletTransactionException("Currency mismatch between source and target wallets.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletTransactionException("Transaction amount cannot be zero.");
        }

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new WalletTransactionException("Insufficient balance.");
        }
    }
}
