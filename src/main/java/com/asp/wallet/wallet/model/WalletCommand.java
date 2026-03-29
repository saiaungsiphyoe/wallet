package com.asp.wallet.wallet.model;

import com.asp.wallet.wallet.exception.InvalidWalletCommandException;

public enum WalletCommand {
    ACTIVATE,
    DEACTIVATE,
    SUSPEND,
    CLOSE,
    REJECT;

    public static WalletCommand fromString(String command) {
        try {
            return WalletCommand.valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidWalletCommandException("The command " + command + " is Invalid.");
        }
    }
}
