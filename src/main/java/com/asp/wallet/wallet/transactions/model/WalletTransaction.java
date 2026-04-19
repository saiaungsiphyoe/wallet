package com.asp.wallet.wallet.transactions.model;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.wallet.model.Currency;
import com.asp.wallet.wallet.model.Wallet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallet_transaction")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id")
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

    @Column(name = "is_reversed")
    private boolean isReversed;

    @Column(name = "reversed_transaction_id")
    private Long reversedTransactionId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    protected WalletTransaction(AppUser initiator, Wallet fromWallet, Wallet toWallet, LocalDate transactionDate, WalletTransactionType transactionType, BigDecimal amount, Currency currency) {
        this.initiator = initiator;
        this.fromWallet = fromWallet;
        this.toWallet = toWallet;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
    }

    public static WalletTransaction depositTransaction(AppUser initiator, Wallet toWallet, LocalDate transactionDate, BigDecimal amount) {
        return new WalletTransaction(initiator, null, toWallet, transactionDate, WalletTransactionType.DEPOSIT, amount, toWallet.getCurrency());
    }

    public static WalletTransaction withdrawTransaction(AppUser initiator, Wallet fromWallet, LocalDate transactionDate, BigDecimal amount) {
        return new WalletTransaction(initiator, fromWallet, null, transactionDate, WalletTransactionType.WITHDRAW, amount, fromWallet.getCurrency());
    }

    public static WalletTransaction transferTransaction(AppUser initiator, Wallet fromWallet, Wallet toWallet, LocalDate transactionDate, BigDecimal amount) {
        return new WalletTransaction(initiator, fromWallet, toWallet, transactionDate, WalletTransactionType.TRANSFER, amount, fromWallet.getCurrency());
    }
}
