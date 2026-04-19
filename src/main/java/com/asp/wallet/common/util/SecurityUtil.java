package com.asp.wallet.common.util;

import com.asp.wallet.appuser.model.AppUser;
import com.asp.wallet.common.security.UserPrincipal;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor
public class SecurityUtil {

    public static AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return userPrincipal.getAppUser();
    }
}
