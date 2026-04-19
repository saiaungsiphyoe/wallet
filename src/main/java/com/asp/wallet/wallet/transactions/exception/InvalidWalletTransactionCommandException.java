package com.asp.wallet.wallet.transactions.exception;

public class InvalidWalletTransactionCommandException extends RuntimeException {

    public InvalidWalletTransactionCommandException(String message) {
        super(message);
    }

    public InvalidWalletTransactionCommandException(String command, Throwable cause) {
        super("The command " + command + " is Invalid.", cause);
    }
}
