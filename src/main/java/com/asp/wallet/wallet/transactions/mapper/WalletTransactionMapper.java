package com.asp.wallet.wallet.transactions.mapper;

import com.asp.wallet.wallet.transactions.dto.WalletTransactionRequest;
import com.asp.wallet.wallet.transactions.dto.WalletTransactionResponse;
import com.asp.wallet.wallet.transactions.model.WalletTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletTransactionMapper {

    @Mapping(source = "fromWallet.id", target = "fromWalletId")
    @Mapping(source = "toWallet.id", target = "toWalletId")
    WalletTransactionResponse toResponse(WalletTransaction walletTransaction);

    WalletTransaction toEntity(WalletTransactionRequest walletTransactionRequest);
}
