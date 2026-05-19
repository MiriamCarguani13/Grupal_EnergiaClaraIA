package com.energiaclara.infrastructure.energyops.persistence.adapter;

import com.energiaclara.application.port.out.EnergyBaselineProviderPort;
import com.energiaclara.core.domain.energy.EnergyBaseline;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.infrastructure.energyops.persistence.repository.SnapshotLineaBaseRepository;
import org.springframework.stereotype.Component;

/**
 * SKELETON. Implementa {@link EnergyBaselineProviderPort}.
 * TODO equipo EnergyOps:
 * 1. snapshotRepo.findAllByInquilinoIdAndActivoTrue(tenantId.value()) — filtra columna `activo` agregada por DBA seeds.sql
 * 2. Map<FacilityId, KwhValue> a partir de valor_p95 + tolerancia_porcentaje
 * 3. Considerar cache Redis para v2 (cargar todas en cada llamada es caro)
 */
@Component
public class EnergyBaselineProviderAdapter implements EnergyBaselineProviderPort {

    private final SnapshotLineaBaseRepository snapshotRepo;

    public EnergyBaselineProviderAdapter(SnapshotLineaBaseRepository snapshotRepo) {
        this.snapshotRepo = snapshotRepo;
    }

    @Override
    public EnergyBaseline currentBaselineFor(TenantId tenantId) {
        // TODO equipo EnergyOps:
        // var rows = snapshotRepo.findAllByInquilinoIdAndActivoTrue(tenantId.value());
        // Map<FacilityId, KwhValue> map = new HashMap<>();
        // for (var row : rows) {
        //     map.put(FacilityId.of(row.getMedidorId()), KwhValue.of(row.getValorP95().doubleValue()));
        // }
        // return new EnergyBaseline(tenantId, map);
        throw new UnsupportedOperationException("SKELETON: implementar currentBaselineFor() en EnergyBaselineProviderAdapter");
    }
}
