package com.asp.wallet.wallet.transactions.controller;

import com.asp.wallet.wallet.transactions.dto.WalletTransactionRequest;
import com.asp.wallet.wallet.transactions.dto.WalletTransactionResponse;
import com.asp.wallet.wallet.transactions.mapper.WalletTransactionMapper;
import com.asp.wallet.wallet.transactions.model.WalletTransaction;
import com.asp.wallet.wallet.transactions.model.WalletTransactionCommand;
import com.asp.wallet.wallet.transactions.service.WalletTransactionWriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet-transactions")
public class WalletTransactionController {

    private final WalletTransactionWriteService walletTransactionWriteService;
    private final WalletTransactionMapper walletTransactionMapper;

    public WalletTransactionController(WalletTransactionWriteService walletTransactionWriteService,
                                       WalletTransactionMapper walletTransactionMapper) {
        this.walletTransactionWriteService = walletTransactionWriteService;
        this.walletTransactionMapper = walletTransactionMapper;
    }

    @PostMapping
    public ResponseEntity<WalletTransactionResponse> createTransaction(@RequestBody WalletTransactionRequest request,
                                                                       @RequestParam String command) {
        WalletTransactionCommand transactionCommand = WalletTransactionCommand.fromString(command);
        WalletTransaction walletTransaction = handleCommand(request, transactionCommand);
        return ResponseEntity.ok(walletTransactionMapper.toResponse(walletTransaction));
    }

    private WalletTransaction handleCommand(WalletTransactionRequest request, WalletTransactionCommand command) {
        return switch (command) {
            case DEPOSIT -> walletTransactionWriteService.deposit(request.getToWalletId(), request.getAmount());
            case WITHDRAW -> walletTransactionWriteService.withdraw(request.getFromWalletId(), request.getAmount());
            case TRANSFER -> walletTransactionWriteService.transfer(request.getFromWalletId(), request.getToWalletId(), request.getAmount());
        };
    }
}
