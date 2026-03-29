package com.asp.wallet.appuser.controller;

import com.asp.wallet.appuser.dto.AppUserResponse;
import com.asp.wallet.appuser.mapper.AppUserMapper;
import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.appuser.service.AppUserReadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appusers")
public class AppUserController {

    private final AppUserReadService appUserReadService;
    private final AppUserMapper appUserMapper;

    public AppUserController(AppUserReadService appUserReadService,
                             AppUserMapper appUserMapper) {
        this.appUserReadService = appUserReadService;
        this.appUserMapper = appUserMapper;
    }

    @GetMapping
    public ResponseEntity<Page<AppUserResponse>> getAllUsers(Pageable pageable) {
        Page<AppUserResponse> users = appUserReadService.getAllUsers(pageable)
                .map(appUserMapper::toResponse);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{appUserId}")
    public ResponseEntity<AppUserResponse> getUserById(@PathVariable Long appUserId) {
        AppUser appUser = appUserReadService.getUserById(appUserId);

        return ResponseEntity.ok(appUserMapper.toResponse(appUser));
    }
}
