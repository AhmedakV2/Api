package com.aft.api.tenant.repository;

import com.aft.api.tenant.entity.Membership;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    Optional<Membership> findByUserIdAndOrgId(UUID userId, UUID orgId);

    boolean existsByUserIdAndOrgId(UUID userId, UUID orgId);

    @Query("SELECT m.orgId FROM Membership m WHERE m.userId = :userId")
    Set<UUID> findOrgIdsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(m) FROM Membership m WHERE m.orgId = :orgId AND m.roleId = :roleId")
    long countByOrgIdAndRoleId(@Param("orgId") UUID orgId, @Param("roleId") UUID roleId);

    java.util.List<Membership> findByOrgId(UUID orgId);
}
