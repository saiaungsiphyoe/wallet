package com.asp.wallet.wallet.model;

import com.asp.wallet.appuser.model.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Wallet(AppUser appUser, Currency currency) {
        this.appUser = appUser;
        this.status = WalletStatus.PENDING;
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
    }

    public void activate() {
        this.status = WalletStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = WalletStatus.PENDING;
    }

    public void suspend() {
        this.status = WalletStatus.SUSPENDED;
    }

    public void close() {
         this.status = WalletStatus.CLOSED;
    }

    public void reject() {
         this.status = WalletStatus.REJECTED;
    }
}
