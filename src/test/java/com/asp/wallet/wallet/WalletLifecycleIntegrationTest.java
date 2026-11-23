
package com.asp.wallet.wallet;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.appuser.repository.AppUserRepository;
import com.asp.wallet.common.security.UserPrincipal;
import com.asp.wallet.wallet.exception.InvalidWalletCommandException;
import com.asp.wallet.wallet.exception.WalletOperationException;
import com.asp.wallet.wallet.model.Currency;
import com.asp.wallet.wallet.model.Wallet;
import com.asp.wallet.wallet.model.WalletStatus;
import com.asp.wallet.wallet.repository.WalletRepository;
import com.asp.wallet.wallet.service.WalletWriteService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WalletLifecycleIntegrationTest {

    @Autowired
    private WalletWriteService walletWriteService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    private Long appUserId;
    private Long walletId;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        String uniqueId = UUID.randomUUID().toString();
        String testUserPrefix = "wallet-lifecycle-test-";

        AppUser appUser = new AppUser();
        appUser.setUsername(testUserPrefix + uniqueId);
        appUser.setFirstName("Wallet Lifecycle");
        appUser.setLastName("Test");
        appUser.setEmail(testUserPrefix + uniqueId + "@example.com");
        appUser.setPassword("test-password");

        appUserId = appUserRepository.saveAndFlush(appUser).getId();

        Wallet wallet = new Wallet(appUser, Currency.MMK);
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
    void shouldActivatePendingWallet() {
        Wallet activatedWallet = walletWriteService.activateWallet(walletId);

        assertEquals(WalletStatus.ACTIVE, activatedWallet.getStatus());
        assertEquals(WalletStatus.ACTIVE, findWallet().getStatus());
    }

    @Test
    void shouldSuspendActiveWallet() {
        walletWriteService.activateWallet(walletId);

        Wallet suspendedWallet = walletWriteService.suspendWallet(walletId);

        assertEquals(WalletStatus.SUSPENDED, suspendedWallet.getStatus());
        assertEquals(WalletStatus.SUSPENDED, findWallet().getStatus());
    }

    @Test
    void shouldReactivateSuspendedWallet() {
        walletWriteService.activateWallet(walletId);
        walletWriteService.suspendWallet(walletId);

        Wallet activatedWallet = walletWriteService.activateWallet(walletId);

        assertEquals(WalletStatus.ACTIVE, activatedWallet.getStatus());
        assertEquals(WalletStatus.ACTIVE, findWallet().getStatus());
    }

    @Test
    void shouldCloseActiveWalletWithZeroBalance() {
        walletWriteService.activateWallet(walletId);

        Wallet closedWallet = walletWriteService.closeWallet(walletId);

        assertEquals(WalletStatus.CLOSED, closedWallet.getStatus());
        assertEquals(WalletStatus.CLOSED, findWallet().getStatus());
        assertEquals(0, closedWallet.getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void shouldCloseSuspendedWalletWithZeroBalance() {
        walletWriteService.activateWallet(walletId);
        walletWriteService.suspendWallet(walletId);

        Wallet closedWallet = walletWriteService.closeWallet(walletId);

        assertEquals(WalletStatus.CLOSED, closedWallet.getStatus());
        assertEquals(WalletStatus.CLOSED, findWallet().getStatus());
    }

    @Test
    void shouldRejectPendingWallet() {
        Wallet rejectedWallet = walletWriteService.rejectWallet(walletId);

        assertEquals(WalletStatus.REJECTED, rejectedWallet.getStatus());
        assertEquals(WalletStatus.REJECTED, findWallet().getStatus());
    }

    @Test
    void shouldRejectClosingWalletWithNonZeroBalance() {
        walletWriteService.activateWallet(walletId);

        BigDecimal initialBalance = new BigDecimal("10000.00");

        transactionTemplate.executeWithoutResult(status -> {
            Wallet wallet = walletRepository.findById(walletId).orElseThrow();
            wallet.deposit(initialBalance);
        });

        assertThrows(
                WalletOperationException.class,
                () -> walletWriteService.closeWallet(walletId)
        );

        Wallet unchangedWallet = findWallet();

        assertEquals(WalletStatus.ACTIVE, unchangedWallet.getStatus());
        assertEquals(0, unchangedWallet.getBalance().compareTo(initialBalance));
    }

    @Test
    void shouldRejectActivatingClosedWallet() {
        walletWriteService.activateWallet(walletId);
        walletWriteService.closeWallet(walletId);

        assertThrows(
                InvalidWalletCommandException.class,
                () -> walletWriteService.activateWallet(walletId)
        );

        assertEquals(WalletStatus.CLOSED, findWallet().getStatus());
    }

    @Test
    void shouldRejectSuspendingPendingWallet() {
        assertThrows(
                InvalidWalletCommandException.class,
                () -> walletWriteService.suspendWallet(walletId)
        );

        assertEquals(WalletStatus.PENDING, findWallet().getStatus());
    }

    @Test
    void shouldRejectRejectingActiveWallet() {
        walletWriteService.activateWallet(walletId);

        assertThrows(
                InvalidWalletCommandException.class,
                () -> walletWriteService.rejectWallet(walletId)
        );

        assertEquals(WalletStatus.ACTIVE, findWallet().getStatus());
    }

    @Test
    void shouldRejectRejectingClosedWallet() {
        walletWriteService.activateWallet(walletId);
        walletWriteService.closeWallet(walletId);

        assertThrows(
                InvalidWalletCommandException.class,
                () -> walletWriteService.rejectWallet(walletId)
        );

        assertEquals(WalletStatus.CLOSED, findWallet().getStatus());
    }

    @Test
    void shouldRejectClosingRejectedWallet() {
        walletWriteService.rejectWallet(walletId);

        assertThrows(
                InvalidWalletCommandException.class,
                () -> walletWriteService.closeWallet(walletId)
        );

        assertEquals(WalletStatus.REJECTED, findWallet().getStatus());
    }

    private Wallet findWallet() {
        return walletRepository.findById(walletId).orElseThrow();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();

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
