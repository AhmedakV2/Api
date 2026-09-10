package com.aft.api.user.web;

import com.aft.api.user.dto.PreferenceBulkRequest;
import com.aft.api.user.dto.PreferenceDto;
import com.aft.api.user.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{id}/preferences")
@Tag(name = "Kullanici Tercihi", description = "Tema, dil ve panel tercihleri")
public class UserPreferenceController {
    private final UserPreferenceService preferenceService;

    public UserPreferenceController(UserPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    @PreAuthorize("@aft.isSelf(#id, authentication)")
    @Operation(summary = "Kullanıcı tercihlerini getirir")
    public List<PreferenceDto> getPreferences(@PathVariable UUID id) {
        return preferenceService.findAll(id);
    }

    @PutMapping
    @PreAuthorize("@aft.isSelf(#id, authentication)")
    @Operation(summary = "Kullanıcı tercihlerini yazma")
    public List<PreferenceDto> replace(@PathVariable UUID id, @Valid @RequestBody PreferenceBulkRequest request) {
        return preferenceService.replaceAll(id, request.preferences());
    }
}
