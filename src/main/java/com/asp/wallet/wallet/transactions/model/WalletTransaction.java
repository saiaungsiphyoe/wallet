package com.asp.wallet.wallet.transactions.model;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.wallet.model.Currency;
import com.asp.wallet.wallet.model.Wallet;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "wallet_transaction")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private AppUser initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_id")
    private Wallet fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id")
    private Wallet toWallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private WalletTransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Column(name = "is_reversed", nullable = false)
    private boolean isReversed;

    @Column(name = "reversed_transaction_id")
    private Long reversedTransactionId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private WalletTransaction(AppUser initiator, Wallet fromWallet, Wallet toWallet, LocalDate transactionDate, WalletTransactionType transactionType, BigDecimal amount, Currency currency) {
        this.initiator = Objects.requireNonNull(initiator, "Initiator cannot be null.");
        this.fromWallet = fromWallet;
        this.toWallet = toWallet;
        this.transactionDate = Objects.requireNonNull(transactionDate, "Transaction date cannot be null.");
        this.transactionType = Objects.requireNonNull(transactionType, "Transaction type cannot be null.");
        this.amount = Objects.requireNonNull(amount, "Transaction amount cannot be null.");
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null.");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero."
            );
        }
    }

    public static WalletTransaction depositTransaction(AppUser initiator, Wallet toWallet, LocalDate transactionDate, BigDecimal amount) {
        Objects.requireNonNull(toWallet, "Destination wallet cannot be null.");

        return new WalletTransaction(initiator, null, toWallet, transactionDate, WalletTransactionType.DEPOSIT, amount, toWallet.getCurrency());
    }

    public static WalletTransaction withdrawTransaction(AppUser initiator, Wallet fromWallet, LocalDate transactionDate, BigDecimal amount) {
        Objects.requireNonNull(fromWallet, "Source wallet cannot be null.");

        return new WalletTransaction(initiator, fromWallet, null, transactionDate, WalletTransactionType.WITHDRAW, amount, fromWallet.getCurrency());
    }

    public static WalletTransaction transferTransaction(AppUser initiator, Wallet fromWallet, Wallet toWallet, LocalDate transactionDate, BigDecimal amount) {
        Objects.requireNonNull(fromWallet, "Source wallet cannot be null.");
        Objects.requireNonNull(toWallet, "Destination wallet cannot be null.");

        if (fromWallet.getCurrency() != toWallet.getCurrency()) {
            throw new IllegalArgumentException(
                    "Source and destination wallets must have the same currency."
            );
        }

        return new WalletTransaction(initiator, fromWallet, toWallet, transactionDate, WalletTransactionType.TRANSFER, amount, fromWallet.getCurrency());
    }
}
