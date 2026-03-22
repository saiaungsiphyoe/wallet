package com.asp.wallet.appuser.controller;

import com.asp.wallet.appuser.dto.AppUserResponse;
import com.asp.wallet.appuser.service.AppUserReadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appusers")
public class AppUserController {

    private final AppUserReadService appUserReadService;

    public AppUserController(AppUserReadService appUserReadService) {
        this.appUserReadService = appUserReadService;
    }

    @GetMapping
    public ResponseEntity<Page<AppUserResponse>> getAllUsers(Pageable pageable) {

        return ResponseEntity.ok(appUserReadService.getAllUsers(pageable));
    }
}
