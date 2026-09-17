package com.aft.api.security.web;

import com.aft.api.security.AftPrincipal;
import com.aft.api.security.dto.*;
import com.aft.api.security.service.AuthService;
import com.aft.api.security.service.WsTicketService;
import com.aft.api.user.dto.ChangePasswordRequest;
import com.aft.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Kimlik", description = "Oturum, jeton ve parola islemleri")
public class AuthController {
    private final AuthService authService;
    private final WsTicketService wsTicketService;
    private final UserService userService;

    public AuthController(AuthService authService, WsTicketService wsTicketService, UserService userService) {
        this.authService = authService;
        this.wsTicketService = wsTicketService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Kullanici adi ve parola ile jeton alma")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return authService.login(request, userAgent(servletRequest), clientIp(servletRequest));
    }

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Yeni hesap olusturma ve jeton alma")
    public TokenResponse register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        return authService.register(request, userAgent(servletRequest), clientIp(servletRequest));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "Erisim jetonu yenileme")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest servletRequest) {
        return authService.refresh(request.refreshToken(), userAgent(servletRequest), clientIp(servletRequest));
    }

    @PostMapping("/logout")
    @Operation(summary = "Yenileme jetonunu iptal etme")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request,
                                       @AuthenticationPrincipal AftPrincipal principal) {
        authService.logout(request.refreshToken(), principal.userId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Profil, roller ve organizasyonlar")
    public MeResponse me(@AuthenticationPrincipal AftPrincipal principal) {
        return authService.me(principal.userId());
    }

    @PostMapping("/password")
    @Operation(summary = "Parola degistirme")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               @AuthenticationPrincipal AftPrincipal principal) {
        userService.changePassword(principal.userId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ws-ticket")
    @Operation(summary = "WebSocket icin tek kullanimlik jeton")
    public WsTicketResponse wsTicket(@AuthenticationPrincipal AftPrincipal principal) {
        WsTicketService.Ticket ticket = wsTicketService.issue(principal.userId());
        return new WsTicketResponse(ticket.value(), ticket.expiresInSeconds());
    }

    private String userAgent(HttpServletRequest request) {
        String value = request.getHeader(HttpHeaders.USER_AGENT);
        return value == null ? null : value.substring(0, Math.min(value.length(), 255));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
