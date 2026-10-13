package com.servicesphere.assignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentProfileRepository extends JpaRepository<AgentProfile, Long> {
    Optional<AgentProfile> findByUserId(Long userId);

    @Query("SELECT ap FROM AgentProfile ap JOIN FETCH ap.user u WHERE ap.available = true AND ap.currentActiveCases < ap.maxActiveCases")
    List<AgentProfile> findEligibleAvailableAgents();

    @Query("SELECT ap FROM AgentProfile ap JOIN FETCH ap.user u")
    List<AgentProfile> findAllWithUser();
}
