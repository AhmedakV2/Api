package com.aft.api.user.entity;

import com.aft.api.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_account")
public class UserAccount extends AuditableEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, columnDefinition = "citext")
    private String username;

    @Column(name = "email", nullable = false, unique = true, columnDefinition = "citext")
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "locale", nullable = false, length = 8)
    private String locale = "tr";

    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    protected UserAccount() {
    }

    public UserAccount(String username, String email, String passwordHash, String displayName, String locale) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.locale = locale;
    }

    public void changePasswordHash(String newHash) {
        this.passwordHash = newHash;
    }

    public void rename(String newDisplayName) {
        this.displayName = newDisplayName;
    }

    public void changeLocale(String newLocale) {
        this.locale = newLocale;
    }

    public void changeStatus(UserStatus newStatus) {
        this.status = newStatus;
    }

    public void setMfaEnabled(boolean enabled) {
        this.mfaEnabled = enabled;
    }

    public void markLogin(Instant when) {
        this.lastLoginAt = when;
    }

    public void grant(Role role) {
        roles.add(role);
    }

    public boolean revoke(Role role) {
        if (roles.size() <= 1) {
            return false;
        }
        return roles.remove(role);
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public String getLocale() {
        return locale;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Set<Role> getRoles() {
        return Set.copyOf(roles);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof UserAccount account && id != null && id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
