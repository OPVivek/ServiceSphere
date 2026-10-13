package com.servicesphere.cases;

import com.servicesphere.cases.dto.*;
import com.servicesphere.common.ApiResponse;
import com.servicesphere.common.PagedResponse;
import com.servicesphere.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cases")
@Tag(name = "Case Management", description = "Core customer support case workflows")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @PostMapping
    @Operation(summary = "Create a new support case")
    public ResponseEntity<ApiResponse<CaseDto>> createCase(
            @Valid @RequestBody CaseCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CaseDto createdCase = caseService.createCase(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Case created successfully", createdCase));
    }

    @GetMapping
    @Operation(summary = "Search and filter support cases with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<CaseDto>>> getCases(
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) CasePriority priority,
            @RequestParam(required = false) CaseCategory category,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long agentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<CaseDto> cases = caseService.getCases(
                status, priority, category, query, customerId, agentId, principal, page, size
        );
        return ResponseEntity.ok(ApiResponse.success(cases));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed case information, comments, and history")
    public ResponseEntity<ApiResponse<CaseDetailDto>> getCaseDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CaseDetailDto detail = caseService.getCaseDetail(id, principal);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update case status (with state machine validation)")
    public ResponseEntity<ApiResponse<CaseDto>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CaseStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CaseDto updatedCase = caseService.updateStatus(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success("Case status updated", updatedCase));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Manually assign or reassign agent (Staff only)")
    public ResponseEntity<ApiResponse<CaseDto>> assignCase(
            @PathVariable Long id,
            @Valid @RequestBody CaseAssignmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CaseDto updatedCase = caseService.assignCase(id, request, principal);
        return ResponseEntity.ok(ApiResponse.success("Agent assigned successfully", updatedCase));
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment or internal note to a case")
    public ResponseEntity<ApiResponse<CaseCommentDto>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CaseCommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CaseCommentDto comment = caseService.addComment(id, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", comment));
    }
}
