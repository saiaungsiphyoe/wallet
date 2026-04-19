package com.asp.wallet.wallet.mapper;

import com.asp.wallet.wallet.dto.WalletResponse;
import com.asp.wallet.wallet.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(source = "appUser.id", target = "appUserId")
    @Mapping(source = "status", target = "status")
    WalletResponse toResponse(Wallet wallet);

    List<WalletResponse> toResponseLists(List<Wallet> wallet);
}
