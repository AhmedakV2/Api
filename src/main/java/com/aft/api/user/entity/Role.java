package com.aft.api.user.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true, length = 32)
    private RoleCode code;
    @Column(name = "description",nullable = false)
    private String description;
    protected Role() {}
    public UUID getId() {
        return id;
    }
    public RoleCode getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
}
