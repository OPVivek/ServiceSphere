package com.servicesphere.sla;

import com.servicesphere.cases.*;
import com.servicesphere.notification.NotificationService;
import com.servicesphere.notification.NotificationType;
import com.servicesphere.security.Role;
import com.servicesphere.user.User;
import com.servicesphere.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaServiceTest {

    @Mock
    private SlaPolicyRepository slaPolicyRepository;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private CaseHistoryRepository caseHistoryRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SlaService slaService;

    private User agent;
    private User customer;

    @BeforeEach
    void setUp() {
        agent = new User("agent@servicesphere.io", "hash", "Sarah", "Agent", Role.AGENT);
        agent.setId(5L);
        customer = new User("customer@acme.com", "hash", "John", "Customer", Role.CUSTOMER);
        customer.setId(10L);
    }

    @Test
    @DisplayName("Calculate deadlines using configured SlaPolicy")
    void testCalculateDeadlines() {
        SlaPolicy policy = new SlaPolicy(CasePriority.CRITICAL, 1, 4, "Critical");
        when(slaPolicyRepository.findByPriority(CasePriority.CRITICAL)).thenReturn(Optional.of(policy));

        Instant now = Instant.parse("2026-10-10T12:00:00Z");
        SlaService.Deadlines deadlines = slaService.calculateDeadlines(CasePriority.CRITICAL, now);

        assertEquals(now.plus(Duration.ofHours(1)), deadlines.firstResponseDeadline());
        assertEquals(now.plus(Duration.ofHours(4)), deadlines.resolutionDeadline());
    }

    @Test
    @DisplayName("SLA escalation must be strictly idempotent")
    void testEscalateCaseIdempotency() {
        SupportCase breachedCase = new SupportCase();
        breachedCase.setId(100L);
        breachedCase.setCaseNumber("CASE-202610-0099");
        breachedCase.setCustomer(customer);
        breachedCase.setAssignedAgent(agent);
        breachedCase.setSlaEscalated(true); // Already escalated

        slaService.escalateCase(breachedCase, Instant.now());

        // Since already escalated, should NOT save again or emit duplicate audit/notifications
        verify(caseRepository, never()).save(breachedCase);
        verify(caseHistoryRepository, never()).save(any());
        verify(notificationService, never()).createNotification(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("First-time SLA escalation updates flag, records audit history, and notifies agent and admin")
    void testEscalateCaseFirstTime() {
        SupportCase breachedCase = new SupportCase();
        breachedCase.setId(101L);
        breachedCase.setCaseNumber("CASE-202610-0101");
        breachedCase.setCustomer(customer);
        breachedCase.setAssignedAgent(agent);
        breachedCase.setSlaEscalated(false);

        User admin = new User("admin@servicesphere.io", "hash", "Admin", "User", Role.ADMIN);
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of(admin));

        Instant now = Instant.now();
        slaService.escalateCase(breachedCase, now);

        assertTrue(breachedCase.isSlaEscalated());
        verify(caseRepository, times(1)).save(breachedCase);
        verify(caseHistoryRepository, times(1)).save(any(CaseHistory.class));
        verify(notificationService, atLeastOnce()).createNotification(eq(agent), anyString(), anyString(), eq(NotificationType.SLA_BREACH), eq(101L), eq("CASE"));
    }
}
