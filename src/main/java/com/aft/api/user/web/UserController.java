package com.aft.api.user.web;

import com.aft.api.common.dto.PageResponse;
import com.aft.api.user.dto.AssignRoleRequest;
import com.aft.api.user.dto.ChangePasswordRequest;
import com.aft.api.user.dto.CreateUserRequest;
import com.aft.api.user.dto.UpdateUserRequest;
import com.aft.api.user.dto.UserDto;
import com.aft.api.user.entity.RoleCode;
import com.aft.api.user.entity.UserStatus;
import com.aft.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Kullanici", description = "Kullanici ve rol yonetimi")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kullanici listesi ve filtre")
    public PageResponse<UserDto> list(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) UserStatus status,
                                      @ParameterObject @PageableDefault(size = 20, sort = "createdAt",
                                              direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.of(userService.search(q, status, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kullanici olustur")
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest request) {
        UserDto created = userService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @aft.isSelf(#id, authentication)")
    @Operation(summary = "Kullanici detay")
    public UserDto get(@PathVariable UUID id) {
        return userService.get(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @aft.isSelf(#id, authentication)")
    @Operation(summary = "Kullanici guncelle")
    public UserDto update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kullanici sil")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.disable(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kullaniciya rol ata")
    public UserDto assignRole(@PathVariable UUID id, @Valid @RequestBody AssignRoleRequest request) {
        return userService.assignRole(id, request.code());
    }

    @DeleteMapping("/{id}/roles/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kullanici rol sil")
    public UserDto revokeRole(@PathVariable UUID id, @PathVariable RoleCode code) {
        return userService.revokeRole(id, code);
    }

    @PostMapping("/{id}/password")
    @PreAuthorize("@aft.isSelf(#id, authentication)")
    @Operation(summary = "Parola degistir")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
