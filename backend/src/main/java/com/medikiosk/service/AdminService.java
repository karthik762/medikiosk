package com.medikiosk.service;

import com.medikiosk.dto.admin.AuditLogDetailDto;
import com.medikiosk.dto.admin.CreateUserRequestDto;
import com.medikiosk.dto.admin.SystemAnalyticsDto;
import com.medikiosk.dto.admin.SystemConfigSummaryDto;
import com.medikiosk.dto.admin.UserManagementDto;
import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import com.medikiosk.entity.enums.UserStatus;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    SystemAnalyticsDto getSystemAnalytics();
    List<UserManagementDto> getAllUsers(UserRole roleFilter, String searchQuery);
    UserManagementDto createUser(CreateUserRequestDto request, User adminUser);
    UserManagementDto updateUserStatus(UUID userId, UserStatus status, User adminUser);
    List<AuditLogDetailDto> getAuditLogs(String actionFilter, UUID userIdFilter, int limit);
    SystemConfigSummaryDto getSystemConfigSummary();
}
