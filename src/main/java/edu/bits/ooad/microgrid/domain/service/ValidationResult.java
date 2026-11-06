package edu.bits.ooad.microgrid.domain.service;

import edu.bits.ooad.microgrid.domain.model.PowerPlan;

import java.util.Collections;
import java.util.List;

/**
 * Result record returned by PlanValidator.
 */
public record ValidationResult(
        boolean valid,
        PowerPlan validatedPlan,
        List<String> auditLogs) {

    public ValidationResult {
        auditLogs = (auditLogs != null) ? List.copyOf(auditLogs) : Collections.emptyList();
    }
}

