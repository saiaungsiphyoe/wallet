package com.asp.wallet.wallet.transactions.service;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.common.util.SecurityUtil;
import com.asp.wallet.wallet.model.Wallet;
import com.asp.wallet.wallet.service.WalletReadService;
import com.asp.wallet.wallet.exception.WalletOperationException;
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
        Wallet toWallet = walletReadService.retrieveWalletForTransfer(toWalletId);

        validateForBalanceTransfer(fromWallet, toWallet);

        fromWallet.withdraw(amount);
        toWallet.deposit(amount);

        WalletTransaction walletTransaction = WalletTransaction.transferTransaction(currentUser, fromWallet, toWallet, transactionDate, amount);

        return walletTransactionRepository.save(walletTransaction);
    }

    private void validateForBalanceTransfer(Wallet fromWallet, Wallet toWallet) {
        if (fromWallet.getId().equals(toWallet.getId())) {
            throw new WalletOperationException(
                    "Cannot transfer to same wallet."
            );
        }

        if (fromWallet.getCurrency() != toWallet.getCurrency()) {
            throw new WalletOperationException(
                    "Currency mismatch between source and target wallets."
            );
        }
    }
}
