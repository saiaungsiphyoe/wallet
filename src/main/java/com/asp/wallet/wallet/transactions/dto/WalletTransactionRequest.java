package com.asp.wallet.wallet.transactions.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionRequest {

    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;
}
