package com.asp.wallet.wallet.transactions.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionResponse {

    private Long id;
    private Long fromWalletId;
    private Long toWalletId;
    private String transactionType;
    private LocalDate transactionDate;
    private String currency;
    private BigDecimal amount;
}
