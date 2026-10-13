package com.servicesphere.cases;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<SupportCase, Long> {

    Optional<SupportCase> findByCaseNumber(String caseNumber);

    boolean existsByCaseNumber(String caseNumber);

    long countByStatus(CaseStatus status);

    long countByPriority(CasePriority priority);

    long countByCategory(CaseCategory category);

    long countByAssignedAgentIdAndStatusNotIn(Long agentId, List<CaseStatus> terminalStatuses);

    @Query("SELECT c FROM SupportCase c WHERE " +
           "(:customerId IS NULL OR c.customer.id = :customerId) AND " +
           "(:agentId IS NULL OR c.assignedAgent.id = :agentId) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:query IS NULL OR LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<SupportCase> filterCases(
            @Param("customerId") Long customerId,
            @Param("agentId") Long agentId,
            @Param("status") CaseStatus status,
            @Param("priority") CasePriority priority,
            @Param("category") CaseCategory category,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("SELECT c FROM SupportCase c WHERE " +
           "c.status NOT IN :terminalStatuses AND " +
           "(c.resolutionDeadline < :now OR (c.firstRespondedAt IS NULL AND c.firstResponseDeadline < :now)) AND " +
           "c.slaEscalated = false")
    List<SupportCase> findBreachedCases(
            @Param("now") Instant now,
            @Param("terminalStatuses") List<CaseStatus> terminalStatuses
    );

    @Query("SELECT c FROM SupportCase c WHERE " +
           "c.status NOT IN :terminalStatuses AND " +
           "c.resolutionDeadline >= :now AND c.resolutionDeadline <= :approachingThreshold")
    List<SupportCase> findCasesApproachingDeadline(
            @Param("now") Instant now,
            @Param("approachingThreshold") Instant approachingThreshold,
            @Param("terminalStatuses") List<CaseStatus> terminalStatuses
    );

    @Query("SELECT COUNT(c) FROM SupportCase c WHERE " +
           "c.status NOT IN :terminalStatuses AND " +
           "(c.resolutionDeadline < :now OR (c.firstRespondedAt IS NULL AND c.firstResponseDeadline < :now))")
    long countBreachedCases(
            @Param("now") Instant now,
            @Param("terminalStatuses") List<CaseStatus> terminalStatuses
    );

    List<SupportCase> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<SupportCase> findTop5ByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
