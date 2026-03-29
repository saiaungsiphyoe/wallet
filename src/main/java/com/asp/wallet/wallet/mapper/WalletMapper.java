package com.asp.wallet.wallet.mapper;

import com.asp.wallet.wallet.dto.WalletResponse;
import com.asp.wallet.wallet.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "appUserId", source = "appUser.id")
    @Mapping(target = "status", source = "status")
    WalletResponse toResponse(Wallet wallet);

    List<WalletResponse> toResponseLists(List<Wallet> wallet);
}
