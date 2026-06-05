package com.energiaclara.application.port.in;

import com.energiaclara.application.analytics.dto.KpiSnapshotResult;

import java.util.List;

public interface GetKpiSnapshotsUseCase {
    List<KpiSnapshotResult> kpis();
}
