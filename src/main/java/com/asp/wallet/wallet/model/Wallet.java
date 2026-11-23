package com.asp.wallet.wallet.model;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.wallet.exception.InvalidWalletCommandException;
import com.asp.wallet.wallet.exception.WalletOperationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appuser_id", nullable = false)
    private AppUser appUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_status", nullable = false, length = 20)
    private WalletStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Wallet(AppUser appUser, Currency currency) {
        this.appUser = Objects.requireNonNull(appUser, "AppUser cannot be null.");
        this.status = WalletStatus.PENDING;
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null.");
        this.balance = BigDecimal.ZERO;
    }

    public void activate() {
        if (this.status != WalletStatus.PENDING && this.status != WalletStatus.SUSPENDED) {
            throw new InvalidWalletCommandException(
                    "Only pending or suspended wallets can be activated."
            );
        }

        this.status = WalletStatus.ACTIVE;
    }

    public void suspend() {
        if (this.status != WalletStatus.ACTIVE) {
            throw new InvalidWalletCommandException(
                    "Only active wallets can be suspended."
            );
        }

        this.status = WalletStatus.SUSPENDED;
    }

    public void close() {
        if (this.status != WalletStatus.ACTIVE && this.status != WalletStatus.SUSPENDED) {
            throw new InvalidWalletCommandException(
                    "Only active or suspended wallets can be closed."
            );
        }

        if (this.balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new WalletOperationException(
                    "Wallet must have zero balance before it can be closed."
            );
        }

        this.status = WalletStatus.CLOSED;
    }

    public void reject() {
        if (this.status != WalletStatus.PENDING) {
            throw new InvalidWalletCommandException(
                    "Only pending wallets can be rejected."
            );
        }

        this.status = WalletStatus.REJECTED;
    }

    public void deposit(BigDecimal amount) {
        validateWalletStatus();
        validateTransactionAmount(amount);

        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        validateWalletStatus();
        validateTransactionAmount(amount);
        validateSufficientBalance(amount);

        this.balance = this.balance.subtract(amount);
    }

    private void validateWalletStatus() {
        if (this.status != WalletStatus.ACTIVE) {
            throw new WalletOperationException(
                    "Transactions are not allowed for wallets with status: " + this.status
            );
        }
    }

    private void validateTransactionAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletOperationException(
                    "Transaction amount must be greater than zero."
            );
        }
    }

    private void validateSufficientBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new WalletOperationException("Insufficient balance.");
        }
    }
}
