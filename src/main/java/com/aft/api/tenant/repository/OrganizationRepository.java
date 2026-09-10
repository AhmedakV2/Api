package com.aft.api.tenant.repository;

import com.aft.api.tenant.entity.Organization;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    boolean existsBySlug(String slug);

    @Query("""
            SELECT o FROM Organization o
            WHERE o.id IN (SELECT m.orgId FROM Membership m WHERE m.userId = :userId)
            ORDER BY o.name
            """)
    List<Organization> findByMember(@Param("userId") UUID userId);
}
