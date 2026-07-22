package com.bleep.learnhub.security;

import com.bleep.learnhub.entity.User;
import com.bleep.learnhub.entity.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security expects roles to be prefixed, or just the raw authority string
        // We return the exact Role enum name (e.g., "SUPER_ADMIN", "VENDOR")
        return Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Blocked users cannot log in
        return user.getStatus() != AccountStatus.BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Pending users shouldn't be fully active until OTP is verified
        return user.getStatus() == AccountStatus.ACTIVE;
    }

    // Expose the underlying ID if needed later
    public String getId() {
        return user.getId().toString();
    }
}