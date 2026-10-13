package com.servicesphere.assignment;

import com.servicesphere.assignment.dto.AgentAvailabilityRequest;
import com.servicesphere.assignment.dto.AgentDto;
import com.servicesphere.common.ApiResponse;
import com.servicesphere.common.exception.ResourceNotFoundException;
import com.servicesphere.security.Role;
import com.servicesphere.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/agents")
@Tag(name = "Agent Management", description = "Agent skills, capacity, and availability settings")
public class AgentController {

    private final AgentProfileRepository agentProfileRepository;

    public AgentController(AgentProfileRepository agentProfileRepository) {
        this.agentProfileRepository = agentProfileRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "List all support agents, their skills and load")
    public ResponseEntity<ApiResponse<List<AgentDto>>> getAgents() {
        List<AgentDto> agents = agentProfileRepository.findAllWithUser().stream()
                .map(AgentDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(agents));
    }

    @PatchMapping("/{userId}/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    @Operation(summary = "Update agent availability and max capacity")
    public ResponseEntity<ApiResponse<AgentDto>> updateAvailability(
            @PathVariable Long userId,
            @RequestBody AgentAvailabilityRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // Only Admins or the agent themselves can update their settings
        if (principal.getRole() != Role.ADMIN && !principal.getId().equals(userId)) {
            throw new AccessDeniedException("You cannot modify another agent's availability");
        }

        AgentProfile profile = agentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent profile", "userId", userId));

        if (request.getAvailable() != null) {
            profile.setAvailable(request.getAvailable());
        }
        if (request.getMaxActiveCases() != null && request.getMaxActiveCases() > 0) {
            profile.setMaxActiveCases(request.getMaxActiveCases());
        }
        if (request.getSkills() != null && !request.getSkills().isBlank()) {
            profile.setSkills(request.getSkills().trim());
        }

        profile = agentProfileRepository.save(profile);
        return ResponseEntity.ok(ApiResponse.success("Availability updated", AgentDto.fromEntity(profile)));
    }
}
