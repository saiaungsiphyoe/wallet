package com.asp.wallet.wallet.controller;

import com.asp.wallet.wallet.dto.WalletResponse;
import com.asp.wallet.wallet.mapper.WalletMapper;
import com.asp.wallet.wallet.model.Wallet;
import com.asp.wallet.wallet.model.WalletCommand;
import com.asp.wallet.wallet.service.WalletReadService;
import com.asp.wallet.wallet.service.WalletWriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletReadService walletReadService;
    private final WalletWriteService walletWriteService;
    private final WalletMapper walletMapper;

    public WalletController(WalletReadService walletReadService,
                            WalletWriteService walletWriteService,
                            WalletMapper walletMapper) {
        this.walletReadService = walletReadService;
        this.walletWriteService = walletWriteService;
        this.walletMapper = walletMapper;
    }

    @GetMapping("/me")
    public ResponseEntity<List<WalletResponse>> getWallet() {
        List<Wallet> wallets = walletReadService.retrieveCurrentUserWallets();
        return ResponseEntity.ok(walletMapper.toResponseLists(wallets));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long walletId) {
        Wallet wallet = walletReadService.retrieveWallet(walletId);
        return ResponseEntity.ok(walletMapper.toResponse(wallet));
    }

    @PostMapping("/{walletId}")
    public ResponseEntity<WalletResponse> processCommand(@PathVariable Long walletId,
                                                         @RequestParam String command) {
        WalletCommand walletCommand = WalletCommand.fromString(command);
        Wallet wallet = handleCommand(walletId, walletCommand);
        return ResponseEntity.ok(walletMapper.toResponse(wallet));
    }

    private Wallet handleCommand(Long walletId, WalletCommand command) {
        return switch (command) {
            case ACTIVATE -> walletWriteService.activateWallet(walletId);
            case DEACTIVATE -> walletWriteService.deactivateWallet(walletId);
            case SUSPEND -> walletWriteService.suspendWallet(walletId);
            case CLOSE -> walletWriteService.closeWallet(walletId);
            case REJECT -> walletWriteService.rejectWallet(walletId);
        };
    }
}

