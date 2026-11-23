package com.asp.wallet.wallet.transactions.controller;

import com.asp.wallet.wallet.transactions.dto.WalletTransactionRequest;
import com.asp.wallet.wallet.transactions.dto.WalletTransactionResponse;
import com.asp.wallet.wallet.transactions.mapper.WalletTransactionMapper;
import com.asp.wallet.wallet.transactions.model.WalletTransaction;
import com.asp.wallet.wallet.transactions.model.WalletTransactionCommand;
import com.asp.wallet.wallet.transactions.service.WalletTransactionReadService;
import com.asp.wallet.wallet.transactions.service.WalletTransactionWriteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api")
public class WalletTransactionController {

    private final WalletTransactionWriteService walletTransactionWriteService;
    private final WalletTransactionReadService walletTransactionReadService;
    private final WalletTransactionMapper walletTransactionMapper;

    public WalletTransactionController(WalletTransactionWriteService walletTransactionWriteService,
                                       WalletTransactionReadService walletTransactionReadService,
                                       WalletTransactionMapper walletTransactionMapper) {
        this.walletTransactionWriteService = walletTransactionWriteService;
        this.walletTransactionReadService = walletTransactionReadService;
        this.walletTransactionMapper = walletTransactionMapper;
    }

    @PostMapping("/wallet-transactions")
    public ResponseEntity<WalletTransactionResponse> createTransaction(@RequestBody WalletTransactionRequest request,
                                                                       @RequestParam String command) {
        WalletTransactionCommand transactionCommand = WalletTransactionCommand.fromString(command);
        WalletTransaction walletTransaction = handleCommand(request, transactionCommand);
        return ResponseEntity.ok(walletTransactionMapper.toResponse(walletTransaction));
    }

    @GetMapping("/wallets/{walletId}/transactions")
    public ResponseEntity<Page<WalletTransactionResponse>> retrieveWalletTransactions(
            @PathVariable Long walletId,
            @PageableDefault(size = 10, sort = "transactionDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<WalletTransactionResponse> transactions =
                walletTransactionReadService.retrieveWalletTransactions(walletId, pageable)
                        .map(walletTransactionMapper::toResponse);

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/wallets/{walletId}/transactions/{transactionId}")
    public ResponseEntity<WalletTransactionResponse> retrieveWalletTransaction(
            @PathVariable Long walletId,
            @PathVariable Long transactionId) {

        WalletTransaction transaction =
                walletTransactionReadService.retrieveWalletTransaction(walletId, transactionId);

        return ResponseEntity.ok(walletTransactionMapper.toResponse(transaction));
    }

    private WalletTransaction handleCommand(WalletTransactionRequest request, WalletTransactionCommand command) {
        return switch (command) {
            case DEPOSIT -> walletTransactionWriteService.deposit(request.getToWalletId(), request.getAmount());
            case WITHDRAW -> walletTransactionWriteService.withdraw(request.getFromWalletId(), request.getAmount());
            case TRANSFER -> walletTransactionWriteService.transfer(request.getFromWalletId(), request.getToWalletId(), request.getAmount());
        };
    }
}
