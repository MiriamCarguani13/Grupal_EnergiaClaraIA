package com.energiaclara.infrastructure.energyops.persistence.adapter;

import com.energiaclara.application.port.out.EnergyBaselineProviderPort;
import com.energiaclara.core.domain.energy.EnergyBaseline;
import com.energiaclara.core.domain.shared.TenantId;
import org.springframework.stereotype.Component;

/**
 * SKELETON. Implementa {@link EnergyBaselineProviderPort}.
 * TODO equipo EnergyOps: leer todas las baselines activas del tenant + armar Map<FacilityId, KwhValue>.
 * Considerar cache Redis para v2 (cargar todas en cada llamada es caro).
 */
@Component
public class EnergyBaselineProviderAdapter implements EnergyBaselineProviderPort {

    @Override
    public EnergyBaseline currentBaselineFor(TenantId tenantId) {
        // TODO equipo: query snapshot_linea_base WHERE inquilino_id=? AND activo=1
        // Mapear cada row a Map<FacilityId, KwhValue> usando valor_p95 + tolerancia
        throw new UnsupportedOperationException("SKELETON: implementar currentBaselineFor() en EnergyBaselineProviderAdapter");
    }
}
