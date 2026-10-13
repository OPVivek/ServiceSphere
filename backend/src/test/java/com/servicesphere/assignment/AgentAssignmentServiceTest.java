package com.servicesphere.assignment;

import com.servicesphere.cases.CaseCategory;
import com.servicesphere.cases.CasePriority;
import com.servicesphere.security.Role;
import com.servicesphere.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentAssignmentServiceTest {

    @Mock
    private AgentProfileRepository agentProfileRepository;

    @InjectMocks
    private AgentAssignmentService assignmentService;

    private User agentTech;
    private User agentGeneral;
    private AgentProfile profileTech;
    private AgentProfile profileGeneral;

    @BeforeEach
    void setUp() {
        agentTech = new User("tech@servicesphere.io", "hash", "Tech", "Lead", Role.AGENT);
        agentTech.setId(10L);

        agentGeneral = new User("gen@servicesphere.io", "hash", "Gen", "Lead", Role.AGENT);
        agentGeneral.setId(20L);

        profileTech = new AgentProfile(agentTech, "TECHNICAL,BILLING", 10);
        profileTech.setCurrentActiveCases(2);

        profileGeneral = new AgentProfile(agentGeneral, "GENERAL,ACCOUNT", 10);
        profileGeneral.setCurrentActiveCases(1);
    }

    @Test
    @DisplayName("Should select agent with matching skill bonus for Technical case")
    void shouldPrioritizeSkillMatchOverLowerCaseload() {
        when(agentProfileRepository.findEligibleAvailableAgents())
                .thenReturn(List.of(profileGeneral, profileTech));

        AssignmentResult result = assignmentService.assignAgent(CaseCategory.TECHNICAL, CasePriority.HIGH);

        assertTrue(result.isAssigned());
        assertEquals("Tech Lead", result.getAgent().getFullName());
        assertTrue(result.getReason().contains("Skill match: YES"));
        assertEquals(3, profileTech.getCurrentActiveCases());
        verify(agentProfileRepository, times(1)).save(profileTech);
    }

    @Test
    @DisplayName("Should route to unassigned queue when no eligible agents exist")
    void shouldReturnUnassignedWhenNoAgentsAvailable() {
        when(agentProfileRepository.findEligibleAvailableAgents())
                .thenReturn(List.of());

        AssignmentResult result = assignmentService.assignAgent(CaseCategory.BILLING, CasePriority.CRITICAL);

        assertFalse(result.isAssigned());
        assertNull(result.getAgent());
        assertTrue(result.getReason().contains("unassigned queue"));
    }

    @Test
    @DisplayName("Workload scoring awards headroom and subtracts utilization")
    void testScoringFormula() {
        AgentAssignmentService.ScoredAgent scored = assignmentService.scoreAgent(
                profileTech, CaseCategory.TECHNICAL, CasePriority.CRITICAL
        );

        // Skill bonus (50) + Critical bonus (25) + Headroom (8 * 10 = 80) - Utilization ((2/10)*20 = 4) = 151.0
        assertEquals(151.0, scored.score(), 0.001);
        assertTrue(scored.hasSkillMatch());
    }
}
