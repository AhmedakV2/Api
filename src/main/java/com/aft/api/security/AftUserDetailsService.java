package com.aft.api.security;

import com.aft.api.tenant.repository.MembershipRepository;
import com.aft.api.user.entity.UserAccount;
import com.aft.api.user.repository.UserRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AftUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public AftUserDetailsService(UserRepository userRepository, MembershipRepository membershipRepository) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AftPrincipal loadUserByUsername(String username) {
        UserAccount user = userRepository.findWithRolesByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanici bulunamadi"));
        return toPrincipal(user);
    }

    @Transactional(readOnly = true)
    public AftPrincipal loadById(UUID userId) {
        UserAccount user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanici bulunamadi"));
        return toPrincipal(user);
    }

    private AftPrincipal toPrincipal(UserAccount user) {
        Set<UUID> orgIds = membershipRepository.findOrgIdsByUserId(user.getId());
        return AftPrincipal.from(user, orgIds);
    }
}
