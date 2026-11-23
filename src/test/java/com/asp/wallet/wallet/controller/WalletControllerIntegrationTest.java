package com.asp.wallet.wallet.controller;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.appuser.repository.AppUserRepository;
import com.asp.wallet.auth.dto.request.LoginRequest;
import com.asp.wallet.auth.dto.response.AuthResponse;
import com.asp.wallet.auth.service.AuthService;
import com.asp.wallet.common.security.UserPrincipal;
import com.asp.wallet.wallet.model.Currency;
import com.asp.wallet.wallet.model.Wallet;
import com.asp.wallet.wallet.repository.WalletRepository;
import com.asp.wallet.wallet.service.WalletWriteService;
import com.asp.wallet.wallet.transactions.repository.WalletTransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletWriteService walletWriteService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    private TransactionTemplate transactionTemplate;

    private AppUser appUser;
    private Wallet wallet;
    private AppUser anotherAppUser;
    private Wallet anotherWallet;
    private String jwtToken;



    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        String username = "wallet-api-" + UUID.randomUUID();
        String rawPassword = "TestPassword123!";

        transactionTemplate.executeWithoutResult(status -> {
            appUser = new AppUser();
            appUser.setUsername(username);
            appUser.setFirstName("Test");
            appUser.setLastName("User");
            appUser.setEmail(username + "@example.com");
            appUser.setPassword(passwordEncoder.encode(rawPassword));

            appUser = appUserRepository.saveAndFlush(appUser);

            wallet = walletRepository.saveAndFlush(
                    new Wallet(appUser, Currency.MMK)
            );
        });

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new UserPrincipal(appUser),
                        null,
                        List.of()
                )
        );

        try {
            wallet = walletWriteService.activateWallet(wallet.getId());
        } finally {
            SecurityContextHolder.clearContext();
        }

        AuthResponse authResponse = authService.verify(
                new LoginRequest(username, rawPassword)
        );

        jwtToken = authResponse.getAccessToken();
    }

    @Test
    void shouldReturnCurrentUserWalletsWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/wallets/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(wallet.getId()))
                .andExpect(jsonPath("$[0].appUserId").value(appUser.getId()))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].balance").value(0))
                .andExpect(jsonPath("$[0].currency").value("MMK"));
    }

    @Test
    void shouldReturnWalletWhenAuthenticatedUserOwnsIt() throws Exception {
        mockMvc.perform(get("/api/wallets/{walletId}", wallet.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(wallet.getId()))
                .andExpect(jsonPath("$.appUserId").value(appUser.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("MMK"));
    }

    @Test
    void shouldNotAllowUserToRetrieveAnotherUsersWallet() throws Exception {
        createAnotherUsersWallet();

        mockMvc.perform(get("/api/wallets/{walletId}", anotherWallet.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSuspendActiveWallet() throws Exception {
        mockMvc.perform(post("/api/wallets/{walletId}", wallet.getId())
                        .param("command", "suspend")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(wallet.getId()))
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void shouldReactivateSuspendedWallet() throws Exception {
        mockMvc.perform(post("/api/wallets/{walletId}", wallet.getId())
                        .param("command", "suspend")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        mockMvc.perform(post("/api/wallets/{walletId}", wallet.getId())
                        .param("command", "activate")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(wallet.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            if (wallet != null) {
                walletTransactionRepository.deleteAllByFromWallet_IdOrToWallet_Id(
                        wallet.getId(), wallet.getId()
                );
            }

            if (anotherWallet != null) {
                walletTransactionRepository.deleteAllByFromWallet_IdOrToWallet_Id(
                        anotherWallet.getId(), anotherWallet.getId()
                );
                walletRepository.deleteById(anotherWallet.getId());
            }

            if (anotherAppUser != null) {
                appUserRepository.deleteById(anotherAppUser.getId());
            }

            if (wallet != null) {
                walletRepository.deleteById(wallet.getId());
            }

            if (appUser != null) {
                appUserRepository.deleteById(appUser.getId());
            }
        });
    }

    private void createAnotherUsersWallet() {
        transactionTemplate.executeWithoutResult(status -> {
            String username = "wallet-api-" + UUID.randomUUID();

            anotherAppUser = new AppUser();
            anotherAppUser.setUsername(username);
            anotherAppUser.setFirstName("Another");
            anotherAppUser.setLastName("User");
            anotherAppUser.setEmail(username + "@example.com");
            anotherAppUser.setPassword(passwordEncoder.encode("TestPassword123!"));

            anotherAppUser = appUserRepository.saveAndFlush(anotherAppUser);

            anotherWallet = walletRepository.saveAndFlush(
                    new Wallet(anotherAppUser, Currency.MMK)
            );
        });
    }
}
