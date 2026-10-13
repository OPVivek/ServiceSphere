package com.servicesphere.cases;

import com.servicesphere.common.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CaseStateMachineTest {

    private final CaseService caseService = new CaseService(null, null, null, null, null, null, null);

    @Test
    @DisplayName("Valid status transitions should pass without exception")
    void testValidTransitions() {
        assertDoesNotThrow(() -> caseService.validateStatusTransition(CaseStatus.OPEN, CaseStatus.IN_PROGRESS));
        assertDoesNotThrow(() -> caseService.validateStatusTransition(CaseStatus.IN_PROGRESS, CaseStatus.WAITING_FOR_CUSTOMER));
        assertDoesNotThrow(() -> caseService.validateStatusTransition(CaseStatus.WAITING_FOR_CUSTOMER, CaseStatus.RESOLVED));
        assertDoesNotThrow(() -> caseService.validateStatusTransition(CaseStatus.RESOLVED, CaseStatus.CLOSED));
        assertDoesNotThrow(() -> caseService.validateStatusTransition(CaseStatus.RESOLVED, CaseStatus.OPEN));
    }

    @Test
    @DisplayName("Transitioning from CLOSED should throw InvalidStateTransitionException")
    void testClosedIsTerminalState() {
        assertThrows(InvalidStateTransitionException.class, () ->
                caseService.validateStatusTransition(CaseStatus.CLOSED, CaseStatus.OPEN));
        assertThrows(InvalidStateTransitionException.class, () ->
                caseService.validateStatusTransition(CaseStatus.CLOSED, CaseStatus.IN_PROGRESS));
        assertThrows(InvalidStateTransitionException.class, () ->
                caseService.validateStatusTransition(CaseStatus.CLOSED, CaseStatus.RESOLVED));
    }

    @Test
    @DisplayName("Resolved case cannot transition to arbitrary states other than CLOSED or OPEN")
    void testResolvedTransitionLimits() {
        assertThrows(InvalidStateTransitionException.class, () ->
                caseService.validateStatusTransition(CaseStatus.RESOLVED, CaseStatus.WAITING_FOR_CUSTOMER));
        assertThrows(InvalidStateTransitionException.class, () ->
                caseService.validateStatusTransition(CaseStatus.RESOLVED, CaseStatus.IN_PROGRESS));
    }
}
