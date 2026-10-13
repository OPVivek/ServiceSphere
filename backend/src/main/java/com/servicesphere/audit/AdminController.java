package com.servicesphere.audit;

import com.servicesphere.audit.dto.AuditLogDto;
import com.servicesphere.auth.dto.UserResponse;
import com.servicesphere.common.ApiResponse;
import com.servicesphere.common.PagedResponse;
import com.servicesphere.common.exception.ResourceNotFoundException;
import com.servicesphere.security.Role;
import com.servicesphere.security.UserPrincipal;
import com.servicesphere.user.User;
import com.servicesphere.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "User administration, roles, and immutable audit logs")
public class AdminController {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public AdminController(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @GetMapping("/users")
    @Operation(summary = "Search all system users with role and pagination (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<User> users = userRepository.searchUsers(role, query, PageRequest.of(page, size, Sort.by("id")));
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(users.map(UserResponse::fromUser))));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update user role (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        Role oldRole = user.getRole();
        user.setRole(role);
        user = userRepository.save(user);

        User actor = userRepository.findById(principal.getId()).orElse(null);
        auditService.recordEvent(actor, "UPDATE_USER_ROLE", "USER", String.valueOf(id),
                "Role changed from " + oldRole + " to " + role, null);

        return ResponseEntity.ok(ApiResponse.success("User role updated", UserResponse.fromUser(user)));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Activate or deactivate user account (Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(active);
        user = userRepository.save(user);

        User actor = userRepository.findById(principal.getId()).orElse(null);
        auditService.recordEvent(actor, active ? "ACTIVATE_USER" : "DEACTIVATE_USER",
                "USER", String.valueOf(id), "Active state changed to " + active, null);

        return ResponseEntity.ok(ApiResponse.success("User status updated", UserResponse.fromUser(user)));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get immutable system audit trail (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogDto>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PagedResponse<AuditLogDto> logs = auditService.getAuditLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
