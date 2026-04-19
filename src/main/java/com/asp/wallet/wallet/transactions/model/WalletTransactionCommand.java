package com.asp.wallet.wallet.transactions.model;

import com.asp.wallet.wallet.transactions.exception.InvalidWalletTransactionCommandException;

public enum WalletTransactionCommand {
    DEPOSIT,
    WITHDRAW,
    TRANSFER;

    public static WalletTransactionCommand fromString(String command) {
        try {
            return WalletTransactionCommand.valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidWalletTransactionCommandException(command, e);
        }
    }
}
