package com.asp.wallet.appuser.mapper;

import com.asp.wallet.appuser.dto.AppUserResponse;
import com.asp.wallet.auth.dto.request.RegisterRequest;
import com.asp.wallet.appuser.model.AppUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppUserMapper {

    AppUserResponse toResponse(AppUser appUser);

    AppUser toEntity(RegisterRequest appUserResponse);
}
