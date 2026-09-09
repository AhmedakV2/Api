package com.aft.api.user.repository;

import com.aft.api.user.entity.Role;
import com.aft.api.user.entity.RoleCode;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(RoleCode code);
}
