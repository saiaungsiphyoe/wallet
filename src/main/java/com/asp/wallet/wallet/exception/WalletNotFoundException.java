package com.asp.wallet.wallet.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(Long id) {
        super("Wallet with id: " + id + " not found");
    }

    public WalletNotFoundException(String message) {
        super(message);
    }
}
