
package com.asp.wallet.wallet;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.appuser.repository.AppUserRepository;
import com.asp.wallet.wallet.model.Currency;
import com.asp.wallet.wallet.model.Wallet;
import com.asp.wallet.wallet.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WalletOptimisticLockingIntegrationTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private Long walletId;
    private Long appUserId;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        String uniqueId = UUID.randomUUID().toString();

        AppUser appUser = new AppUser();
        appUser.setUsername("locking-test-" + uniqueId);
        appUser.setFirstName("Locking");
        appUser.setLastName("Test");
        appUser.setEmail("locking-test-" + uniqueId + "@example.com");
        appUser.setPassword("test-password");

        appUserId = appUserRepository.saveAndFlush(appUser).getId();

        Wallet wallet = new Wallet(appUser, Currency.MMK);
        wallet.activate();
        wallet.deposit(new BigDecimal("100000.00"));

        walletId = walletRepository.saveAndFlush(wallet).getId();
    }

    @Test
    void shouldRejectUpdateWhenWalletVersionIsStale() {

        Wallet staleWallet = transactionTemplate.execute(status ->
                walletRepository.findById(walletId).orElseThrow()
        );

        assertNotNull(staleWallet);
        Long originalVersion = staleWallet.getVersion();

        transactionTemplate.executeWithoutResult(status -> {
            Wallet currentWallet = walletRepository.findById(walletId).orElseThrow();
            currentWallet.withdraw(new BigDecimal("20000.00"));
            walletRepository.flush();
        });

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> transactionTemplate.executeWithoutResult(status -> {
                    staleWallet.withdraw(new BigDecimal("10000.00"));
                    walletRepository.saveAndFlush(staleWallet);
                })
        );

        Wallet finalWallet = walletRepository.findById(walletId).orElseThrow();

        assertEquals(
                0,
                finalWallet.getBalance().compareTo(new BigDecimal("80000.00"))
        );

        assertEquals(originalVersion + 1, finalWallet.getVersion());
    }

    @AfterEach
    void tearDown() {
        if (walletId == null && appUserId == null) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            if (walletId != null) {
                walletRepository.deleteById(walletId);
            }

            if (appUserId != null) {
                appUserRepository.deleteById(appUserId);
            }
        });
    }
}
