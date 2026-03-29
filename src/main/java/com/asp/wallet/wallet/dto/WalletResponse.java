package com.asp.wallet.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponse {

    private Long id;
    private Long appUserId;
    private String status;
    private BigDecimal balance;
    private String currency;
}
