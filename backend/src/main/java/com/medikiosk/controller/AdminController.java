package com.medikiosk.controller;

import com.medikiosk.dto.ApiResponse;
import com.medikiosk.dto.admin.AuditLogDetailDto;
import com.medikiosk.dto.admin.CreateUserRequestDto;
import com.medikiosk.dto.admin.SystemAnalyticsDto;
import com.medikiosk.dto.admin.SystemConfigSummaryDto;
import com.medikiosk.dto.admin.UserManagementDto;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.exception.ApiException;
import com.medikiosk.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<SystemAnalyticsDto>> getAnalytics(Authentication authentication) {
        checkAdminRole(authentication);
        SystemAnalyticsDto analytics = adminService.getSystemAnalytics();
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserManagementDto>>> getUsers(
            Authentication authentication,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) String query) {
        checkAdminRole(authentication);
        List<UserManagementDto> users = adminService.getAllUsers(role, query);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserManagementDto>> createUser(
            Authentication authentication,
            @Valid @RequestBody CreateUserRequestDto request) {
        User adminUser = checkAdminRole(authentication);
        UserManagementDto created = adminService.createUser(request, adminUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<UserManagementDto>> updateUserStatus(
            Authentication authentication,
            @PathVariable UUID userId,
            @RequestParam UserStatus status) {
        User adminUser = checkAdminRole(authentication);
        UserManagementDto updated = adminService.updateUserStatus(userId, status, adminUser);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogDetailDto>>> getAuditLogs(
            Authentication authentication,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "100") int limit) {
        checkAdminRole(authentication);
        List<AuditLogDetailDto> logs = adminService.getAuditLogs(action, userId, limit);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<SystemConfigSummaryDto>> getConfigSummary(Authentication authentication) {
        checkAdminRole(authentication);
        SystemConfigSummaryDto config = adminService.getSystemConfigSummary();
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    private User checkAdminRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new ApiException("Authentication required", HttpStatus.UNAUTHORIZED);
        }
        User user = (User) authentication.getPrincipal();
        if (user.getRole() != UserRole.ADMIN) {
            throw new ApiException("Access denied: Requires ADMIN role", HttpStatus.FORBIDDEN);
        }
        return user;
    }
}
