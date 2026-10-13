package com.servicesphere.assignment;

import com.servicesphere.cases.CaseCategory;
import com.servicesphere.cases.CasePriority;
import com.servicesphere.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AgentAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(AgentAssignmentService.class);

    private final AgentProfileRepository agentProfileRepository;

    public AgentAssignmentService(AgentProfileRepository agentProfileRepository) {
        this.agentProfileRepository = agentProfileRepository;
    }

    /**
     * Rule-based transparent scoring algorithm for agent assignment:
     * - Filters for available agents with headroom under maxActiveCases
     * - Adds skill bonus (+50 points) when agent specializes in case category
     * - Adds capacity headroom points (headroom * 10)
     * - Subtracts utilization penalty to avoid overloading
     * - Priority weighting: for CRITICAL/HIGH cases, prioritizes exact skill matches
     */
    @Transactional
    public AssignmentResult assignAgent(CaseCategory category, CasePriority priority) {
        List<AgentProfile> eligibleAgents = agentProfileRepository.findEligibleAvailableAgents();

        if (eligibleAgents.isEmpty()) {
            log.info("No available eligible agents with capacity. Routing to unassigned queue.");
            return AssignmentResult.unassigned("No agents currently available with open capacity. Placed in unassigned queue.");
        }

        // Score each candidate
        Optional<ScoredAgent> bestCandidate = eligibleAgents.stream()
                .map(profile -> scoreAgent(profile, category, priority))
                .max(Comparator.comparingDouble(ScoredAgent::score)
                        .thenComparing(s -> -s.profile().getCurrentActiveCases()));

        if (bestCandidate.isEmpty()) {
            return AssignmentResult.unassigned("Unable to match suitable agent. Placed in unassigned queue.");
        }

        ScoredAgent selected = bestCandidate.get();
        AgentProfile profile = selected.profile();

        // Increment current active cases
        profile.setCurrentActiveCases(profile.getCurrentActiveCases() + 1);
        agentProfileRepository.save(profile);

        String explanation = String.format(
                "Rule-based routing selected %s (Score: %.1f | Skill match: %s | Active load: %d/%d)",
                profile.getUser().getFullName(),
                selected.score(),
                selected.hasSkillMatch() ? "YES" : "NO",
                profile.getCurrentActiveCases(),
                profile.getMaxActiveCases()
        );

        log.info("Case assigned: {}", explanation);
        return new AssignmentResult(profile.getUser(), selected.score(), explanation);
    }

    public ScoredAgent scoreAgent(AgentProfile profile, CaseCategory category, CasePriority priority) {
        double score = 0.0;
        boolean hasSkillMatch = profile.hasSkill(category.name());

        // 1. Skill match bonus
        if (hasSkillMatch) {
            score += 50.0;
            if (priority == CasePriority.CRITICAL || priority == CasePriority.HIGH) {
                score += 25.0; // Extra bonus for critical skills
            }
        }

        // 2. Capacity headroom bonus
        int headroom = profile.getMaxActiveCases() - profile.getCurrentActiveCases();
        score += headroom * 10.0;

        // 3. Utilization penalty
        double utilization = (double) profile.getCurrentActiveCases() / profile.getMaxActiveCases();
        score -= (utilization * 20.0);

        return new ScoredAgent(profile, score, hasSkillMatch);
    }

    @Transactional
    public void decrementAgentLoad(User agent) {
        if (agent == null) return;
        agentProfileRepository.findByUserId(agent.getId()).ifPresent(profile -> {
            if (profile.getCurrentActiveCases() > 0) {
                profile.setCurrentActiveCases(profile.getCurrentActiveCases() - 1);
                agentProfileRepository.save(profile);
                log.info("Decremented active load for agent {}: {}/{}",
                        agent.getEmail(), profile.getCurrentActiveCases(), profile.getMaxActiveCases());
            }
        });
    }

    @Transactional
    public void incrementAgentLoad(User agent) {
        if (agent == null) return;
        agentProfileRepository.findByUserId(agent.getId()).ifPresent(profile -> {
            profile.setCurrentActiveCases(profile.getCurrentActiveCases() + 1);
            agentProfileRepository.save(profile);
            log.info("Incremented active load for agent {}: {}/{}",
                    agent.getEmail(), profile.getCurrentActiveCases(), profile.getMaxActiveCases());
        });
    }

    public record ScoredAgent(AgentProfile profile, double score, boolean hasSkillMatch) {}
}
