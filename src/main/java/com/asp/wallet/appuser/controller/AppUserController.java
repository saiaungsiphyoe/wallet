package com.asp.wallet.appuser.controller;

import com.asp.wallet.appuser.dto.AppUserResponse;
import com.asp.wallet.appuser.mapper.AppUserMapper;
import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.common.util.SecurityUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/appusers")
public class AppUserController {

    private final AppUserMapper appUserMapper;

    public AppUserController(AppUserMapper appUserMapper) {
        this.appUserMapper = appUserMapper;
    }

    @GetMapping("/{appUserId}")
    public ResponseEntity<AppUserResponse> getUserById(@PathVariable Long appUserId) {
        AppUser currentUser = SecurityUtil.getCurrentUser();

        if (!currentUser.getId().equals(appUserId)) {
            throw new AccessDeniedException("You are not allowed to access this user.");
        }

        return ResponseEntity.ok(appUserMapper.toResponse(currentUser));
    }
}
