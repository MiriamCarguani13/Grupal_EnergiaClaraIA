package com.energiaclara.application.port.out;

import com.energiaclara.core.domain.energy.EnergyBaseline;
import com.energiaclara.core.domain.shared.TenantId;

public interface EnergyBaselineProviderPort {
    EnergyBaseline currentBaselineFor(TenantId tenantId);
}
