package com.asp.wallet.wallet.transactions;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.appuser.repository.AppUserRepository;
import com.asp.wallet.common.security.UserPrincipal;
import com.asp.wallet.wallet.exception.WalletNotFoundException;
import com.asp.wallet.wallet.model.Currency;
import com.asp.wallet.wallet.model.Wallet;
import com.asp.wallet.wallet.repository.WalletRepository;
import com.asp.wallet.wallet.transactions.exception.WalletTransactionNotFoundException;
import com.asp.wallet.wallet.transactions.model.WalletTransaction;
import com.asp.wallet.wallet.transactions.model.WalletTransactionType;
import com.asp.wallet.wallet.transactions.repository.WalletTransactionRepository;
import com.asp.wallet.wallet.transactions.service.WalletTransactionReadService;
import com.asp.wallet.wallet.transactions.service.WalletTransactionWriteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WalletTransactionReadServiceIntegrationTest {

    @Autowired
    private WalletTransactionReadService walletTransactionReadService;

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
    private Long recipientAppUserId;
    private Long recipientWalletId;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        String uniqueId = UUID.randomUUID().toString();

        AppUser appUser = createTestUser("wallet-read-test-", uniqueId);
        appUserId = appUserRepository.saveAndFlush(appUser).getId();

        Wallet wallet = new Wallet(appUser, Currency.MMK);
        wallet.activate();
        walletId = walletRepository.saveAndFlush(wallet).getId();

        AppUser recipient = createTestUser("wallet-read-recipient-", uniqueId);
        recipientAppUserId = appUserRepository.saveAndFlush(recipient).getId();

        Wallet recipientWallet = new Wallet(recipient, Currency.MMK);
        recipientWallet.activate();
        recipientWalletId = walletRepository.saveAndFlush(recipientWallet).getId();

        fundWallet(recipientWalletId, new BigDecimal("100000.00"));

        authenticateAs(appUserId);
    }

    @Test
    void shouldRetrieveTransactionsForOwnedWallet() {
        walletTransactionWriteService.deposit(walletId, new BigDecimal("10000.00"));
        walletTransactionWriteService.deposit(walletId, new BigDecimal("20000.00"));
        walletTransactionWriteService.withdraw(walletId, new BigDecimal("5000.00"));

        Page<WalletTransaction> transactions =
                walletTransactionReadService.retrieveWalletTransactions(
                        walletId, PageRequest.of(0, 10)
                );

        assertEquals(3, transactions.getTotalElements());
        assertEquals(3, transactions.getContent().size());
    }

    @Test
    void shouldRetrieveIncomingTransfersForRecipientWallet() {
        authenticateAs(recipientAppUserId);

        WalletTransaction transfer = walletTransactionWriteService.transfer(
                recipientWalletId,
                walletId,
                new BigDecimal("10000.00")
        );

        authenticateAs(appUserId);

        Page<WalletTransaction> transactions =
                walletTransactionReadService.retrieveWalletTransactions(
                        walletId, PageRequest.of(0, 10)
                );

        assertEquals(1, transactions.getTotalElements());
        assertEquals(transfer.getId(), transactions.getContent().get(0).getId());
        assertEquals(WalletTransactionType.TRANSFER,
                transactions.getContent().get(0).getTransactionType());
        assertEquals(walletId,
                transactions.getContent().get(0).getToWallet().getId());
    }

    @Test
    void shouldRetrieveTransactionBelongingToWallet() {
        WalletTransaction createdTransaction =
                walletTransactionWriteService.deposit(
                        walletId, new BigDecimal("15000.00")
                );

        WalletTransaction retrievedTransaction =
                walletTransactionReadService.retrieveWalletTransaction(
                        walletId, createdTransaction.getId()
                );

        assertEquals(createdTransaction.getId(), retrievedTransaction.getId());
        assertEquals(WalletTransactionType.DEPOSIT,
                retrievedTransaction.getTransactionType());
    }

    @Test
    void shouldRejectTransactionNotBelongingToWallet() {
        authenticateAs(recipientAppUserId);

        WalletTransaction recipientTransaction =
                walletTransactionWriteService.deposit(
                        recipientWalletId, new BigDecimal("5000.00")
                );

        authenticateAs(appUserId);

        assertThrows(
                WalletTransactionNotFoundException.class,
                () -> walletTransactionReadService.retrieveWalletTransaction(
                        walletId, recipientTransaction.getId()
                )
        );
    }

    @Test
    void shouldRejectAccessToWalletNotOwnedByCurrentUser() {
        assertThrows(
                WalletNotFoundException.class,
                () -> walletTransactionReadService.retrieveWalletTransactions(
                        recipientWalletId, PageRequest.of(0, 10)
                )
        );
    }

    @Test
    void shouldRetrieveTransactionsWithPagination() {
        walletTransactionWriteService.deposit(walletId, new BigDecimal("10000.00"));
        walletTransactionWriteService.deposit(walletId, new BigDecimal("20000.00"));
        walletTransactionWriteService.deposit(walletId, new BigDecimal("30000.00"));

        Page<WalletTransaction> firstPage =
                walletTransactionReadService.retrieveWalletTransactions(
                        walletId, PageRequest.of(0, 2)
                );

        Page<WalletTransaction> secondPage =
                walletTransactionReadService.retrieveWalletTransactions(
                        walletId, PageRequest.of(1, 2)
                );

        assertEquals(3, firstPage.getTotalElements());
        assertEquals(2, firstPage.getContent().size());
        assertEquals(2, firstPage.getTotalPages());
        assertTrue(firstPage.hasNext());

        assertEquals(1, secondPage.getContent().size());
        assertTrue(secondPage.isLast());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();

        if (transactionTemplate == null) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            if (walletId != null) {
                walletTransactionRepository
                        .deleteAllByFromWallet_IdOrToWallet_Id(walletId, walletId);
            }

            if (recipientWalletId != null) {
                walletTransactionRepository
                        .deleteAllByFromWallet_IdOrToWallet_Id(
                                recipientWalletId, recipientWalletId
                        );
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

    private AppUser createTestUser(String prefix, String uniqueId) {
        AppUser appUser = new AppUser();
        appUser.setUsername(prefix + uniqueId);
        appUser.setFirstName("Wallet Read");
        appUser.setLastName("Test");
        appUser.setEmail(prefix + uniqueId + "@example.com");
        appUser.setPassword("test-password");
        return appUser;
    }

    private void authenticateAs(Long userId) {
        AppUser appUser = appUserRepository.findById(userId).orElseThrow();

        UserPrincipal userPrincipal = new UserPrincipal(appUser);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void fundWallet(Long targetWalletId, BigDecimal amount) {
        transactionTemplate.executeWithoutResult(status -> {
            Wallet wallet = walletRepository.findById(targetWalletId).orElseThrow();
            wallet.deposit(amount);
        });
    }
}
