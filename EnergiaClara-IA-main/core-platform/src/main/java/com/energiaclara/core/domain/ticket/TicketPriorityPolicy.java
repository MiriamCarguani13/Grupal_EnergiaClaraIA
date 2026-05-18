package com.energiaclara.core.domain.ticket;

import com.energiaclara.core.domain.energy.AnomalySeverity;
import com.energiaclara.core.domain.energy.KwhValue;

public class TicketPriorityPolicy {
    public static TicketPriority calculate(AnomalySeverity severity, KwhValue estimatedWaste) {
        if (severity == AnomalySeverity.CRITICAL || estimatedWaste.getValue() > 500) {
            return TicketPriority.URGENT;
        }
        if (severity == AnomalySeverity.WARNING || estimatedWaste.getValue() > 200) {
            return TicketPriority.HIGH;
        }
        if (estimatedWaste.getValue() > 50) {
            return TicketPriority.MEDIUM;
        }
        return TicketPriority.LOW;
    }
}
