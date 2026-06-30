package com.wanted.momocity.auth.infrastructure.security;

import com.wanted.momocity.auth.domain.model.Category;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Category category;

    public CustomUserDetails(Long userId, String password, Collection<? extends GrantedAuthority> authorities, Category category) {
        this.userId = userId;
        this.password = password;
        this.authorities = authorities;
        this.category = category;
    }

    public Long getUserId() { return userId; }
    public Category getCategory() { return category; }

    @Override public String getUsername() { return String.valueOf(userId); }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}