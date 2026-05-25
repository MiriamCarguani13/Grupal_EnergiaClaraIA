package com.energiaclara.application.analytics.port.in;

import com.energiaclara.application.analytics.dto.KpiSnapshotResult;

import java.util.List;

public interface GetKpiSnapshotsUseCase {
    List<KpiSnapshotResult> kpis();
}
