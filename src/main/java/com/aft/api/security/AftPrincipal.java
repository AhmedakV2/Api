package com.aft.api.security;

import com.aft.api.user.entity.UserAccount;
import com.aft.api.user.entity.UserStatus;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AftPrincipal(UUID userId,
                           String email,
                           String passwordHash,
                           UserStatus status,
                           Set<UUID> orgIds,
                           List<GrantedAuthority> authorities) implements UserDetails {
    public static AftPrincipal from(UserAccount user, Set<UUID> orgIds) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.getCode().name()))
                .toList();
        return new AftPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(),
                user.getStatus(), orgIds, authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return userId.toString();
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
