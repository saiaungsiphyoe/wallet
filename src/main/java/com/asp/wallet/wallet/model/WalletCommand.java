package com.asp.wallet.wallet.model;

import com.asp.wallet.wallet.exception.InvalidWalletCommandException;

public enum WalletCommand {
    ACTIVATE,
    SUSPEND,
    CLOSE,
    REJECT;

    public static WalletCommand fromString(String command) {
        if (command == null || command.isBlank()) {
            throw new InvalidWalletCommandException("Wallet command cannot be blank.");
        }

        try {
            return WalletCommand.valueOf(command.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidWalletCommandException("The command " + command + " is Invalid.");
        }
    }
}
