package com.aft.api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aft.api.common.exception.ConflictException;
import com.aft.api.common.exception.NotFoundException;
import com.aft.api.common.exception.ValidationException;
import com.aft.api.user.dto.CreateUserRequest;
import com.aft.api.user.dto.UpdateUserRequest;
import com.aft.api.user.dto.UserDto;
import com.aft.api.user.entity.RoleCode;
import com.aft.api.user.entity.UserAccount;
import com.aft.api.user.entity.UserStatus;
import com.aft.api.user.mapper.UserMapper;
import com.aft.api.user.repository.RoleRepository;
import com.aft.api.user.repository.UserRepository;
import com.aft.api.user.service.PasswordPolicyService;
import com.aft.api.user.service.UserService;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordPolicyService passwordPolicy;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordPolicy, passwordEncoder, userMapper);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$ozet");
        when(userMapper.toDto(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount user = invocation.getArgument(0);
            return new UserDto(user.getId(), user.getEmail(), user.getDisplayName(),
                    user.getStatus().name(), user.getLocale(), user.isMfaEnabled(), Set.of(), null, null);
        });
    }

    @Test
    void tekrarliEpostaCakismaHatasiVerir() {
        when(userRepository.existsByEmailIgnoreCase("ahmet@aft.local")).thenReturn(true);
        CreateUserRequest request = new CreateUserRequest("ahmet@aft.local", "Kalkan-2026-Gizli!",
                "Ahmet", "tr", Set.of(RoleCode.USER));

        assertThatThrownBy(() -> userService.create(request)).isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void bilinmeyenKullaniciBulunamadiHatasiVerir() {
        UUID id = UUID.randomUUID();
        when(userRepository.findWithRolesById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.get(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void guncellemeYalnizcaGonderilenAlanlariUygular() {
        UserAccount user = new UserAccount("ahmet@aft.local", "$2a$12$ozet", "Ahmet", "tr");
        when(userRepository.findWithRolesById(any())).thenReturn(Optional.of(user));

        userService.update(UUID.randomUUID(), new UpdateUserRequest("Ahmet Akin", null, null, null));

        assertThat(user.getDisplayName()).isEqualTo("Ahmet Akin");
        assertThat(user.getLocale()).isEqualTo("tr");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void pasiflestirmeKaydiSilmezDurumuDegistirir() {
        UserAccount user = new UserAccount("ahmet@aft.local", "$2a$12$ozet", "Ahmet", "tr");
        when(userRepository.findWithRolesById(any())).thenReturn(Optional.of(user));

        userService.disable(UUID.randomUUID());

        assertThat(user.getStatus()).isEqualTo(UserStatus.DISABLED);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void hataliMevcutParolaDegisikligiEngeller() {
        UserAccount user = new UserAccount("ahmet@aft.local", "$2a$12$ozet", "Ahmet", "tr");
        when(userRepository.findWithRolesById(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("yanlis", "$2a$12$ozet")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(UUID.randomUUID(), "yanlis", "Kalkan-2026-Gizli!"))
                .isInstanceOf(ValidationException.class);
    }
}
