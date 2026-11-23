package com.asp.wallet.wallet.transactions;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.appuser.repository.AppUserRepository;
import com.asp.wallet.common.security.UserPrincipal;
import com.asp.wallet.wallet.exception.WalletOperationException;
import com.asp.wallet.wallet.model.Currency;
import com.asp.wallet.wallet.model.Wallet;
import com.asp.wallet.wallet.model.WalletStatus;
import com.asp.wallet.wallet.repository.WalletRepository;
import com.asp.wallet.wallet.transactions.model.WalletTransaction;
import com.asp.wallet.wallet.transactions.model.WalletTransactionType;
import com.asp.wallet.wallet.transactions.repository.WalletTransactionRepository;
import com.asp.wallet.wallet.transactions.service.WalletTransactionWriteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WalletTransactionWriteServiceIntegrationTest {

    @Autowired
    private WalletTransactionWriteService walletTransactionWriteService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    private Long appUserId;
    private Long walletId;
    private Long transactionId;
    private Long recipientAppUserId;
    private Long recipientWalletId;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        String uniqueId = UUID.randomUUID().toString();
        String testUserPrefix = "wallet-transaction-test-";

        AppUser appUser = new AppUser();
        appUser.setUsername(testUserPrefix + uniqueId);
        appUser.setFirstName("Wallet Transaction");
        appUser.setLastName("Test");
        appUser.setEmail(testUserPrefix + uniqueId + "@example.com");
        appUser.setPassword("test-password");

        appUserId = appUserRepository.saveAndFlush(appUser).getId();

        Wallet wallet = new Wallet(appUser, Currency.MMK);
        wallet.activate();

        walletId = walletRepository.saveAndFlush(wallet).getId();

        UserPrincipal userPrincipal = new UserPrincipal(appUser);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void shouldDepositMoneyAndCreateTransactionRecord() {
        BigDecimal depositAmount = new BigDecimal("50000.00");

        WalletTransaction transaction =
                walletTransactionWriteService.deposit(walletId, depositAmount);

        transactionId = transaction.getId();

        Wallet updatedWallet = walletRepository.findById(walletId).orElseThrow();

        assertEquals(
                0,
                updatedWallet.getBalance().compareTo(depositAmount)
        );

        assertNotNull(transaction.getId());
        assertEquals(WalletTransactionType.DEPOSIT, transaction.getTransactionType());
        assertEquals(0, transaction.getAmount().compareTo(depositAmount));
        assertEquals(walletId, transaction.getToWallet().getId());
        assertNull(transaction.getFromWallet());
        assertEquals(appUserId, transaction.getInitiator().getId());

        assertTrue(walletTransactionRepository.existsById(transaction.getId()));
    }

    @Test
    void shouldWithdrawMoneyAndCreateTransactionRecord() {
        fundWallet(walletId, new BigDecimal("100000.00"));

        BigDecimal withdrawalAmount = new BigDecimal("30000.00");
        BigDecimal expectedBalance = new BigDecimal("70000.00");

        WalletTransaction transaction =
                walletTransactionWriteService.withdraw(walletId, withdrawalAmount);

        transactionId = transaction.getId();

        Wallet updatedWallet = walletRepository.findById(walletId).orElseThrow();

        assertEquals(
                0,
                updatedWallet.getBalance().compareTo(expectedBalance)
        );

        assertNotNull(transaction.getId());
        assertEquals(WalletTransactionType.WITHDRAW, transaction.getTransactionType());
        assertEquals(0, transaction.getAmount().compareTo(withdrawalAmount));
        assertEquals(walletId, transaction.getFromWallet().getId());
        assertNull(transaction.getToWallet());
        assertEquals(appUserId, transaction.getInitiator().getId());

        assertTrue(walletTransactionRepository.existsById(transaction.getId()));
    }

    @Test
    void shouldRejectWithdrawalWhenBalanceIsInsufficient() {
        BigDecimal initialBalance = new BigDecimal("10000.00");
        BigDecimal withdrawalAmount = new BigDecimal("20000.00");

        fundWallet(walletId, initialBalance);

        long transactionCountBefore =
                walletTransactionRepository.countByFromWallet_IdOrToWallet_Id(walletId, walletId);

        assertThrows(
                WalletOperationException.class,
                () -> walletTransactionWriteService.withdraw(walletId, withdrawalAmount)
        );

        Wallet updatedWallet = walletRepository.findById(walletId).orElseThrow();

        assertEquals(
                0,
                updatedWallet.getBalance().compareTo(initialBalance)
        );

        assertEquals(
                transactionCountBefore,
                walletTransactionRepository.countByFromWallet_IdOrToWallet_Id(walletId, walletId)
        );
    }

    @Test
    void shouldRejectTransferWhenSourceWalletHasInsufficientBalance() {
        BigDecimal sourceInitialBalance = new BigDecimal("10000.00");
        BigDecimal destinationInitialBalance = new BigDecimal("20000.00");
        BigDecimal transferAmount = new BigDecimal("30000.00");

        fundWallet(walletId, sourceInitialBalance);

        createRecipientWallet(Currency.MMK, destinationInitialBalance);

        long transactionCountBefore =
                walletTransactionRepository.countByFromWallet_IdOrToWallet_Id(
                        walletId, recipientWalletId);

        assertThrows(WalletOperationException.class,
                () -> walletTransactionWriteService.transfer(walletId, recipientWalletId, transferAmount));

        Wallet updatedSourceWallet = walletRepository.findById(walletId).orElseThrow();
        Wallet updatedDestinationWallet = walletRepository.findById(recipientWalletId).orElseThrow();

        assertEquals(0, updatedSourceWallet.getBalance().compareTo(sourceInitialBalance));
        assertEquals(0, updatedDestinationWallet.getBalance().compareTo(destinationInitialBalance));

        assertEquals(
                transactionCountBefore,
                walletTransactionRepository.countByFromWallet_IdOrToWallet_Id(
                        walletId, recipientWalletId)
        );
    }

    @Test
    void shouldRejectTransferWhenWalletCurrenciesDoNotMatch() {
        BigDecimal sourceInitialBalance = new BigDecimal("100000.00");
        BigDecimal destinationInitialBalance = new BigDecimal("200.00");
        BigDecimal transferAmount = new BigDecimal("30000.00");

        fundWallet(walletId, sourceInitialBalance);

        createRecipientWallet(Currency.USD, destinationInitialBalance);

        long transactionCountBefore =
                walletTransactionRepository.countByFromWallet_IdOrToWallet_Id(
                        walletId, recipientWalletId);

        assertThrows(
                WalletOperationException.class,
                () -> walletTransactionWriteService.transfer(
                        walletId, recipientWalletId, transferAmount
                )
        );

        Wallet updatedSourceWallet = walletRepository.findById(walletId).orElseThrow();
        Wallet updatedDestinationWallet = walletRepository.findById(recipientWalletId).orElseThrow();

        assertEquals(0, updatedSourceWallet.getBalance().compareTo(sourceInitialBalance));
        assertEquals(0, updatedDestinationWallet.getBalance().compareTo(destinationInitialBalance));

        assertEquals(
                transactionCountBefore,
                walletTransactionRepository.countByFromWallet_IdOrToWallet_Id(
                        walletId, recipientWalletId)
        );
    }

    @Test
    void shouldTransferMoneyAndCreateTransactionRecord() {
        BigDecimal sourceInitialBalance = new BigDecimal("100000.00");
        BigDecimal destinationInitialBalance = new BigDecimal("20000.00");
        BigDecimal transferAmount = new BigDecimal("30000.00");

        fundWallet(walletId, sourceInitialBalance);

        createRecipientWallet(Currency.MMK, destinationInitialBalance);

        WalletTransaction transaction =
                walletTransactionWriteService.transfer(walletId, recipientWalletId, transferAmount);

        transactionId = transaction.getId();

        Wallet updatedSourceWallet = walletRepository.findById(walletId).orElseThrow();
        Wallet updatedDestinationWallet = walletRepository.findById(recipientWalletId).orElseThrow();

        BigDecimal expectedSourceBalance = new BigDecimal("70000.00");
        BigDecimal expectedDestinationBalance = new BigDecimal("50000.00");

        assertEquals(0, updatedSourceWallet.getBalance().compareTo(expectedSourceBalance));
        assertEquals(0, updatedDestinationWallet.getBalance().compareTo(expectedDestinationBalance));

        assertNotNull(transaction.getId());
        assertEquals(WalletTransactionType.TRANSFER, transaction.getTransactionType());
        assertEquals(0, transaction.getAmount().compareTo(transferAmount));
        assertEquals(walletId, transaction.getFromWallet().getId());
        assertEquals(recipientWalletId, transaction.getToWallet().getId());
        assertEquals(appUserId, transaction.getInitiator().getId());

        assertEquals(
                1,
                walletTransactionRepository.countByFromWallet_IdOrToWallet_Id(
                        walletId, recipientWalletId)
        );
    }

    @Test
    void shouldRejectTransferWhenSourceWalletIsSuspended() {
        BigDecimal sourceInitialBalance = new BigDecimal("100000.00");
        BigDecimal destinationInitialBalance = new BigDecimal("20000.00");
        BigDecimal transferAmount = new BigDecimal("30000.00");

        fundWallet(walletId, sourceInitialBalance);

        createRecipientWallet(Currency.MMK, destinationInitialBalance);

        transactionTemplate.executeWithoutResult(status -> {
            Wallet sourceWallet = walletRepository.findById(walletId).orElseThrow();
            sourceWallet.suspend();
        });

        long transactionCountBefore =
                walletTransactionRepository.countByFromWallet_IdOrToWallet_Id(
                        walletId, recipientWalletId);

        assertThrows(
                WalletOperationException.class,
                () -> walletTransactionWriteService.transfer(
                        walletId, recipientWalletId, transferAmount)
        );

        Wallet updatedSourceWallet = walletRepository.findById(walletId).orElseThrow();
        Wallet updatedDestinationWallet =
                walletRepository.findById(recipientWalletId).orElseThrow();

        assertEquals(WalletStatus.SUSPENDED, updatedSourceWallet.getStatus());
        assertEquals(WalletStatus.ACTIVE, updatedDestinationWallet.getStatus());
        assertEquals(0, updatedSourceWallet.getBalance().compareTo(sourceInitialBalance));
        assertEquals(0, updatedDestinationWallet.getBalance().compareTo(destinationInitialBalance));

        assertEquals(
                transactionCountBefore,
                walletTransactionRepository.countByFromWallet_IdOrToWallet_Id(
                        walletId, recipientWalletId)
        );
    }

    @Test
    void shouldRejectTransferWhenDestinationWalletIsSuspended() {
        BigDecimal sourceInitialBalance = new BigDecimal("100000.00");
        BigDecimal destinationInitialBalance = new BigDecimal("20000.00");
        BigDecimal transferAmount = new BigDecimal("30000.00");

        fundWallet(walletId, sourceInitialBalance);

        createRecipientWallet(Currency.MMK, destinationInitialBalance);

        transactionTemplate.executeWithoutResult(status -> {
            Wallet destinationWallet =
                    walletRepository.findById(recipientWalletId).orElseThrow();
            destinationWallet.suspend();
        });

        long transactionCountBefore =
                walletTransactionRepository.countByFromWallet_IdOrToWallet_Id(
                        walletId, recipientWalletId);

        assertThrows(
                WalletOperationException.class,
                () -> walletTransactionWriteService.transfer(
                        walletId, recipientWalletId, transferAmount)
        );

        Wallet updatedSourceWallet = walletRepository.findById(walletId).orElseThrow();
        Wallet updatedDestinationWallet =
                walletRepository.findById(recipientWalletId).orElseThrow();

        assertEquals(WalletStatus.ACTIVE, updatedSourceWallet.getStatus());
        assertEquals(WalletStatus.SUSPENDED, updatedDestinationWallet.getStatus());
        assertEquals(0, updatedSourceWallet.getBalance().compareTo(sourceInitialBalance));
        assertEquals(0, updatedDestinationWallet.getBalance().compareTo(destinationInitialBalance));

        assertEquals(
                transactionCountBefore,
                walletTransactionRepository.countByFromWallet_IdOrToWallet_Id(
                        walletId, recipientWalletId)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();

        if (transactionId == null
                && walletId == null
                && appUserId == null
                && recipientWalletId == null
                && recipientAppUserId == null) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            if (transactionId != null) {
                walletTransactionRepository.deleteById(transactionId);
            }

            if (walletId != null) {
                walletRepository.deleteById(walletId);
            }

            if (recipientWalletId != null) {
                walletRepository.deleteById(recipientWalletId);
            }

            if (appUserId != null) {
                appUserRepository.deleteById(appUserId);
            }

            if (recipientAppUserId != null) {
                appUserRepository.deleteById(recipientAppUserId);
            }
        });
    }

    private void fundWallet(Long targetWalletId, BigDecimal amount) {
        transactionTemplate.executeWithoutResult(status -> {
            Wallet wallet = walletRepository.findById(targetWalletId).orElseThrow();
            wallet.deposit(amount);
        });
    }

    private void createRecipientWallet(Currency currency, BigDecimal initialBalance) {
        String uniqueId = UUID.randomUUID().toString();
        String testUserPrefix = "wallet-recipient-test-";

        AppUser recipient = new AppUser();
        recipient.setUsername(testUserPrefix + uniqueId);
        recipient.setFirstName("Recipient");
        recipient.setLastName("Test");
        recipient.setEmail(testUserPrefix + uniqueId + "@example.com");
        recipient.setPassword("test-password");

        recipientAppUserId = appUserRepository.saveAndFlush(recipient).getId();

        Wallet recipientWallet = new Wallet(recipient, currency);
        recipientWallet.activate();

        recipientWalletId = walletRepository.saveAndFlush(recipientWallet).getId();

        fundWallet(recipientWalletId, initialBalance);
    }
}
