package com.skrt.wigellpadelservice.utility;

import com.skrt.wigellpadelservice.exceptions.AuthenticationRequiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static Authentication authOrThrow(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || auth.getName() == null || auth.getName().isBlank()){
            throw new AuthenticationRequiredException("resolve current customer", "no principal");
        }
        return auth;
    }

    public static boolean isAdmin(){
        return authOrThrow().getAuthorities()
                .stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public static <T> void assertOwnerOrAdmin(T ownerId, T currentId, RuntimeException onFail){
        if(isAdmin())return;
        if(ownerId == null || !ownerId.equals(currentId)) throw onFail;
    }

    public static String currentUsername(){
        return authOrThrow().getName().trim();
    }

}
