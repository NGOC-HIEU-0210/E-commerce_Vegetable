package com.hieu.config;

import com.hieu.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class CurrentUser {

    private static final String ADMIN = "ADMIN";

    private final UserService userService;

    public Long id(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }
        return userService.findByUsername(authentication.getName()).getId();
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> ADMIN.equals(a.getAuthority()));
    }

    public void requireOwnerOrAdmin(Authentication authentication, Long ownerId) {
        if (isAdmin(authentication)) {
            return;
        }
        if (ownerId == null || !ownerId.equals(id(authentication))) {
            throw new AccessDeniedException("Access denied");
        }
    }
}